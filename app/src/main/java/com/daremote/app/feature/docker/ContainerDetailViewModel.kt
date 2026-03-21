package com.daremote.app.feature.docker

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daremote.app.core.data.ssh.SshCommandExecutor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContainerDetailState(
    val containerId: String = "",
    val containerName: String = "",
    val logs: List<String> = emptyList(),
    val stats: String = "",
    val isLoading: Boolean = true,
    val selectedTab: Int = 0
)

@HiltViewModel
class ContainerDetailViewModel @Inject constructor(
    private val commandExecutor: SshCommandExecutor,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val serverId: Long = savedStateHandle["serverId"] ?: 0L
    private val containerId: String = savedStateHandle["containerId"] ?: ""
    private val _state = MutableStateFlow(ContainerDetailState(containerId = containerId))
    val state: StateFlow<ContainerDetailState> = _state

    init {
        loadInfo()
        loadLogs()
    }

    private fun loadInfo() {
        viewModelScope.launch {
            val result = commandExecutor.execute(serverId,
                "docker inspect --format '{{.Name}}' $containerId")
            result.onSuccess { name ->
                _state.update { it.copy(containerName = name.trim().removePrefix("/")) }
            }
        }
    }

    fun loadLogs() {
        viewModelScope.launch {
            val result = commandExecutor.execute(serverId, "docker logs --tail 200 $containerId")
            result.onSuccess { output ->
                _state.update { it.copy(logs = output.lines(), isLoading = false) }
            }
            result.onFailure { e ->
                _state.update { it.copy(logs = listOf("Error: ${e.message}"), isLoading = false) }
            }
        }
    }

    fun loadStats() {
        viewModelScope.launch {
            val result = commandExecutor.execute(serverId,
                "docker stats --no-stream --format 'CPU: {{.CPUPerc}} | Mem: {{.MemUsage}} | Net: {{.NetIO}} | Block: {{.BlockIO}}' $containerId")
            result.onSuccess { output ->
                _state.update { it.copy(stats = output.trim()) }
            }
        }
    }

    fun selectTab(tab: Int) {
        _state.update { it.copy(selectedTab = tab) }
        when (tab) {
            0 -> loadLogs()
            1 -> loadStats()
        }
    }
}
