package com.openjuicessh.app.feature.terminal

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.text.InputType
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.compose.foundation.focusable
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.openjuicessh.app.core.domain.model.Snippet
import com.openjuicessh.app.core.terminal.TerminalInputEncoder
import com.openjuicessh.app.core.terminal.TerminalSnapshot
import com.openjuicessh.app.core.ui.theme.*
import com.openjuicessh.app.feature.settings.SettingsViewModel
import android.graphics.Paint as AndroidPaint
import android.graphics.RectF
import android.graphics.Typeface

private val PanelIndicator = Color(0xFF26C6DA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(
    onNavigateBack: () -> Unit,
    viewModel: TerminalViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val settingsState by settingsViewModel.state.collectAsStateWithLifecycle()
    val terminalTheme = terminalThemes.find { it.name == settingsState.terminalTheme } ?: terminalThemes.first()
    var showFnKeys by remember { mutableStateOf(false) }

    val currentSession = state.sessions.find { it.id == state.currentSessionId }
    val snapshot = currentSession?.snapshot

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
                    containerColor = terminalTheme.toolbar
                )
            )
        },
        containerColor = terminalTheme.background,
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .windowInsetsPadding(WindowInsets.ime.exclude(WindowInsets.navigationBars))
        ) {
            when (state.activePanel) {
                TerminalPanel.TERMINAL -> TerminalPanelContent(state, snapshot, viewModel, settingsState.terminalFontSize, terminalTheme)
                TerminalPanel.SFTP -> DualPaneSftpScreen(state, viewModel)
                TerminalPanel.SNIPPETS -> SnippetsPanel(state, viewModel)
            }
            if (state.activePanel == TerminalPanel.TERMINAL) {
                Surface(
                    color = terminalTheme.toolbar,
                    tonalElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
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
        }
    }
}

