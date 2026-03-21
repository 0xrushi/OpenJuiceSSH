package com.daremote.app.feature.proxies

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daremote.app.core.domain.model.Proxy
import com.daremote.app.core.domain.model.ProxyAuthType
import com.daremote.app.core.domain.model.ProxyType
import com.daremote.app.core.domain.model.SshKey
import com.daremote.app.core.domain.repository.ProxyRepository
import com.daremote.app.core.domain.repository.SshKeyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditProxyState(
    val name: String = "",
    val type: ProxyType = ProxyType.SSH,
    val host: String = "",
    val port: String = "",
    val username: String = "",
    val authType: ProxyAuthType = ProxyAuthType.NONE,
    val password: String = "",
    val sshKeyId: Long? = null,
    val availableKeys: List<SshKey> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false
)

@HiltViewModel
class AddEditProxyViewModel @Inject constructor(
    private val proxyRepository: ProxyRepository,
    private val sshKeyRepository: SshKeyRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val proxyId: Long? = savedStateHandle.get<Long>("proxyId")

    var state by mutableStateOf(AddEditProxyState())
        private set

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val keys = sshKeyRepository.getAllKeys().first()
            state = state.copy(availableKeys = keys)

            if (proxyId != null && proxyId != -1L) {
                state = state.copy(isLoading = true)
                proxyRepository.getProxyById(proxyId)?.let { proxy ->
                    state = state.copy(
                        name = proxy.name,
                        type = proxy.type,
                        host = proxy.host,
                        port = proxy.port.toString(),
                        username = proxy.username ?: "",
                        authType = proxy.authType,
                        password = proxy.password ?: "",
                        sshKeyId = proxy.sshKeyId,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onNameChange(name: String) { state = state.copy(name = name) }
    fun onTypeChange(type: ProxyType) { state = state.copy(type = type) }
    fun onHostChange(host: String) { state = state.copy(host = host) }
    fun onPortChange(port: String) { state = state.copy(port = port) }
    fun onUsernameChange(username: String) { state = state.copy(username = username) }
    fun onAuthTypeChange(authType: ProxyAuthType) { state = state.copy(authType = authType) }
    fun onPasswordChange(password: String) { state = state.copy(password = password) }
    fun onSshKeyChange(sshKeyId: Long?) { state = state.copy(sshKeyId = sshKeyId) }

    fun saveProxy() {
        viewModelScope.launch {
            if (state.name.isBlank() || state.host.isBlank() || state.port.isBlank()) {
                _eventFlow.emit(UiEvent.ShowSnackbar("Please fill in all required fields"))
                return@launch
            }

            state = state.copy(isSaving = true)
            try {
                val currentId = if (proxyId == null || proxyId == -1L) 0L else proxyId
                val proxy = Proxy(
                    id = currentId,
                    name = state.name,
                    type = state.type,
                    host = state.host,
                    port = state.port.toIntOrNull() ?: 0,
                    username = if (state.username.isNotBlank()) state.username else null,
                    authType = state.authType,
                    password = if (state.password.isNotBlank()) state.password else null,
                    sshKeyId = state.sshKeyId
                )
                proxyRepository.saveProxy(proxy)
                _eventFlow.emit(UiEvent.SaveSuccess)
            } catch (e: Exception) {
                state = state.copy(isSaving = false)
                _eventFlow.emit(UiEvent.ShowSnackbar("Error saving proxy: ${e.message}"))
            }
        }
    }

    fun deleteProxy() {
        viewModelScope.launch {
            if (proxyId != null && proxyId != -1L) {
                proxyRepository.getProxyById(proxyId)?.let {
                    proxyRepository.deleteProxy(it)
                    _eventFlow.emit(UiEvent.SaveSuccess)
                }
            }
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        object SaveSuccess : UiEvent()
    }
}
