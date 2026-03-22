package com.openjuicessh.app.feature.docker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.openjuicessh.app.core.domain.model.ContainerState
import com.openjuicessh.app.core.ui.theme.StatusConnected
import com.openjuicessh.app.core.ui.theme.StatusDisconnected
import com.openjuicessh.app.core.ui.theme.StatusError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DockerListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToContainer: (Long, String) -> Unit,
    viewModel: DockerListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Docker - ${state.serverName}") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshContainers() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        if (!state.isDockerAvailable) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Docker is not installed on this server",
                    style = MaterialTheme.typography.bodyLarge)
            }
        } else if (state.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.containers, key = { it.id }) { container ->
                    Card(
                        onClick = { onNavigateToContainer(state.serverId, container.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(container.name, style = MaterialTheme.typography.titleMedium)
                                    Text(container.image, style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                AssistChip(
                                    onClick = { },
                                    label = {
                                        Text(container.state.name, style = MaterialTheme.typography.labelSmall)
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(container.status, style = MaterialTheme.typography.bodySmall)
                            if (container.ports.isNotBlank()) {
                                Text(container.ports, style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row {
                                if (container.state != ContainerState.RUNNING) {
                                    IconButton(onClick = { viewModel.startContainer(container.id) }) {
                                        Icon(Icons.Default.PlayArrow, "Start", tint = StatusConnected)
                                    }
                                } else {
                                    IconButton(onClick = { viewModel.stopContainer(container.id) }) {
                                        Icon(Icons.Default.Stop, "Stop", tint = StatusError)
                                    }
                                }
                                IconButton(onClick = { viewModel.restartContainer(container.id) }) {
                                    Icon(Icons.Default.RestartAlt, "Restart")
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(onClick = { viewModel.removeContainer(container.id) }) {
                                    Icon(Icons.Default.Delete, "Remove", tint = StatusError)
                                }
                            }
                        }
                    }
                }

                state.error?.let { error ->
                    item {
                        Text(error, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
