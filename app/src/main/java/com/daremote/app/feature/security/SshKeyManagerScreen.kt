package com.daremote.app.feature.security

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.daremote.app.core.domain.model.KeyType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SshKeyManagerScreen(
    onNavigateBack: () -> Unit,
    viewModel: SshKeyManagerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showGenerateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SSH Keys") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showGenerateDialog = true }) {
                Icon(Icons.Default.Add, "Generate Key")
            }
        }
    ) { padding ->
        if (state.keys.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Key, null,
                        modifier = Modifier.padding(bottom = 8.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("No SSH keys", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.keys) { key ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(key.name, style = MaterialTheme.typography.titleMedium)
                                Text("Type: ${key.type.name}", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    key.publicKey.take(50) + "...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { viewModel.deleteKey(key) }) {
                                Icon(Icons.Default.Delete, "Delete")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showGenerateDialog) {
        var keyName by remember { mutableStateOf("") }
        var keyType by remember { mutableStateOf(KeyType.ED25519) }

        AlertDialog(
            onDismissRequest = { showGenerateDialog = false },
            title = { Text("Generate SSH Key") },
            text = {
                Column {
                    OutlinedTextField(
                        value = keyName, onValueChange = { keyName = it },
                        label = { Text("Key Name") }, modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        KeyType.entries.forEachIndexed { index, type ->
                            SegmentedButton(
                                selected = keyType == type,
                                onClick = { keyType = type },
                                shape = SegmentedButtonDefaults.itemShape(index, KeyType.entries.size)
                            ) { Text(type.name) }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (keyName.isNotBlank()) {
                            viewModel.generateKey(keyName, keyType)
                            showGenerateDialog = false
                        }
                    },
                    enabled = !state.isGenerating
                ) { Text("Generate") }
            },
            dismissButton = {
                TextButton(onClick = { showGenerateDialog = false }) { Text("Cancel") }
            }
        )
    }
}
