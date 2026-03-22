package com.openjuicessh.app.core.data.ssh

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.openjuicessh.app.core.service.SshConnectionService
import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.schmizz.sshj.connection.channel.direct.Session
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

data class TerminalSessionData(
    val serverId: Long,
    val sessionId: Int,
    val name: String,
    val terminalSession: TerminalSession,
    val shell: Session.Shell,
    val outputStream: OutputStream
)

@Singleton
class TerminalSessionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionManager: SshSessionManager
) : TerminalSessionClient {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _sessions = MutableStateFlow<Map<Long, List<TerminalSessionData>>>(emptyMap())
    val sessions = _sessions.asStateFlow()

    private var sessionCounter = 0
    private val listeners = mutableSetOf<TerminalSessionListener>()

    interface TerminalSessionListener {
        fun onSessionUpdated(serverId: Long, sessionId: Int)
    }

    fun addListener(listener: TerminalSessionListener) = listeners.add(listener)
    fun removeListener(listener: TerminalSessionListener) = listeners.remove(listener)

    private fun findShellPath(): String {
        val candidates = listOf(
            "/system/bin/sh", "/system/bin/toybox", "/system/bin/toolbox",
            "/vendor/bin/sh", "/system/xbin/sh"
        )
        return candidates.firstOrNull { File(it).canExecute() } ?: "/system/bin/sh"
    }

    suspend fun openSession(serverId: Long): Int = withContext(Dispatchers.IO) {
        val client = sessionManager.getSession(serverId) ?: throw Exception("SSH client not connected")
        
        val session = client.startSession()
        session.allocatePTY("xterm-256color", 80, 24, 0, 0, emptyMap())
        val shell = session.startShell()
        val outputStream = shell.outputStream
        val inputStream = shell.inputStream

        val terminalSessionId = sessionCounter++
        
        val termSession = withContext(Dispatchers.Main) {
            val shellPath = findShellPath()
            TerminalSession(
                shellPath, context.filesDir.absolutePath, arrayOf(shellPath),
                arrayOf("TERM=xterm-256color", "HOME=${context.filesDir.absolutePath}"),
                10000, this@TerminalSessionManager
            ).also { 
                try { it.initializeEmulator(80, 24) } catch (_: Exception) {} 
            }
        }

        val data = TerminalSessionData(
            serverId, terminalSessionId, "Session ${terminalSessionId + 1}",
            termSession, shell, outputStream
        )

        _sessions.update { current ->
            val list = current[serverId] ?: emptyList()
            current + (serverId to (list + data))
        }

        readShellOutput(data, inputStream)
        
        // Start foreground service to keep process alive
        val intent = Intent(context, SshConnectionService::class.java)
        ContextCompat.startForegroundService(context, intent)

        terminalSessionId
    }

    private fun readShellOutput(data: TerminalSessionData, inputStream: InputStream) {
        scope.launch(Dispatchers.IO) {
            try {
                val buf = ByteArray(16384)
                while (true) {
                    val n = inputStream.read(buf); if (n == -1) break
                    val emulator = data.terminalSession.emulator ?: continue
                    emulator.append(buf, n)
                    withContext(Dispatchers.Main) {
                        listeners.forEach { it.onSessionUpdated(data.serverId, data.sessionId) }
                    }
                }
            } catch (_: Exception) {
                // Handle session closed/error
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
                try { it.shell.close() } catch (_: Exception) {}
                it.terminalSession.finishIfRunning()
            }
            val newList = list.filter { it.sessionId != sessionId }
            if (newList.isEmpty()) current - serverId else current + (serverId to newList)
        }
    }

    fun closeAllSessions() {
        _sessions.value.values.flatten().forEach {
            try { it.shell.close() } catch (_: Exception) {}
            it.terminalSession.finishIfRunning()
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

    // TerminalSessionClient implementation
    override fun onTextChanged(session: TerminalSession) {
        findSession(session)?.let { (serverId, sessionId) ->
            listeners.forEach { it.onSessionUpdated(serverId, sessionId) }
        }
    }

    private fun findSession(session: TerminalSession): Pair<Long, Int>? {
        _sessions.value.forEach { (serverId, sessions) ->
            sessions.find { it.terminalSession == session }?.let { return serverId to it.sessionId }
        }
        return null
    }

    override fun onSessionFinished(session: TerminalSession) {
        findSession(session)?.let { closeSession(it.first, it.second) }
    }
    
    override fun onCopyTextToClipboard(session: TerminalSession, text: String) {}
    override fun onPasteTextFromClipboard(session: TerminalSession) {}
    override fun onBell(session: TerminalSession) {}
    override fun onColorsChanged(session: TerminalSession) {}
    override fun onTitleChanged(session: TerminalSession) {}
    override fun onTerminalCursorStateChange(state: Boolean) {}
    override fun getTerminalCursorStyle(): Int? = null
    override fun logError(tag: String, message: String) {}
    override fun logWarn(tag: String, message: String) {}
    override fun logInfo(tag: String, message: String) {}
    override fun logDebug(tag: String, message: String) {}
    override fun logVerbose(tag: String, message: String) {}
    override fun logStackTraceWithMessage(tag: String, message: String, e: Exception) {}
    override fun logStackTrace(tag: String, e: Exception) {}
}
