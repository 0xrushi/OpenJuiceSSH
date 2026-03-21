package com.daremote.app.feature.connections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.daremote.app.core.domain.model.ConnectionStatus
import com.daremote.app.feature.connections.components.GroupChip
import com.daremote.app.feature.connections.components.ServerCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionListScreen(
    onNavigateToAddServer: () -> Unit,
    onNavigateToEditServer: (Long) -> Unit,
    onNavigateToTerminal: (Long) -> Unit,
    onNavigateToMonitoring: (Long) -> Unit,
    onNavigateToFileManager: (Long) -> Unit,
    onNavigateToDocker: (Long) -> Unit,
    viewModel: ConnectionListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Connections") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddServer) {
                Icon(Icons.Default.Add, contentDescription = "Add Server")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (state.groups.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        GroupChip(
                            group = com.daremote.app.core.domain.model.ServerGroup(
                                name = "All", color = 0
                            ),
                            selected = state.selectedGroupId == null,
                            onClick = { viewModel.selectGroup(null) }
                        )
                    }
                    items(state.groups) { group ->
                        GroupChip(
                            group = group,
                            selected = state.selectedGroupId == group.id,
                            onClick = { viewModel.selectGroup(group.id) }
                        )
                    }
                }
            }

            if (state.servers.isEmpty() && !state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No servers added yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.servers, key = { it.id }) { server ->
                        ServerCard(
                            server = server,
                            status = state.connectionStatuses[server.id]
                                ?: ConnectionStatus.DISCONNECTED,
                            onConnect = { viewModel.connect(server) },
                            onDisconnect = { viewModel.disconnect(server.id) },
                            onEdit = { onNavigateToEditServer(server.id) },
                            onDelete = { viewModel.deleteServer(server.id) },
                            onTerminal = { onNavigateToTerminal(server.id) },
                            onMonitoring = { onNavigateToMonitoring(server.id) },
                            onFileManager = { onNavigateToFileManager(server.id) },
                            onDocker = { onNavigateToDocker(server.id) }
                        )
                    }
                }
            }
        }
    }
}
