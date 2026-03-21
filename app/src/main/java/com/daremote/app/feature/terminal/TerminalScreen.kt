package com.daremote.app.feature.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.daremote.app.core.domain.model.RemoteFile
import com.daremote.app.core.domain.model.Snippet
import com.daremote.app.core.ui.theme.*
import com.termux.view.TerminalView

private val PanelIndicator = Color(0xFF26C6DA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(
    onNavigateBack: () -> Unit,
    viewModel: TerminalViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showFnKeys by remember { mutableStateOf(false) }

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
                    PanelTabButton(
                        symbol = ">_",
                        selected = state.activePanel == TerminalPanel.TERMINAL,
                        onClick = { viewModel.setActivePanel(TerminalPanel.TERMINAL) }
                    )
                    PanelTabButton(
                        symbol = "±",
                        selected = state.activePanel == TerminalPanel.SFTP,
                        onClick = { viewModel.setActivePanel(TerminalPanel.SFTP) }
                    )
                    PanelTabButton(
                        symbol = "</>",
                        selected = state.activePanel == TerminalPanel.SNIPPETS,
                        onClick = { viewModel.setActivePanel(TerminalPanel.SNIPPETS) }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TerminalBackground
                )
            )
        },
        bottomBar = {
            if (state.activePanel == TerminalPanel.TERMINAL) {
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
                            TerminalKey("CTRL") { }
                            TerminalKey("ALT") { }
                            TerminalKey("←") { viewModel.sendInput(state.currentSessionId, "\u001b[D".toByteArray()) }
                            TerminalKey("↓") { viewModel.sendInput(state.currentSessionId, "\u001b[B".toByteArray()) }
                            TerminalKey("→") { viewModel.sendInput(state.currentSessionId, "\u001b[C".toByteArray()) }
                            TerminalKey("PGDN") { viewModel.sendInput(state.currentSessionId, "\u001b[6~".toByteArray()) }
                        }
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
            when (state.activePanel) {
                TerminalPanel.TERMINAL -> TerminalPanelContent(state, terminalSession, viewModel)
                TerminalPanel.SFTP -> SftpPanel(state, viewModel)
                TerminalPanel.SNIPPETS -> SnippetsPanel(state, viewModel)
            }
        }
    }
}

@Composable
private fun ColumnScope.TerminalPanelContent(
    state: TerminalState,
    terminalSession: com.termux.terminal.TerminalSession?,
    viewModel: TerminalViewModel
) {
    // Session tabs + new session button
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TerminalToolbarBackground),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (state.sessions.isNotEmpty()) {
            ScrollableTabRow(
                selectedTabIndex = state.sessions.indexOfFirst { it.id == state.currentSessionId }
                    .coerceAtLeast(0),
                containerColor = TerminalToolbarBackground,
                contentColor = Color.White,
                edgePadding = 0.dp,
                modifier = Modifier.weight(1f)
            ) {
                state.sessions.forEach { session ->
                    Tab(
                        selected = session.id == state.currentSessionId,
                        onClick = { viewModel.switchSession(session.id) },
                        text = { Text(session.name, style = TextStyle(fontSize = 12.sp)) }
                    )
                }
            }
        } else {
            Spacer(Modifier.weight(1f))
        }
        IconButton(onClick = { viewModel.openNewSession() }) {
            Icon(Icons.Default.Add, "New Session", tint = Color.White)
        }
    }

    // Terminal view
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

@Composable
private fun ColumnScope.SftpPanel(state: TerminalState, viewModel: TerminalViewModel) {
    // Path breadcrumb row
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TerminalToolbarBackground)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (state.sftpPath != "/") {
            IconButton(
                onClick = { viewModel.sftpNavigateUp() },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Up", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        } else {
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = state.sftpPath,
            color = PanelIndicator,
            style = TextStyle(fontSize = 12.sp, fontFamily = FontFamily.Monospace),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }

    if (state.sftpLoading) {
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PanelIndicator)
        }
    } else if (state.sftpFiles.isEmpty()) {
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("Empty directory", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp)
        }
    } else {
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            items(state.sftpFiles) { file ->
                SftpFileRow(file = file, onNavigate = { viewModel.sftpNavigateTo(file.path) })
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
            }
        }
    }
}

@Composable
private fun SftpFileRow(file: RemoteFile, onNavigate: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (file.isDirectory) Modifier.clickable(onClick = onNavigate) else Modifier)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (file.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
            contentDescription = null,
            tint = if (file.isDirectory) PanelIndicator else Color.White.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = file.name,
                color = Color.White,
                style = TextStyle(fontSize = 13.sp)
            )
            if (!file.isDirectory) {
                Text(
                    text = formatFileSize(file.size),
                    color = Color.White.copy(alpha = 0.4f),
                    style = TextStyle(fontSize = 11.sp)
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.SnippetsPanel(state: TerminalState, viewModel: TerminalViewModel) {
    if (state.snippets.isEmpty()) {
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text("No snippets saved", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp)
        }
    } else {
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            items(state.snippets) { snippet ->
                SnippetRow(snippet = snippet, onRun = { viewModel.executeSnippet(snippet) })
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
            }
        }
    }
}

@Composable
private fun SnippetRow(snippet: Snippet, onRun: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = snippet.name,
                color = Color.White,
                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium)
            )
            Text(
                text = snippet.command,
                color = PanelIndicator.copy(alpha = 0.8f),
                style = TextStyle(fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            snippet.description?.takeIf { it.isNotBlank() }?.let { desc ->
                Text(
                    text = desc,
                    color = Color.White.copy(alpha = 0.4f),
                    style = TextStyle(fontSize = 11.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        IconButton(onClick = onRun) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Run snippet",
                tint = PanelIndicator
            )
        }
    }
}

@Composable
private fun PanelTabButton(
    symbol: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = symbol,
            color = if (selected) Color.White else Color.White.copy(alpha = 0.45f),
            style = TextStyle(
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .width(22.dp)
                .height(2.dp)
                .background(if (selected) PanelIndicator else Color.Transparent)
        )
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

private fun formatFileSize(bytes: Long): String = when {
    bytes >= 1_073_741_824L -> "%.1f GB".format(bytes / 1_073_741_824.0)
    bytes >= 1_048_576L -> "%.1f MB".format(bytes / 1_048_576.0)
    bytes >= 1_024L -> "%.1f KB".format(bytes / 1_024.0)
    else -> "$bytes B"
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
