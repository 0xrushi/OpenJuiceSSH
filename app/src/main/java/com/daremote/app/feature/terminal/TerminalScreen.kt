package com.daremote.app.feature.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.daremote.app.core.ui.theme.TerminalBackground
import com.daremote.app.core.ui.theme.TerminalGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(
    onNavigateBack: () -> Unit,
    viewModel: TerminalViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var inputText by remember { mutableStateOf("") }

    val currentSession = state.sessions.find { it.id == state.currentSessionId }

    LaunchedEffect(currentSession?.outputLines?.size) {
        currentSession?.outputLines?.size?.let { size ->
            if (size > 0) listState.animateScrollToItem(size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.serverName) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.openNewSession() }) {
                        Icon(Icons.Default.Add, "New Session")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Session tabs
            if (state.sessions.size > 1) {
                ScrollableTabRow(
                    selectedTabIndex = state.sessions.indexOfFirst { it.id == state.currentSessionId }
                        .coerceAtLeast(0)
                ) {
                    state.sessions.forEach { session ->
                        Tab(
                            selected = session.id == state.currentSessionId,
                            onClick = { viewModel.switchSession(session.id) },
                            text = { Text(session.name) }
                        )
                    }
                }
            }

            // Terminal output
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(TerminalBackground)
                    .padding(8.dp)
            ) {
                items(currentSession?.outputLines ?: emptyList()) { line ->
                    Text(
                        text = line,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = TerminalGreen
                        )
                    )
                }
            }

            // Input bar
            Row(modifier = Modifier.fillMaxWidth()) {
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Enter command...") },
                    textStyle = TextStyle(fontFamily = FontFamily.Monospace),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = TerminalBackground,
                        unfocusedContainerColor = TerminalBackground,
                        focusedTextColor = TerminalGreen,
                        unfocusedTextColor = TerminalGreen
                    )
                )
                IconButton(onClick = {
                    if (inputText.isNotEmpty()) {
                        viewModel.sendInput(inputText + "\n")
                        inputText = ""
                    }
                }) {
                    Text(">>", color = TerminalGreen)
                }
            }

            // Special keys toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp)
            ) {
                listOf("Tab" to "\t", "Esc" to "\u001b", "Ctrl+C" to "\u0003",
                    "Ctrl+D" to "\u0004", "Up" to "\u001b[A", "Down" to "\u001b[B"
                ).forEach { (label, code) ->
                    IconButton(
                        onClick = { viewModel.sendInput(code) },
                        modifier = Modifier.padding(horizontal = 2.dp)
                    ) {
                        Text(label, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
