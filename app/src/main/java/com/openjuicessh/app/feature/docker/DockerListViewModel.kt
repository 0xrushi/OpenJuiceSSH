package com.openjuicessh.app.feature.docker

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openjuicessh.app.core.data.ssh.SshCommandExecutor
import com.openjuicessh.app.core.domain.model.ContainerState
import com.openjuicessh.app.core.domain.model.DockerContainer
import com.openjuicessh.app.core.domain.repository.ServerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DockerListState(
    val serverName: String = "",
    val serverId: Long = 0,
    val containers: List<DockerContainer> = emptyList(),
    val isDockerAvailable: Boolean = true,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class DockerListViewModel @Inject constructor(
    private val commandExecutor: SshCommandExecutor,
    private val serverRepository: ServerRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val serverId: Long = savedStateHandle["serverId"] ?: 0L
    private val _state = MutableStateFlow(DockerListState(serverId = serverId))
    val state: StateFlow<DockerListState> = _state

    init {
        viewModelScope.launch {
            val server = serverRepository.getServerById(serverId)
            _state.update { it.copy(serverName = server?.name ?: "Unknown") }
            checkDockerAndLoad()
        }
    }

    private suspend fun checkDockerAndLoad() {
        val check = commandExecutor.execute(serverId, "which docker")
        if (check.isFailure || check.getOrDefault("").isBlank()) {
            _state.update { it.copy(isDockerAvailable = false, isLoading = false) }
            return
        }
        refreshContainers()
    }

    fun refreshContainers() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = commandExecutor.execute(serverId,
                "docker ps -a --format '{{.ID}}|{{.Names}}|{{.Image}}|{{.Status}}|{{.State}}|{{.Ports}}'")
            result.fold(
                onSuccess = { output ->
                    val containers = output.lines().filter { it.isNotBlank() }.map { line ->
                        val parts = line.split("|")
                        DockerContainer(
                            id = parts.getOrElse(0) { "" },
                            name = parts.getOrElse(1) { "" },
                            image = parts.getOrElse(2) { "" },
                            status = parts.getOrElse(3) { "" },
                            state = parseState(parts.getOrElse(4) { "" }),
                            ports = parts.getOrElse(5) { "" }
                        )
                    }
                    _state.update { it.copy(containers = containers, isLoading = false) }
                },
                onFailure = { e ->
                    _state.update { it.copy(error = e.message, isLoading = false) }
                }
            )
        }
    }

    fun startContainer(containerId: String) {
        executeAction("docker start $containerId")
    }

    fun stopContainer(containerId: String) {
        executeAction("docker stop $containerId")
    }

    fun restartContainer(containerId: String) {
        executeAction("docker restart $containerId")
    }

    fun removeContainer(containerId: String) {
        executeAction("docker rm -f $containerId")
    }

    private fun executeAction(command: String) {
        viewModelScope.launch {
            commandExecutor.execute(serverId, command)
            refreshContainers()
        }
    }

    private fun parseState(state: String): ContainerState = when (state.lowercase()) {
        "running" -> ContainerState.RUNNING
        "exited" -> ContainerState.STOPPED
        "paused" -> ContainerState.PAUSED
        "restarting" -> ContainerState.RESTARTING
        "dead" -> ContainerState.DEAD
        "created" -> ContainerState.CREATED
        else -> ContainerState.UNKNOWN
    }
}
