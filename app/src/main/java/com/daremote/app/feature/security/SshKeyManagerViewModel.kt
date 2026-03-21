package com.daremote.app.feature.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daremote.app.core.domain.model.KeyType
import com.daremote.app.core.domain.model.SshKey
import com.daremote.app.core.domain.repository.SshKeyRepository
import com.daremote.app.core.security.SshKeyManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SshKeyManagerState(
    val keys: List<SshKey> = emptyList(),
    val isGenerating: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SshKeyManagerViewModel @Inject constructor(
    private val sshKeyManager: SshKeyManager,
    private val sshKeyRepository: SshKeyRepository
) : ViewModel() {

    private val _isGenerating = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val state: StateFlow<SshKeyManagerState> = combine(
        sshKeyRepository.getAllKeys(),
        _isGenerating,
        _error
    ) { keys, isGenerating, error ->
        SshKeyManagerState(keys = keys, isGenerating = isGenerating, error = error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SshKeyManagerState())

    fun generateKey(name: String, type: KeyType) {
        viewModelScope.launch(Dispatchers.IO) {
            _isGenerating.value = true
            _error.value = null
            try {
                val key = sshKeyManager.generateKeyPair(name, type)
                sshKeyRepository.saveKey(key)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun importKey(name: String, privateKeyContent: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isGenerating.value = true
            _error.value = null
            try {
                val key = sshKeyManager.importKey(name, privateKeyContent)
                sshKeyRepository.saveKey(key)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun deleteKey(key: SshKey) {
        viewModelScope.launch {
            sshKeyManager.deleteKey(key.privateKeyRef)
            sshKeyRepository.deleteKey(key)
        }
    }
}
