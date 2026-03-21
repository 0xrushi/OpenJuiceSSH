package com.daremote.app.feature.terminal

import android.content.Context
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daremote.app.core.data.ssh.SshSessionManager
import com.daremote.app.core.domain.repository.ServerRepository
import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient
import com.termux.view.TerminalView
import com.termux.view.TerminalViewClient
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.schmizz.sshj.connection.channel.direct.Session
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.lang.ref.WeakReference
import javax.inject.Inject

data class TerminalSessionData(
    val id: Int,
    val name: String,
    val isConnected: Boolean = false,
    val terminalSession: TerminalSession? = null
)

data class TerminalState(
    val sessions: List<TerminalSessionData> = emptyList(),
    val currentSessionId: Int = 0,
    val serverName: String = "",
    val isConnected: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TerminalViewModel @Inject constructor(
    private val sessionManager: SshSessionManager,
    private val serverRepository: ServerRepository,
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context
) : ViewModel(), TerminalSessionClient, TerminalViewClient {

    private val serverId: Long = savedStateHandle["serverId"] ?: 0L
    private val _state = MutableStateFlow(TerminalState())
    val state: StateFlow<TerminalState> = _state
    private var sessionCounter = 0

    private val shellMap = mutableMapOf<Int, Pair<Session.Shell, OutputStream>>()
    private var terminalViewRef: WeakReference<TerminalView>? = null

    init {
        viewModelScope.launch {
            try {
                val server = serverRepository.getServerById(serverId)
                if (server == null) {
                    _state.update { it.copy(error = "Server not found") }
                    return@launch
                }
                _state.update { it.copy(serverName = server.name) }

                if (!sessionManager.isConnected(serverId)) {
                    sessionManager.connect(server)
                }
                _state.update { it.copy(isConnected = true) }
                openNewSession()
            } catch (e: Exception) {
                _state.update { it.copy(error = "Connection failed: ${e.message}") }
            }
        }
    }

    fun setTerminalView(view: TerminalView) {
        terminalViewRef = WeakReference(view)
    }

    private fun findShellPath(): String {
        val candidates = listOf(
            "/system/bin/cat",
            "/system/bin/sh",
            "/system/bin/toybox",
            "/system/bin/toolbox",
            "/vendor/bin/sh",
            "/system/xbin/sh"
        )
        return candidates.firstOrNull { File(it).canExecute() }
            ?: "/system/bin/sh"
    }

    fun openNewSession() {
        viewModelScope.launch {
            try {
                val client = sessionManager.getSession(serverId)
                if (client == null) {
                    _state.update { it.copy(error = "SSH client session not available") }
                    return@launch
                }

                val sshResult = withContext(Dispatchers.IO) {
                    val session = client.startSession()
                    session.allocatePTY("xterm-256color", 80, 24, 0, 0, emptyMap())
                    val shell = session.startShell()
                    session to shell
                }

                val (_, shell) = sshResult

                val termSession = withContext(Dispatchers.Main) {
                    val shellPath = findShellPath()
                    val session = TerminalSession(
                        shellPath,
                        context.filesDir.absolutePath,
                        arrayOf(shellPath),
                        arrayOf("TERM=xterm-256color", "HOME=" + context.filesDir.absolutePath),
                        10000,
                        this@TerminalViewModel
                    )
                    // initializeEmulator creates the emulator BEFORE starting the subprocess.
                    // Even if the subprocess fails, the emulator is still usable.
                    try {
                        session.initializeEmulator(80, 24)
                    } catch (_: Exception) {
                        // Subprocess may fail on some devices (e.g. /system/bin/sh inaccessible).
                        // The emulator is already set at this point, so we can still use it
                        // by feeding SSH output directly via emulator.append().
                    }
                    session
                }

                val sessionId = sessionCounter++
                shellMap[sessionId] = shell to shell.outputStream

                val sessionData = TerminalSessionData(
                    id = sessionId,
                    name = "Session ${sessionId + 1}",
                    isConnected = true,
                    terminalSession = termSession
                )

                _state.update { state ->
                    state.copy(
                        sessions = state.sessions + sessionData,
                        currentSessionId = sessionId
                    )
                }

                readShellOutput(sessionId, shell.inputStream, termSession)
            } catch (e: Exception) {
                _state.update { it.copy(error = "Failed to start shell: ${e.message}") }
            }
        }
    }

    fun switchSession(sessionId: Int) {
        _state.update { it.copy(currentSessionId = sessionId) }
    }

    private fun readShellOutput(sessionId: Int, inputStream: InputStream, termSession: TerminalSession) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val buffer = ByteArray(16384)
                while (true) {
                    val bytesRead = inputStream.read(buffer)
                    if (bytesRead == -1) break
                    // Write directly to the terminal emulator, bypassing the PTY.
                    // This works even if the local subprocess failed to start.
                    val emulator = termSession.emulator ?: continue
                    emulator.append(buffer, bytesRead)
                    terminalViewRef?.get()?.let { view ->
                        view.post { view.onScreenUpdated() }
                    }
                }
            } catch (_: Exception) { }
        }
    }

    // TerminalSessionClient implementation
    override fun onTextChanged(session: TerminalSession) {
        terminalViewRef?.get()?.onScreenUpdated()
    }
    override fun onSessionFinished(session: TerminalSession) {}
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

    // TerminalViewClient implementation
    override fun onScale(scale: Float): Float = scale

    override fun onSingleTapUp(e: MotionEvent) {
        terminalViewRef?.get()?.let { view ->
            view.requestFocus()
            val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun getSessionId(session: TerminalSession): Int {
        return _state.value.sessions.find { it.terminalSession == session }?.id ?: _state.value.currentSessionId
    }

    override fun shouldBackButtonBeMappedToEscape(): Boolean = false
    override fun shouldEnforceCharBasedInput(): Boolean = false
    override fun shouldUseCtrlSpaceWorkaround(): Boolean = false
    override fun isTerminalViewSelected(): Boolean = true
    override fun copyModeChanged(boolean: Boolean) {}
    override fun onKeyUp(keyCode: Int, e: KeyEvent): Boolean = false
    override fun onLongPress(event: MotionEvent): Boolean = false
    override fun readControlKey(): Boolean = false
    override fun readAltKey(): Boolean = false
    override fun readShiftKey(): Boolean = false
    override fun readFnKey(): Boolean = false
    override fun onEmulatorSet() {}

    override fun onKeyDown(keyCode: Int, e: KeyEvent, session: TerminalSession): Boolean {
        val sessionId = getSessionId(session)
        val bytes: ByteArray? = when (keyCode) {
            KeyEvent.KEYCODE_ENTER -> "\r".toByteArray()
            KeyEvent.KEYCODE_DEL -> byteArrayOf(0x7f) // Backspace -> DEL
            KeyEvent.KEYCODE_FORWARD_DEL -> "\u001b[3~".toByteArray()
            KeyEvent.KEYCODE_TAB -> "\t".toByteArray()
            KeyEvent.KEYCODE_ESCAPE -> "\u001b".toByteArray()
            KeyEvent.KEYCODE_DPAD_UP -> "\u001b[A".toByteArray()
            KeyEvent.KEYCODE_DPAD_DOWN -> "\u001b[B".toByteArray()
            KeyEvent.KEYCODE_DPAD_RIGHT -> "\u001b[C".toByteArray()
            KeyEvent.KEYCODE_DPAD_LEFT -> "\u001b[D".toByteArray()
            KeyEvent.KEYCODE_MOVE_HOME -> "\u001b[1~".toByteArray()
            KeyEvent.KEYCODE_MOVE_END -> "\u001b[4~".toByteArray()
            KeyEvent.KEYCODE_PAGE_UP -> "\u001b[5~".toByteArray()
            KeyEvent.KEYCODE_PAGE_DOWN -> "\u001b[6~".toByteArray()
            KeyEvent.KEYCODE_INSERT -> "\u001b[2~".toByteArray()
            else -> null
        }
        if (bytes != null) {
            sendInput(sessionId, bytes)
            return true
        }
        return false
    }

    override fun onCodePoint(codePoint: Int, ctrlDown: Boolean, session: TerminalSession): Boolean {
        val sessionId = getSessionId(session)
        if (ctrlDown) {
            if (codePoint in 'a'.code..'z'.code || codePoint in 'A'.code..'Z'.code) {
                val ctrl = (Character.toLowerCase(codePoint.toChar()) - 'a' + 1).toByte()
                sendInput(sessionId, byteArrayOf(ctrl))
                return true
            }
        }
        val text = StringBuilder().appendCodePoint(codePoint).toString()
        sendInput(sessionId, text.toByteArray())
        return true
    }

    fun sendInput(sessionId: Int, data: ByteArray) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                shellMap[sessionId]?.second?.let { output ->
                    output.write(data)
                    output.flush()
                }
            } catch (_: Exception) { }
        }
    }

    override fun onCleared() {
        shellMap.values.forEach { (shell, _) ->
            try { shell.close() } catch (_: Exception) { }
        }
        shellMap.clear()
        _state.value.sessions.forEach { it.terminalSession?.finishIfRunning() }
    }
}
