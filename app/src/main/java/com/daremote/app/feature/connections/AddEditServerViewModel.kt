package com.daremote.app.feature.connections

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daremote.app.core.domain.model.AuthType
import com.daremote.app.core.domain.model.Server
import com.daremote.app.core.domain.repository.ServerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditServerState(
    val name: String = "",
    val host: String = "",
    val port: String = "22",
    val username: String = "",
    val password: String = "",
    val authType: AuthType = AuthType.PASSWORD,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false
)

@HiltViewModel
class AddEditServerViewModel @Inject constructor(
    private val serverRepository: ServerRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val serverId: Long? = savedStateHandle.get<String>("serverId")?.toLongOrNull()
    private val _state = MutableStateFlow(AddEditServerState())
    val state: StateFlow<AddEditServerState> = _state

    private var existingServer: Server? = null

    init {
        if (serverId != null) {
            viewModelScope.launch {
                serverRepository.getServerById(serverId)?.let { server ->
                    existingServer = server
                    _state.update {
                        it.copy(
                            name = server.name,
                            host = server.host,
                            port = server.port.toString(),
                            username = server.username,
                            authType = server.authType,
                            isEditing = true
                        )
                    }
                }
            }
        }
    }

    fun updateName(name: String) = _state.update { it.copy(name = name) }
    fun updateHost(host: String) = _state.update { it.copy(host = host) }
    fun updatePort(port: String) = _state.update { it.copy(port = port) }
    fun updateUsername(username: String) = _state.update { it.copy(username = username) }
    fun updatePassword(password: String) = _state.update { it.copy(password = password) }
    fun updateAuthType(authType: AuthType) = _state.update { it.copy(authType = authType) }

    fun save() {
        val s = _state.value
        if (s.name.isBlank() || s.host.isBlank() || s.username.isBlank()) {
            _state.update { it.copy(error = "Please fill in all required fields") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            try {
                val port = s.port.toIntOrNull() ?: 22
                if (existingServer != null) {
                    val updated = existingServer!!.copy(
                        name = s.name,
                        host = s.host,
                        port = port,
                        username = s.username,
                        authType = s.authType
                    )
                    serverRepository.updateServer(
                        updated,
                        if (s.password.isNotBlank()) s.password else null
                    )
                } else {
                    val server = Server(
                        name = s.name,
                        host = s.host,
                        port = port,
                        username = s.username,
                        authType = s.authType,
                        credentialRef = ""
                    )
                    serverRepository.saveServer(server, s.password)
                }
                _state.update { it.copy(isSaving = false, saved = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }
}
