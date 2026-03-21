package com.daremote.app.feature.terminal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daremote.app.core.data.ssh.SshSessionManager
import com.daremote.app.core.domain.repository.ServerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.schmizz.sshj.connection.channel.direct.Session
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

data class TerminalSession(
    val id: Int,
    val name: String,
    val outputLines: List<String> = emptyList(),
    val shell: Session.Shell? = null
)

data class TerminalState(
    val sessions: List<TerminalSession> = emptyList(),
    val currentSessionId: Int = 0,
    val commandHistory: List<String> = emptyList(),
    val serverName: String = "",
    val isConnected: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TerminalViewModel @Inject constructor(
    private val sessionManager: SshSessionManager,
    private val serverRepository: ServerRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val serverId: Long = savedStateHandle["serverId"] ?: 0L
    private val _state = MutableStateFlow(TerminalState())
    val state: StateFlow<TerminalState> = _state
    private var sessionCounter = 0
    private val shellMap = mutableMapOf<Int, Pair<Session.Shell, OutputStream>>()

    init {
        viewModelScope.launch {
            val server = serverRepository.getServerById(serverId)
            _state.update { it.copy(serverName = server?.name ?: "Unknown") }
            if (sessionManager.isConnected(serverId)) {
                _state.update { it.copy(isConnected = true) }
                openNewSession()
            }
        }
    }

    fun openNewSession() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val client = sessionManager.getSession(serverId) ?: return@launch
                val session = client.startSession()
                session.allocateDefaultPTY()
                val shell = session.startShell()
                val sessionId = sessionCounter++
                val termSession = TerminalSession(
                    id = sessionId,
                    name = "Session ${sessionId + 1}"
                )
                shellMap[sessionId] = shell to shell.outputStream
                _state.update { state ->
                    state.copy(
                        sessions = state.sessions + termSession,
                        currentSessionId = sessionId
                    )
                }
                readShellOutput(sessionId, shell.inputStream)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun switchSession(sessionId: Int) {
        _state.update { it.copy(currentSessionId = sessionId) }
    }

    fun closeSession(sessionId: Int) {
        shellMap.remove(sessionId)?.first?.close()
        _state.update { state ->
            val remaining = state.sessions.filter { it.id != sessionId }
            state.copy(
                sessions = remaining,
                currentSessionId = remaining.lastOrNull()?.id ?: 0
            )
        }
    }

    fun sendInput(text: String) {
        val sessionId = _state.value.currentSessionId
        viewModelScope.launch(Dispatchers.IO) {
            try {
                shellMap[sessionId]?.second?.let { output ->
                    output.write(text.toByteArray())
                    output.flush()
                }
                if (text.endsWith("\n") || text.endsWith("\r")) {
                    val cmd = text.trim()
                    if (cmd.isNotEmpty()) {
                        _state.update { it.copy(commandHistory = it.commandHistory + cmd) }
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    private fun readShellOutput(sessionId: Int, inputStream: InputStream) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val buffer = ByteArray(4096)
                while (true) {
                    val bytesRead = inputStream.read(buffer)
                    if (bytesRead == -1) break
                    val text = String(buffer, 0, bytesRead)
                    _state.update { state ->
                        val sessions = state.sessions.map { session ->
                            if (session.id == sessionId) {
                                val lines = session.outputLines.toMutableList()
                                text.split("\n").forEach { line ->
                                    if (lines.isEmpty()) lines.add(line)
                                    else lines[lines.lastIndex] = lines.last() + line
                                    if (text.contains("\n")) lines.add("")
                                }
                                session.copy(outputLines = lines)
                            } else session
                        }
                        state.copy(sessions = sessions)
                    }
                }
            } catch (_: Exception) { }
        }
    }

    override fun onCleared() {
        shellMap.values.forEach { (shell, _) ->
            try { shell.close() } catch (_: Exception) { }
        }
        shellMap.clear()
    }
}
