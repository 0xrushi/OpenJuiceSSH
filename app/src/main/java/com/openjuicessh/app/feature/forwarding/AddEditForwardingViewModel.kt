package com.openjuicessh.app.feature.forwarding

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openjuicessh.app.core.domain.model.ForwardingRule
import com.openjuicessh.app.core.domain.model.ForwardingType
import com.openjuicessh.app.core.domain.model.Proxy
import com.openjuicessh.app.core.domain.model.Server
import com.openjuicessh.app.core.domain.repository.ForwardingRepository
import com.openjuicessh.app.core.domain.repository.ProxyRepository
import com.openjuicessh.app.core.domain.repository.ServerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditForwardingState(
    val name: String = "",
    val type: ForwardingType = ForwardingType.LOCAL,
    val localHost: String = "127.0.0.1",
    val localPort: String = "",
    val remoteHost: String = "",
    val remotePort: String = "",
    val selectedServerId: Long? = null,
    val proxyId: Long? = null,
    val autoConnect: Boolean = false,
    val servers: List<Server> = emptyList(),
    val availableProxies: List<Proxy> = emptyList(),
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddEditForwardingViewModel @Inject constructor(
    private val forwardingRepository: ForwardingRepository,
    private val serverRepository: ServerRepository,
    private val proxyRepository: ProxyRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val ruleId: Long? = savedStateHandle.get<String>("ruleId")?.toLongOrNull()
    private val _state = MutableStateFlow(AddEditForwardingState())
    val state: StateFlow<AddEditForwardingState> = _state

    init {
        viewModelScope.launch {
            val servers = serverRepository.getAllServers().first()
            val proxies = proxyRepository.getAllProxies().first()
            _state.update { it.copy(servers = servers, availableProxies = proxies) }

            ruleId?.let { id ->
                forwardingRepository.getRuleById(id)?.let { rule ->
                    _state.update {
                        it.copy(
                            name = rule.name,
                            type = rule.type,
                            localHost = rule.localHost,
                            localPort = rule.localPort.toString(),
                            remoteHost = rule.remoteHost ?: "",
                            remotePort = rule.remotePort?.toString() ?: "",
                            selectedServerId = rule.serverId,
                            proxyId = rule.proxyId,
                            autoConnect = rule.autoConnect,
                            isEditing = true
                        )
                    }
                }
            }
        }
    }

    fun updateName(v: String) = _state.update { it.copy(name = v) }
    fun updateType(v: ForwardingType) = _state.update { it.copy(type = v) }
    fun updateLocalHost(v: String) = _state.update { it.copy(localHost = v) }
    fun updateLocalPort(v: String) = _state.update { it.copy(localPort = v) }
    fun updateRemoteHost(v: String) = _state.update { it.copy(remoteHost = v) }
    fun updateRemotePort(v: String) = _state.update { it.copy(remotePort = v) }
    fun updateServer(id: Long) = _state.update { it.copy(selectedServerId = id) }
    fun updateProxyId(id: Long?) = _state.update { it.copy(proxyId = id) }
    fun updateAutoConnect(v: Boolean) = _state.update { it.copy(autoConnect = v) }

    fun save() {
        val s = _state.value
        if (s.name.isBlank() || s.localPort.isBlank() || s.selectedServerId == null) {
            _state.update { it.copy(error = "Please fill in all required fields") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            try {
                val rule = ForwardingRule(
                    id = ruleId ?: 0,
                    serverId = s.selectedServerId,
                    type = s.type,
                    name = s.name,
                    localHost = s.localHost,
                    localPort = s.localPort.toInt(),
                    remoteHost = s.remoteHost.ifBlank { null },
                    remotePort = s.remotePort.toIntOrNull(),
                    proxyId = s.proxyId,
                    autoConnect = s.autoConnect
                )
                if (ruleId != null) {
                    forwardingRepository.updateRule(rule)
                } else {
                    forwardingRepository.saveRule(rule)
                }
                _state.update { it.copy(isSaving = false, saved = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }
}
