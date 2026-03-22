package com.openjuicessh.app.feature.forwarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.openjuicessh.app.core.domain.model.ForwardingType
import com.openjuicessh.app.feature.connections.ProxyDropdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditForwardingScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEditForwardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.saved) { if (state.saved) onNavigateBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "Edit Rule" else "Add Forwarding Rule") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = state.name, onValueChange = viewModel::updateName,
                label = { Text("Rule Name") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text("Forwarding Type", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                ForwardingType.entries.forEachIndexed { index, type ->
                    SegmentedButton(
                        selected = state.type == type,
                        onClick = { viewModel.updateType(type) },
                        shape = SegmentedButtonDefaults.itemShape(index, ForwardingType.entries.size)
                    ) {
                        Text(when (type) {
                            ForwardingType.LOCAL -> "Local"
                            ForwardingType.REMOTE -> "Remote"
                            ForwardingType.DYNAMIC -> "Dynamic"
                        })
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Server picker
            var serverExpanded by remember { mutableStateOf(false) }
            val selectedServer = state.servers.find { it.id == state.selectedServerId }
            ExposedDropdownMenuBox(
                expanded = serverExpanded,
                onExpandedChange = { serverExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedServer?.name ?: "Select server",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("SSH Server") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = serverExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = serverExpanded,
                    onDismissRequest = { serverExpanded = false }
                ) {
                    state.servers.forEach { server ->
                        DropdownMenuItem(
                            text = { Text("${server.name} (${server.host})") },
                            onClick = {
                                viewModel.updateServer(server.id)
                                serverExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Proxy (optional)
            if (state.availableProxies.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                ProxyDropdown(
                    selectedProxyId = state.proxyId,
                    proxies = state.availableProxies,
                    onProxySelected = viewModel::updateProxyId,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Bind address + local port
            OutlinedTextField(
                value = state.localHost, onValueChange = viewModel::updateLocalHost,
                label = { Text("Bind Address") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = state.localPort, onValueChange = viewModel::updateLocalPort,
                label = { Text(if (state.type == ForwardingType.REMOTE) "Public Port" else "Local Port") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (state.type != ForwardingType.DYNAMIC) {
                OutlinedTextField(
                    value = state.remoteHost, onValueChange = viewModel::updateRemoteHost,
                    label = { Text("Destination Host") }, modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.remotePort, onValueChange = viewModel::updateRemotePort,
                    label = { Text("Destination Port") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Auto-connect on app start", modifier = Modifier.weight(1f))
                Switch(checked = state.autoConnect, onCheckedChange = viewModel::updateAutoConnect)
            }
            Spacer(modifier = Modifier.height(16.dp))

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = viewModel::save, enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.isSaving) "Saving..." else "Save")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
