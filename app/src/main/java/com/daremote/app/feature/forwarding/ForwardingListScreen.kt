package com.daremote.app.feature.forwarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.daremote.app.core.domain.model.ForwardingType
import com.daremote.app.core.domain.model.TunnelState
import com.daremote.app.core.ui.theme.StatusConnected
import com.daremote.app.core.ui.theme.StatusConnecting
import com.daremote.app.core.ui.theme.StatusDisconnected
import com.daremote.app.core.ui.theme.StatusError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForwardingListScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    viewModel: ForwardingListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.snackbarShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Port Forwarding") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                windowInsets = WindowInsets(0)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd) {
                Icon(Icons.Default.Add, "Add Rule")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (state.rules.isEmpty() && !state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No forwarding rules yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.rules, key = { it.id }) { rule ->
                    val tunnelState = state.tunnelStates[rule.id] ?: TunnelState.STOPPED

                    Card(
                        onClick = { onNavigateToEdit(rule.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Circle,
                                contentDescription = tunnelState.name,
                                modifier = Modifier.size(12.dp),
                                tint = when (tunnelState) {
                                    TunnelState.ACTIVE -> StatusConnected
                                    TunnelState.CONNECTING -> StatusConnecting
                                    TunnelState.ERROR -> StatusError
                                    TunnelState.STOPPED -> StatusDisconnected
                                }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(rule.name, style = MaterialTheme.typography.titleMedium)
                                val typeLabel = when (rule.type) {
                                    ForwardingType.LOCAL -> "Local"
                                    ForwardingType.REMOTE -> "Remote"
                                    ForwardingType.DYNAMIC -> "Dynamic (SOCKS)"
                                }
                                Text(typeLabel, style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(4.dp))
                                val desc = when (rule.type) {
                                    ForwardingType.LOCAL ->
                                        "${rule.localHost}:${rule.localPort} -> ${rule.remoteHost}:${rule.remotePort}"
                                    ForwardingType.REMOTE ->
                                        "remote:${rule.localPort} -> ${rule.remoteHost}:${rule.remotePort}"
                                    ForwardingType.DYNAMIC ->
                                        "SOCKS on ${rule.localHost}:${rule.localPort}"
                                }
                                Text(desc, style = MaterialTheme.typography.bodySmall)
                            }
                            Switch(
                                checked = tunnelState == TunnelState.ACTIVE || tunnelState == TunnelState.CONNECTING,
                                onCheckedChange = { viewModel.toggleTunnel(rule) }
                            )
                            IconButton(onClick = { viewModel.deleteRule(rule) }) {
                                Icon(Icons.Default.Delete, "Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}
