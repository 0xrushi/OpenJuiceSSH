package com.daremote.app.feature.connections

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daremote.app.core.domain.model.AuthType
import com.daremote.app.core.domain.model.Proxy
import com.daremote.app.core.domain.model.Server
import com.daremote.app.core.domain.repository.ProxyRepository
import com.daremote.app.core.domain.repository.ServerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditServerState(
    val name: String = "",
    val host: String = "",
    val port: String = "22",
    val username: String = "",
    val password: String = "",
    val authType: AuthType = AuthType.PASSWORD,
    val proxyId: Long? = null,
    val availableProxies: List<Proxy> = emptyList(),
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false
)

@HiltViewModel
class AddEditServerViewModel @Inject constructor(
    private val serverRepository: ServerRepository,
    private val proxyRepository: ProxyRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val serverId: Long? = savedStateHandle.get<String>("serverId")?.toLongOrNull()
    private val _internalState = MutableStateFlow(AddEditServerState())
    
    val state: StateFlow<AddEditServerState> = combine(
        _internalState,
        proxyRepository.getAllProxies()
    ) { state, proxies ->
        // If we just added a proxy, auto-select it if nothing was selected
        val updatedProxyId = if (state.proxyId == null && proxies.size > state.availableProxies.size) {
            proxies.maxByOrNull { it.id }?.id
        } else {
            state.proxyId
        }
        state.copy(
            availableProxies = proxies,
            proxyId = updatedProxyId
        )
    }.onEach { newState ->
        // Sync back the auto-selected proxyId to _internalState if it changed
        if (newState.proxyId != _internalState.value.proxyId) {
            _internalState.update { it.copy(proxyId = newState.proxyId) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = _internalState.value
    )

    private var existingServer: Server? = null

    init {
        if (serverId != null) {
            viewModelScope.launch {
                serverRepository.getServerById(serverId)?.let { server ->
                    existingServer = server
                    _internalState.update {
                        it.copy(
                            name = server.name,
                            host = server.host,
                            port = server.port.toString(),
                            username = server.username,
                            authType = server.authType,
                            proxyId = server.proxyId,
                            isEditing = true
                        )
                    }
                }
            }
        }
    }

    fun updateName(name: String) = _internalState.update { it.copy(name = name) }
    fun updateHost(host: String) = _internalState.update { it.copy(host = host) }
    fun updatePort(port: String) = _internalState.update { it.copy(port = port) }
    fun updateUsername(username: String) = _internalState.update { it.copy(username = username) }
    fun updatePassword(password: String) = _internalState.update { it.copy(password = password) }
    fun updateAuthType(authType: AuthType) = _internalState.update { it.copy(authType = authType) }
    fun updateProxyId(proxyId: Long?) = _internalState.update { it.copy(proxyId = proxyId) }

    fun save() {
        val s = _internalState.value
        if (s.name.isBlank() || s.host.isBlank() || s.username.isBlank()) {
            _internalState.update { it.copy(error = "Please fill in all required fields") }
            return
        }

        viewModelScope.launch {
            _internalState.update { it.copy(isSaving = true, error = null) }
            try {
                val portInt = s.port.toIntOrNull() ?: 22
                if (existingServer != null) {
                    val updated = existingServer!!.copy(
                        name = s.name,
                        host = s.host,
                        port = portInt,
                        username = s.username,
                        authType = s.authType,
                        proxyId = s.proxyId
                    )
                    serverRepository.updateServer(
                        updated,
                        if (s.password.isNotBlank()) s.password else null
                    )
                } else {
                    val server = Server(
                        name = s.name,
                        host = s.host,
                        port = portInt,
                        username = s.username,
                        authType = s.authType,
                        credentialRef = "",
                        proxyId = s.proxyId
                    )
                    serverRepository.saveServer(server, s.password)
                }
                _internalState.update { it.copy(isSaving = false, saved = true) }
            } catch (e: Exception) {
                _internalState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }
}
