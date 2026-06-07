package com.openjuicessh.app.core.data.ssh

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.openjuicessh.app.core.service.SshConnectionService
import com.openjuicessh.app.core.terminal.GhosttyBridge
import com.openjuicessh.app.core.terminal.TerminalSnapshot
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.schmizz.sshj.connection.channel.direct.Session
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

data class TerminalSessionData(
    val serverId: Long,
    val sessionId: Int,
    val name: String,
    val ghosttyHandle: Long,
    val shell: Session.Shell,
    val outputStream: OutputStream,
    val snapshotFlow: MutableStateFlow<TerminalSnapshot?> = MutableStateFlow(null)
)

@Singleton
class TerminalSessionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionManager: SshSessionManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val bridge = GhosttyBridge()
    private val _sessions = MutableStateFlow<Map<Long, List<TerminalSessionData>>>(emptyMap())
    val sessions = _sessions.asStateFlow()

    private var sessionCounter = 0
    private val listeners = mutableSetOf<TerminalSessionListener>()

    interface TerminalSessionListener {
        fun onSessionUpdated(serverId: Long, sessionId: Int)
    }

    fun addListener(listener: TerminalSessionListener) = listeners.add(listener)
    fun removeListener(listener: TerminalSessionListener) = listeners.remove(listener)

    suspend fun openSession(serverId: Long): Int = withContext(Dispatchers.IO) {
        val client = sessionManager.getSession(serverId) ?: throw Exception("SSH client not connected")

        val session = client.startSession()
        session.allocatePTY("xterm-256color", 80, 24, 0, 0, emptyMap())
        val shell = session.startShell()
        val outputStream = shell.outputStream
        val inputStream = shell.inputStream

        val terminalSessionId = sessionCounter++

        val ghosttyHandle = withContext(Dispatchers.Main) {
            bridge.nativeCreate(cols = 80, rows = 24, maxScrollback = 1000)
        }

        val data = TerminalSessionData(
            serverId, terminalSessionId, "Session ${terminalSessionId + 1}",
            ghosttyHandle, shell, outputStream
        )

        _sessions.update { current ->
            val list = current[serverId] ?: emptyList()
            current + (serverId to (list + data))
        }

        readShellOutput(data, inputStream)

        val intent = Intent(context, SshConnectionService::class.java)
        ContextCompat.startForegroundService(context, intent)

        terminalSessionId
    }

    private fun readShellOutput(data: TerminalSessionData, inputStream: InputStream) {
        scope.launch(Dispatchers.IO) {
            try {
                val buf = ByteArray(16384)
                var lastSnapshotTime = 0L
                while (true) {
                    val n = inputStream.read(buf)
                    if (n == -1) break

                    bridge.nativeWriteRemote(data.ghosttyHandle, buf.sliceArray(0 until n))

                    val ptyData = bridge.nativeDrainPtyWrites(data.ghosttyHandle)
                    if (ptyData.isNotEmpty()) {
                        data.outputStream.write(ptyData)
                        data.outputStream.flush()
                    }

                    val now = System.currentTimeMillis()
                    if (now - lastSnapshotTime > 16) {
                        val snapshotBuf = bridge.nativeSnapshot(data.ghosttyHandle)
                        val imagesBuf = bridge.nativeSnapshotImages(data.ghosttyHandle)
                        val images = TerminalSnapshot.parseImages(imagesBuf)
                        val snapshot = TerminalSnapshot.fromByteBuffer(snapshotBuf, images)

                        data.snapshotFlow.emit(snapshot)
                        lastSnapshotTime = now

                        withContext(Dispatchers.Main) {
                            listeners.forEach { it.onSessionUpdated(data.serverId, data.sessionId) }
                        }
                    }
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    closeSession(data.serverId, data.sessionId)
                }
            }
        }
    }

    fun closeSession(serverId: Long, sessionId: Int) {
        _sessions.update { current ->
            val list = current[serverId] ?: emptyList()
            val sessionToClose = list.find { it.sessionId == sessionId }
            sessionToClose?.let {
                try { bridge.nativeDestroy(it.ghosttyHandle) } catch (_: Exception) {}
                try { it.shell.close() } catch (_: Exception) {}
            }
            val newList = list.filter { it.sessionId != sessionId }
            if (newList.isEmpty()) current - serverId else current + (serverId to newList)
        }
    }

    fun closeAllSessions() {
        _sessions.value.values.flatten().forEach {
            try { bridge.nativeDestroy(it.ghosttyHandle) } catch (_: Exception) {}
            try { it.shell.close() } catch (_: Exception) {}
        }
        _sessions.update { emptyMap() }
    }

    fun sendInput(serverId: Long, sessionId: Int, data: ByteArray) {
        scope.launch(Dispatchers.IO) {
            _sessions.value[serverId]?.find { it.sessionId == sessionId }?.outputStream?.let {
                try { it.write(data); it.flush() } catch (_: Exception) {}
            }
        }
    }

    fun resize(serverId: Long, sessionId: Int, cols: Int, rows: Int, cellW: Int, cellH: Int) {
        scope.launch(Dispatchers.IO) {
            val session = _sessions.value[serverId]?.find { it.sessionId == sessionId } ?: return@launch
            bridge.nativeResize(session.ghosttyHandle, cols, rows, cellW, cellH)
            try {
                session.shell.changeWindowDimensions(cols, rows, 0, 0)
            } catch (_: Exception) {}
        }
    }

    fun encodeKey(serverId: Long, sessionId: Int, key: Int, cp: Int, mods: Int, action: Int): ByteArray? {
        val session = _sessions.value[serverId]?.find { it.sessionId == sessionId } ?: return null
        return bridge.nativeEncodeKey(session.ghosttyHandle, key, cp, mods, action, null)
    }

    fun scroll(serverId: Long, sessionId: Int, delta: Int, x: Float, y: Float) {
        scope.launch(Dispatchers.IO) {
            val session = _sessions.value[serverId]?.find { it.sessionId == sessionId } ?: return@launch
            bridge.nativeScroll(session.ghosttyHandle, delta, x, y)
            val ptyData = bridge.nativeDrainPtyWrites(session.ghosttyHandle)
            if (ptyData.isNotEmpty()) {
                try { session.outputStream.write(ptyData); session.outputStream.flush() } catch (_: Exception) {}
            }
        }
    }
}
