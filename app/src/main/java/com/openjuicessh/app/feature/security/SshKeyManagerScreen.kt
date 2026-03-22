package com.openjuicessh.app.feature.security

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.openjuicessh.app.core.domain.model.KeyType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SshKeyManagerScreen(
    onNavigateBack: () -> Unit,
    viewModel: SshKeyManagerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, "Add Key")
            }
        }
    ) { padding ->
        if (state.keys.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Key, null,
                        modifier = Modifier.padding(bottom = 8.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("No SSH keys", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "Tap + to generate or import a key",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                                if (key.publicKey.isNotBlank()) {
                                    Text(
                                        key.publicKey.take(50) + "...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (key.publicKey.isNotBlank()) {
                                IconButton(onClick = {
                                    clipboardManager.setText(AnnotatedString(key.publicKey))
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Public key copied – paste it into ~/.ssh/authorized_keys on your server")
                                    }
                                }) {
                                    Icon(Icons.Default.ContentCopy, "Copy public key")
                                }
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

    if (showDialog) {
        AddKeyDialog(
            isLoading = state.isGenerating,
            onGenerate = { name, type ->
                viewModel.generateKey(name, type)
                showDialog = false
            },
            onImport = { name, content ->
                viewModel.importKey(name, content)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
private fun AddKeyDialog(
    isLoading: Boolean,
    onGenerate: (String, KeyType) -> Unit,
    onImport: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var keyName by remember { mutableStateOf("") }
    var keyType by remember { mutableStateOf(KeyType.ED25519) }
    var privateKeyContent by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(if (selectedTab == 0) "Generate SSH Key" else "Import SSH Key")
                Spacer(Modifier.height(8.dp))
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Generate") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Import") })
                }
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = keyName,
                    onValueChange = { keyName = it },
                    label = { Text("Key Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))

                if (selectedTab == 0) {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        KeyType.entries.forEachIndexed { index, type ->
                            SegmentedButton(
                                selected = keyType == type,
                                onClick = { keyType = type },
                                shape = SegmentedButtonDefaults.itemShape(index, KeyType.entries.size)
                            ) { Text(type.name) }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = privateKeyContent,
                        onValueChange = { privateKeyContent = it },
                        label = { Text("Private Key (PEM)") },
                        placeholder = { Text("-----BEGIN ... PRIVATE KEY-----\n...") },
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        maxLines = 10
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (keyName.isBlank()) return@TextButton
                    if (selectedTab == 0) {
                        onGenerate(keyName, keyType)
                    } else {
                        if (privateKeyContent.isNotBlank()) onImport(keyName, privateKeyContent)
                    }
                },
                enabled = !isLoading
            ) { Text(if (selectedTab == 0) "Generate" else "Import") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
