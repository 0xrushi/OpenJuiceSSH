package com.daremote.app.feature.connections.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.daremote.app.core.domain.model.ConnectionStatus
import com.daremote.app.core.domain.model.Server
import com.daremote.app.core.ui.theme.StatusConnected
import com.daremote.app.core.ui.theme.StatusConnecting
import com.daremote.app.core.ui.theme.StatusDisconnected
import com.daremote.app.core.ui.theme.StatusError

@Composable
fun ServerCard(
    server: Server,
    status: ConnectionStatus,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTerminal: () -> Unit,
    onMonitoring: () -> Unit,
    onFileManager: () -> Unit,
    onDocker: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        onClick = {
            when (status) {
                ConnectionStatus.CONNECTED -> showMenu = true
                ConnectionStatus.DISCONNECTED, ConnectionStatus.ERROR -> onConnect()
                ConnectionStatus.CONNECTING -> { }
            }
        },
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Circle,
                contentDescription = status.name,
                tint = when (status) {
                    ConnectionStatus.CONNECTED -> StatusConnected
                    ConnectionStatus.CONNECTING -> StatusConnecting
                    ConnectionStatus.DISCONNECTED -> StatusDisconnected
                    ConnectionStatus.ERROR -> StatusError
                },
                modifier = Modifier.size(12.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = server.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${server.username}@${server.host}:${server.port}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (status == ConnectionStatus.CONNECTED) {
                IconButton(onClick = onTerminal) {
                    Icon(Icons.Default.Terminal, contentDescription = "Terminal")
                }
            }

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }

            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(
                    text = { Text("Terminal") },
                    onClick = { showMenu = false; onTerminal() },
                    leadingIcon = { Icon(Icons.Default.Terminal, null) }
                )
                DropdownMenuItem(
                    text = { Text("Monitoring") },
                    onClick = { showMenu = false; onMonitoring() },
                    leadingIcon = { Icon(Icons.Default.Monitor, null) }
                )
                DropdownMenuItem(
                    text = { Text("File Manager") },
                    onClick = { showMenu = false; onFileManager() },
                    leadingIcon = { Icon(Icons.Default.Folder, null) }
                )
                DropdownMenuItem(
                    text = { Text("Docker") },
                    onClick = { showMenu = false; onDocker() },
                    leadingIcon = { Icon(Icons.Default.Monitor, null) }
                )
                DropdownMenuItem(
                    text = { Text("Disconnect") },
                    onClick = { showMenu = false; onDisconnect() }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = { showMenu = false; onDelete() },
                    leadingIcon = { Icon(Icons.Default.Delete, null) }
                )
            }
        }
    }
}
