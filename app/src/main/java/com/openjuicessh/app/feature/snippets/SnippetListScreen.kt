package com.openjuicessh.app.feature.snippets

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.openjuicessh.app.core.domain.model.Snippet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnippetListScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    viewModel: SnippetListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var serverPickerSnippet by remember { mutableStateOf<Snippet?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Command Snippets") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                windowInsets = WindowInsets(0)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd) {
                Icon(Icons.Default.Add, "Add Snippet")
            }
        }
    ) { padding ->
        if (state.snippets.isEmpty() && !state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No snippets yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.snippets, key = { it.id }) { snippet ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(snippet.name, style = MaterialTheme.typography.titleMedium)
                                    snippet.description?.let {
                                        Text(it, style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                IconButton(onClick = { serverPickerSnippet = snippet }) {
                                    Icon(Icons.Default.PlayArrow, "Execute",
                                        tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { onNavigateToEdit(snippet.id) }) {
                                    Icon(Icons.Default.Edit, "Edit")
                                }
                                IconButton(onClick = { viewModel.deleteSnippet(snippet) }) {
                                    Icon(Icons.Default.Delete, "Delete")
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = snippet.command,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (state.executingSnippetId == snippet.id && state.executionResult != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Output:", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = state.executionResult!!,
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                    maxLines = 10
                                )
                            }
                        }
                    }
                }
            }
        }

        // Server picker dropdown
        serverPickerSnippet?.let { snippet ->
            var expanded by remember { mutableStateOf(true) }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false; serverPickerSnippet = null }) {
                state.servers.forEach { server ->
                    DropdownMenuItem(
                        text = { Text("${server.name} (${server.host})") },
                        onClick = {
                            expanded = false
                            serverPickerSnippet = null
                            viewModel.executeSnippet(snippet, server.id)
                        }
                    )
                }
                if (state.servers.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No connected servers") },
                        onClick = { expanded = false; serverPickerSnippet = null }
                    )
                }
            }
        }
    }
}
