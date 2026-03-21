package com.daremote.app.feature.connections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daremote.app.core.domain.model.ConnectionStatus
import com.daremote.app.core.domain.model.Server
import com.daremote.app.core.domain.model.ServerGroup
import com.daremote.app.core.domain.repository.ServerRepository
import com.daremote.app.core.domain.repository.SshConnectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConnectionListState(
    val servers: List<Server> = emptyList(),
    val groups: List<ServerGroup> = emptyList(),
    val connectionStatuses: Map<Long, ConnectionStatus> = emptyMap(),
    val selectedGroupId: Long? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ConnectionListViewModel @Inject constructor(
    private val serverRepository: ServerRepository,
    private val sshConnectionRepository: SshConnectionRepository
) : ViewModel() {

    private val _selectedGroupId = MutableStateFlow<Long?>(null)

    val state: StateFlow<ConnectionListState> = combine(
        serverRepository.getAllServers(),
        serverRepository.getAllGroups(),
        _selectedGroupId,
        sshConnectionRepository.getConnectionStatuses()
    ) { servers, groups, selectedGroup, statuses ->
        val filteredServers = if (selectedGroup != null) {
            servers.filter { it.groupId == selectedGroup }
        } else servers

        ConnectionListState(
            servers = filteredServers,
            groups = groups,
            connectionStatuses = statuses,
            selectedGroupId = selectedGroup,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ConnectionListState())

    fun selectGroup(groupId: Long?) {
        _selectedGroupId.value = groupId
    }

    fun connect(server: Server) {
        viewModelScope.launch {
            sshConnectionRepository.connect(server)
            serverRepository.updateLastConnected(server.id)
        }
    }

    fun disconnect(serverId: Long) {
        viewModelScope.launch {
            sshConnectionRepository.disconnect(serverId)
        }
    }

    fun deleteServer(serverId: Long) {
        viewModelScope.launch {
            sshConnectionRepository.disconnect(serverId)
            serverRepository.deleteServer(serverId)
        }
    }
}
