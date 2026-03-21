package com.daremote.app.feature.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daremote.app.core.domain.model.KeyType
import com.daremote.app.core.domain.model.SshKey
import com.daremote.app.core.security.SshKeyManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SshKeyManagerState(
    val keys: List<SshKey> = emptyList(),
    val isGenerating: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SshKeyManagerViewModel @Inject constructor(
    private val sshKeyManager: SshKeyManager
) : ViewModel() {

    private val _state = MutableStateFlow(SshKeyManagerState())
    val state: StateFlow<SshKeyManagerState> = _state

    // In a full impl, keys would be stored in Room. For now, in-memory list.
    private val keysList = mutableListOf<SshKey>()

    fun generateKey(name: String, type: KeyType) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(isGenerating = true, error = null) }
            try {
                val key = sshKeyManager.generateKeyPair(name, type)
                keysList.add(key)
                _state.update { it.copy(keys = keysList.toList(), isGenerating = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isGenerating = false, error = e.message) }
            }
        }
    }

    fun deleteKey(key: SshKey) {
        sshKeyManager.deleteKey(key.privateKeyRef)
        keysList.removeAll { it.privateKeyRef == key.privateKeyRef }
        _state.update { it.copy(keys = keysList.toList()) }
    }
}
