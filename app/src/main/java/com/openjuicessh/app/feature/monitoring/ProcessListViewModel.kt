package com.openjuicessh.app.feature.monitoring

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openjuicessh.app.core.data.ssh.SshCommandExecutor
import com.openjuicessh.app.core.domain.model.ProcessInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProcessListState(
    val processes: List<ProcessInfo> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ProcessListViewModel @Inject constructor(
    private val commandExecutor: SshCommandExecutor,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val serverId: Long = savedStateHandle["serverId"] ?: 0L
    private val _state = MutableStateFlow(ProcessListState())
    val state: StateFlow<ProcessListState> = _state

    init { refresh() }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = commandExecutor.execute(serverId, "ps aux --sort=-%cpu | head -50")
            result.onSuccess { output ->
                val processes = output.lines().drop(1).filter { it.isNotBlank() }.mapNotNull { line ->
                    val parts = line.trim().split(Regex("\\s+"), limit = 11)
                    if (parts.size >= 11) {
                        ProcessInfo(
                            pid = parts[1].toIntOrNull() ?: 0,
                            user = parts[0],
                            cpuPercent = parts[2].toFloatOrNull() ?: 0f,
                            memPercent = parts[3].toFloatOrNull() ?: 0f,
                            command = parts[10]
                        )
                    } else null
                }
                _state.update { it.copy(processes = processes, error = null) }
            }.onFailure { e ->
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun killProcess(pid: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            commandExecutor.execute(serverId, "kill -9 $pid")
            refresh()
        }
    }
}