@Composable
private fun ColumnScope.TerminalPanelContent(
    state: TerminalState,
    snapshot: TerminalSnapshot?,
    viewModel: TerminalViewModel,
    fontSize: Int,
    theme: TerminalColorTheme
) {
    // Session tabs + new session button
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(theme.toolbar),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (state.sessions.isNotEmpty()) {
            ScrollableTabRow(
                selectedTabIndex = state.sessions.indexOfFirst { it.id == state.currentSessionId }
                    .coerceAtLeast(0),
                containerColor = theme.toolbar,
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

    // Paint objects are created once per composition and reused across frames
    val bgPaint = remember { AndroidPaint().apply { isAntiAlias = false; style = AndroidPaint.Style.FILL } }
    val fgPaint = remember { AndroidPaint().apply {
        isAntiAlias = true
        style = AndroidPaint.Style.FILL
        typeface = Typeface.MONOSPACE
    }}
    val cursorPaint = remember { AndroidPaint().apply {
        isAntiAlias = false
        style = AndroidPaint.Style.STROKE
        strokeWidth = 2f
        color = android.graphics.Color.argb(220, 255, 255, 255)
    }}
    val cellRect = remember { RectF() }
    val density = LocalDensity.current
    val fontSizePx = remember(fontSize) { with(density) { fontSize.sp.toPx() } }

    // Resize ghostty when the terminal area changes size (keyboard open/close).
    // Cell dimensions are font-driven so line spacing never changes.
    var terminalAreaSize by remember { mutableStateOf(IntSize.Zero) }
    val sessionId = state.currentSessionId
    LaunchedEffect(terminalAreaSize, fontSizePx) {
        if (terminalAreaSize.width <= 0 || terminalAreaSize.height <= 0) return@LaunchedEffect
        delay(80) // debounce IME animation frames
        val cellH = (fontSizePx / 0.82f).toInt().coerceAtLeast(8)
        val cellW = (cellH * 0.55f).toInt().coerceAtLeast(4)
        val newCols = (terminalAreaSize.width  / cellW).coerceAtLeast(40)
        val newRows = (terminalAreaSize.height / cellH).coerceAtLeast(5)
        viewModel.resize(sessionId, newCols, newRows, cellW, cellH)
    }

    val focusRequester = remember { FocusRequester() }
    val inputRef = remember { mutableStateOf<EditText?>(null) }
    var showContextMenu by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    // Selection state: cell indices (-1 = no selection)
    var selStartCell by remember { mutableStateOf(-1) }
    var selEndCell   by remember { mutableStateOf(-1) }
    val hasSelection = selStartCell >= 0 && selEndCell >= 0 && selStartCell != selEndCell

    fun showKeyboard() {
        inputRef.value?.let { et ->
            et.requestFocus()
            val imm = et.context.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    fun offsetToCell(offset: Offset, canvasW: Float, canvasH: Float): Int {
        val snap = snapshot ?: return -1
        val cols = snap.cols.coerceAtLeast(1)
        val rows = snap.rows.coerceAtLeast(1)
        val col = ((offset.x / canvasW) * cols).toInt().coerceIn(0, cols - 1)
        val row = ((offset.y / canvasH) * rows).toInt().coerceIn(0, rows - 1)
        return row * cols + col
    }

    // Terminal view
    @OptIn(ExperimentalFoundationApi::class)
    Box(
        modifier = Modifier
            .weight(1f)
            .background(theme.background)
            .onSizeChanged { terminalAreaSize = it }
            .focusable()
            .focusRequester(focusRequester)
            .combinedClickable(
                onClick = {
                    if (hasSelection) {
                        selStartCell = -1; selEndCell = -1
                    } else {
                        focusRequester.requestFocus(); showKeyboard()
                    }
                },
                onLongClick = { showContextMenu = true }
            )
    ) {
        if (snapshot != null) {
            // Canvas size for cell hit-testing during drag — stored in a state so pointerInput
            // (which runs outside composition) can read the latest value.
            var canvasSize by remember { mutableStateOf(Offset(1f, 1f)) }

            Canvas(modifier = Modifier
                .fillMaxSize()
                .pointerInput(snapshot) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = { offset ->
                            showContextMenu = false
                            val cell = offsetToCell(offset, canvasSize.x, canvasSize.y)
                            selStartCell = cell; selEndCell = cell
                        },
                        onDrag = { change, _ ->
                            selEndCell = offsetToCell(change.position, canvasSize.x, canvasSize.y)
                        },
                        onDragEnd = { /* selection stays visible */ },
                        onDragCancel = { selStartCell = -1; selEndCell = -1 }
                    )
                }
            ) {
                canvasSize = Offset(size.width, size.height)
                drawTerminal(snapshot, theme, bgPaint, fgPaint, cursorPaint, cellRect, fontSizePx,
                    selStart = selStartCell, selEnd = selEndCell)
            }

            // Floating copy toolbar shown while something is selected
            if (hasSelection) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 4.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = {
                        val start = minOf(selStartCell, selEndCell)
                        val end   = maxOf(selStartCell, selEndCell)
                        val text = viewModel.formatSelectionRange(state.currentSessionId, start, end)
                        if (text != null) clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(text))
                        selStartCell = -1; selEndCell = -1
                    }) { Text("Copy", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    TextButton(onClick = { selStartCell = -1; selEndCell = -1 }) {
                        Text("×", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Context menu for paste / select-all
            DropdownMenu(
                expanded = showContextMenu,
                onDismissRequest = { showContextMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Paste") },
                    onClick = {
                        showContextMenu = false
                        val text = clipboardManager.getText()?.text ?: return@DropdownMenuItem
                        viewModel.sendInput(state.currentSessionId, text.toByteArray(Charsets.UTF_8))
                    }
                )
                DropdownMenuItem(
                    text = { Text("Select All & Copy") },
                    onClick = {
                        showContextMenu = false
                        val text = viewModel.selectAll(state.currentSessionId)
                        if (text != null) {
                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(text))
                        }
                    }
                )
            }

            // Hidden 1dp EditText that owns IME focus
            AndroidView(
                factory = { ctx ->
                    EditText(ctx).apply {
                        inputRef.value = this
                        setBackgroundColor(android.graphics.Color.TRANSPARENT)
                        isFocusable = true
                        isFocusableInTouchMode = true
                        inputType = InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                            InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI or EditorInfo.IME_ACTION_NONE
                        // Fallback: some keyboards fire editor action for Enter instead of inserting \n
                        setOnEditorActionListener { _, _, _ ->
                            viewModel.sendInput(state.currentSessionId, byteArrayOf(0x0D))
                            true
                        }
                        var sentLength = 0
                        addTextChangedListener(object : android.text.TextWatcher {
                            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
                            override fun onTextChanged(s: CharSequence?, st: Int, before: Int, count: Int) {
                                val (bytes, newLen) = TerminalInputEncoder.encode(
                                    s?.toString() ?: "", sentLength
                                )
                                if (bytes.isNotEmpty()) {
                                    viewModel.sendInput(state.currentSessionId, bytes)
                                }
                                sentLength = newLen
                            }
                            override fun afterTextChanged(s: android.text.Editable?) {}
                        })
                        post {
                            requestFocus()
                            val imm = context.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
                        }
                    }
                },
                modifier = Modifier.size(1.dp, 1.dp)
            )
        } else if (state.sessions.isNotEmpty() && state.error == null) {
            // Session is open but no snapshot yet - native library not loaded
            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = TerminalGreen)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "SSH connected. Waiting for terminal renderer…",
                        color = Color.White.copy(alpha = 0.7f),
                        style = TextStyle(fontSize = 13.sp, fontFamily = FontFamily.Monospace),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "libchuchu_jni.so not loaded.\nBuild the native library with Zig to enable rendering.",
                        color = Color.White.copy(alpha = 0.4f),
                        style = TextStyle(fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
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

private fun DrawScope.drawTerminal(
    snapshot: TerminalSnapshot,
    theme: TerminalColorTheme,
    bgPaint: AndroidPaint,
    fgPaint: AndroidPaint,
    cursorPaint: AndroidPaint,
    cellRect: RectF,
    fontSizePx: Float,
    selStart: Int = -1,
    selEnd: Int = -1,
) {
    val cols = snapshot.cols.coerceAtLeast(1)
    val rows = snapshot.rows.coerceAtLeast(1)

    // Cell height is font-driven so line spacing never changes when the keyboard
    // opens/closes. cellWidth fills available width evenly across columns.
    fgPaint.textSize = fontSizePx
    val metrics = fgPaint.fontMetrics
    val cellHeight = (-metrics.ascent + metrics.descent).coerceAtLeast(1f)
    val cellWidth  = size.width / cols

    // Fill the full background first
    drawRect(color = theme.background, size = size)

    val baselineOffset = -metrics.ascent

    val defaultBg = snapshot.defaultBgArgb

    val selLo = if (selStart >= 0 && selEnd >= 0) minOf(selStart, selEnd) else -1
    val selHi = if (selStart >= 0 && selEnd >= 0) maxOf(selStart, selEnd) else -1

    val nativeCanvas = drawContext.canvas.nativeCanvas

    for (row in 0 until rows) {
        val cellTop = row * cellHeight
        val cellBottom = cellTop + cellHeight

        for (col in 0 until cols) {
            val i = row * cols + col
            val cellLeft = col * cellWidth
            val cellRight = cellLeft + cellWidth
            val flag = snapshot.flags[i].toInt() and 0xFF

            // Skip spacer cells (right half of wide chars)
            if (flag and TerminalSnapshot.CELL_FLAG_SPACER != 0) continue

            // Draw selection highlight (overrides cell background)
            val inSelection = selLo >= 0 && i in selLo..selHi
            if (inSelection) {
                bgPaint.color = android.graphics.Color.argb(160, 100, 160, 255)
                cellRect.set(cellLeft, cellTop, cellRight, cellBottom)
                nativeCanvas.drawRect(cellRect, bgPaint)
            } else {
                // Draw cell background when different from terminal default
                val bg = snapshot.bgArgb[i]
                if (bg != defaultBg) {
                    bgPaint.color = bg
                    cellRect.set(cellLeft, cellTop, cellRight, cellBottom)
                    nativeCanvas.drawRect(cellRect, bgPaint)
                }
            }

            // codepoints[i] is always the base codepoint; graphemeExtras[i] holds
            // additional combining marks which we skip for now
            val cp = snapshot.codepoints[i]

            // Skip non-printable / space
            if (cp <= 0x20) continue

            // Check whether this is a wide char (next cell is a spacer)
            val isWide = col + 1 < cols &&
                (snapshot.flags[i + 1].toInt() and 0xFF) and TerminalSnapshot.CELL_FLAG_SPACER != 0

            fgPaint.color = snapshot.fgArgb[i]
            val charStr = if (cp <= 0xFFFF) cp.toChar().toString() else String(Character.toChars(cp))
            val drawX = if (isWide) {
                val wideWidth = cellWidth * 2
                val charWidth = fgPaint.measureText(charStr)
                cellLeft + (wideWidth - charWidth) / 2f
            } else {
                cellLeft
            }
            nativeCanvas.drawText(charStr, drawX, cellTop + baselineOffset, fgPaint)
        }
    }

    // Draw cursor (block outline)
    if (snapshot.cursorVisible &&
        snapshot.cursorX in 0 until cols &&
        snapshot.cursorY in 0 until rows
    ) {
        val cx = snapshot.cursorX * cellWidth
        val cy = snapshot.cursorY * cellHeight
        cellRect.set(cx + 1f, cy + 1f, cx + cellWidth - 1f, cy + cellHeight - 1f)
        nativeCanvas.drawRect(cellRect, cursorPaint)
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
