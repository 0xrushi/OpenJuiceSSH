package com.daremote.app.feature.terminal

import android.graphics.Typeface
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.daremote.app.core.ui.theme.*
import com.termux.view.TerminalView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(
    onNavigateBack: () -> Unit,
    viewModel: TerminalViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showFnKeys by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val currentSession = state.sessions.find { it.id == state.currentSessionId }
    val terminalSession = currentSession?.terminalSession

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.serverName, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.openNewSession() }) {
                        Icon(Icons.Default.Add, "New Session", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TerminalBackground
                )
            )
        },
        bottomBar = {
            Surface(
                color = TerminalToolbarBackground,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars))
                        .padding(vertical = 2.dp)
                ) {
                    if (showFnKeys) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            (1..6).forEach { i ->
                                TerminalKey("F$i") { viewModel.sendInput(state.currentSessionId, getFnCode(i).toByteArray()) }
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            (7..12).forEach { i ->
                                TerminalKey("F$i") { viewModel.sendInput(state.currentSessionId, getFnCode(i).toByteArray()) }
                            }
                        }
                    }

                    // Row 1: ESC / | - HOME ↑ END PGUP FN
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TerminalKey("ESC") { viewModel.sendInput(state.currentSessionId, "\u001b".toByteArray()) }
                        TerminalKey("/") { viewModel.sendInput(state.currentSessionId, "/".toByteArray()) }
                        TerminalKey("|") { viewModel.sendInput(state.currentSessionId, "|".toByteArray()) }
                        TerminalKey("-") { viewModel.sendInput(state.currentSessionId, "-".toByteArray()) }
                        TerminalKey("HOME") { viewModel.sendInput(state.currentSessionId, "\u001b[1~".toByteArray()) }
                        TerminalKey("↑") { viewModel.sendInput(state.currentSessionId, "\u001b[A".toByteArray()) }
                        TerminalKey("END") { viewModel.sendInput(state.currentSessionId, "\u001b[4~".toByteArray()) }
                        TerminalKey("PGUP") { viewModel.sendInput(state.currentSessionId, "\u001b[5~".toByteArray()) }
                        TerminalKey("FN") { showFnKeys = !showFnKeys }
                    }

                    // Row 2: TAB CTRL ALT ← ↓ → PGDN
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TerminalKey("TAB") { viewModel.sendInput(state.currentSessionId, "\t".toByteArray()) }
                        TerminalKey("CTRL") { /* To be implemented with terminal session state */ }
                        TerminalKey("ALT") { /* To be implemented with terminal session state */ }
                        TerminalKey("←") { viewModel.sendInput(state.currentSessionId, "\u001b[D".toByteArray()) }
                        TerminalKey("↓") { viewModel.sendInput(state.currentSessionId, "\u001b[B".toByteArray()) }
                        TerminalKey("→") { viewModel.sendInput(state.currentSessionId, "\u001b[C".toByteArray()) }
                        TerminalKey("PGDN") { viewModel.sendInput(state.currentSessionId, "\u001b[6~".toByteArray()) }
                    }
                }
            }
        },
        containerColor = TerminalBackground
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
                        .coerceAtLeast(0),
                    containerColor = TerminalToolbarBackground,
                    contentColor = Color.White,
                    edgePadding = 0.dp
                ) {
                    state.sessions.forEach { session ->
                        Tab(
                            selected = session.id == state.currentSessionId,
                            onClick = { viewModel.switchSession(session.id) },
                            text = { Text(session.name, style = TextStyle(fontSize = 12.sp)) }
                        )
                    }
                }
            }

            // Real Terminal View
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(TerminalBackground)
            ) {
                if (terminalSession != null) {
                    AndroidView(
                        factory = { ctx ->
                            TerminalView(ctx, null).apply {
                                setTerminalViewClient(viewModel)
                                setTextSize(14)
                                attachSession(terminalSession)
                                viewModel.setTerminalView(this)
                                isFocusable = true
                                isFocusableInTouchMode = true
                                requestFocus()
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                        update = { view ->
                            view.setTerminalViewClient(viewModel)
                            viewModel.setTerminalView(view)
                            if (view.mTermSession != terminalSession) {
                                view.attachSession(terminalSession)
                            }
                        }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = TerminalGreen)
                    }
                }
                
                // Overlay error if present
                state.error?.let { error ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.7f))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error: $error",
                            color = Color.Red,
                            style = TextStyle(fontSize = 14.sp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.TerminalKey(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(36.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = TerminalButtonText,
            style = TextStyle(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            )
        )
    }
}

fun getFnCode(n: Int): String = when (n) {
    1 -> "\u001bOP"
    2 -> "\u001bOQ"
    3 -> "\u001bOR"
    4 -> "\u001bOS"
    5 -> "\u001b[15~"
    6 -> "\u001b[17~"
    7 -> "\u001b[18~"
    8 -> "\u001b[19~"
    9 -> "\u001b[20~"
    10 -> "\u001b[21~"
    11 -> "\u001b[23~"
    12 -> "\u001b[24~"
    else -> ""
}
