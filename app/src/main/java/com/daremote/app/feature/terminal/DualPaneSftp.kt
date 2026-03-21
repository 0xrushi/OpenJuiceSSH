package com.daremote.app.feature.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daremote.app.core.ui.theme.TerminalBackground
import com.daremote.app.core.ui.theme.TerminalToolbarBackground

private val Teal = Color(0xFF26C6DA)
private val DimWhite = Color.White.copy(alpha = 0.5f)
private val SubtleDiv = Color.White.copy(alpha = 0.07f)
private val ActiveBorder = Teal
private val SelectedBg = Color(0xFF1A3A40)

@Composable
fun ColumnScope.DualPaneSftpScreen(state: TerminalState, viewModel: TerminalViewModel) {
    val dp = state.dualPane

    // Operation error banner
    dp.operationError?.let { err ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF5C1A1A))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(err, color = Color(0xFFFF8A80), fontSize = 11.sp, modifier = Modifier.weight(1f))
            IconButton(onClick = { viewModel.dismissOperationError() }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Close, null, tint = Color(0xFFFF8A80), modifier = Modifier.size(16.dp))
            }
        }
    }

    // Dual pane row — wrapped in Box so the progress overlay floats above it
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
    ) {
    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        PaneColumn(
            pane = dp.leftPane,
            isActive = dp.activePaneIsLeft,
            modifier = Modifier.weight(1f),
            onActivate = { viewModel.setActivePane(true) },
            onNavigateTo = { viewModel.navigateTo(true, it) },
            onNavigateUp = { viewModel.navigateUp(true) },
            onToggleSelect = { viewModel.toggleSelection(true, it) },
            onSelectAll = { viewModel.selectAll(true) },
            onClearSelection = { viewModel.clearSelection(true) },
            onToggleLocal = { viewModel.togglePaneIsLocal(true) }
        )

        VerticalDivider(
            modifier = Modifier.fillMaxHeight(),
            color = Color.White.copy(alpha = 0.12f),
            thickness = 1.dp
        )

        PaneColumn(
            pane = dp.rightPane,
            isActive = !dp.activePaneIsLeft,
            modifier = Modifier.weight(1f),
            onActivate = { viewModel.setActivePane(false) },
            onNavigateTo = { viewModel.navigateTo(false, it) },
            onNavigateUp = { viewModel.navigateUp(false) },
            onToggleSelect = { viewModel.toggleSelection(false, it) },
            onSelectAll = { viewModel.selectAll(false) },
            onClearSelection = { viewModel.clearSelection(false) },
            onToggleLocal = { viewModel.togglePaneIsLocal(false) }
        )
    } // end Row

    // Floating transfer progress overlay
    if (dp.isTransferring) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = Color(0xFF00303F)),
                elevation = CardDefaults.cardElevation(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 36.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { dp.transferProgress ?: 0f },
                            modifier = Modifier.size(88.dp),
                            color = Teal,
                            trackColor = Color.White.copy(alpha = 0.1f),
                            strokeWidth = 6.dp
                        )
                        Text(
                            text = "${((dp.transferProgress ?: 0f) * 100).toInt()}%",
                            color = Color.White,
                            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = dp.operationLabel.ifBlank { "Working…" },
                        color = DimWhite,
                        style = TextStyle(fontSize = 13.sp, letterSpacing = 0.3.sp)
                    )
                }
            }
        }
    }
    } // end outer Box

    // Action bar
    DualPaneActionBar(
        hasSelection = run {
            val src = if (dp.activePaneIsLeft) dp.leftPane else dp.rightPane
            src.selectedPaths.isNotEmpty()
        },
        isTransferring = dp.isTransferring,
        onCopy = { viewModel.copySelected() },
        onMove = { viewModel.moveSelected() },
        onZip = { viewModel.zipSelected(it) },
        onTar = { viewModel.tarSelected(it) },
        onDelete = { viewModel.deleteSelected() }
    )
}

// ── Pane column ───────────────────────────────────────────────────────────────

@Composable
private fun PaneColumn(
    pane: PaneState,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    onActivate: () -> Unit,
    onNavigateTo: (String) -> Unit,
    onNavigateUp: () -> Unit,
    onToggleSelect: (String) -> Unit,
    onSelectAll: () -> Unit,
    onClearSelection: () -> Unit,
    onToggleLocal: () -> Unit
) {
    val borderColor = if (isActive) ActiveBorder else Color.Transparent

    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(TerminalBackground)
            .border(width = 1.dp, color = borderColor)
            .clickable(onClick = onActivate)
    ) {
        // Pane header
        PaneHeader(
            pane = pane,
            isActive = isActive,
            onNavigateUp = onNavigateUp,
            onSelectAll = onSelectAll,
            onClearSelection = onClearSelection,
            onToggleLocal = onToggleLocal
        )

        // Content
        when {
            pane.isLoading -> Box(
                Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = Teal, modifier = Modifier.size(24.dp), strokeWidth = 2.dp) }

            pane.error != null -> Box(
                Modifier.weight(1f).fillMaxWidth().padding(8.dp),
                contentAlignment = Alignment.Center
            ) { Text(pane.error, color = Color(0xFFFF8A80), fontSize = 11.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center) }

            pane.files.isEmpty() -> Box(
                Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) { Text("Empty", color = DimWhite, fontSize = 11.sp) }

            else -> LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                items(pane.files, key = { it.path }) { file ->
                    PaneFileRow(
                        file = file,
                        selected = file.path in pane.selectedPaths,
                        onToggleSelect = { onToggleSelect(file.path) },
                        onNavigate = { if (file.isDirectory) onNavigateTo(file.path) }
                    )
                    HorizontalDivider(color = SubtleDiv, thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
private fun PaneHeader(
    pane: PaneState,
    isActive: Boolean,
    onNavigateUp: () -> Unit,
    onSelectAll: () -> Unit,
    onClearSelection: () -> Unit,
    onToggleLocal: () -> Unit
) {
    val allSelected = pane.files.isNotEmpty() && pane.selectedPaths.size == pane.files.size
    val anySelected = pane.selectedPaths.isNotEmpty()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isActive) Color(0xFF005060) else TerminalToolbarBackground)
            .padding(horizontal = 4.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Local/Remote badge
        Surface(
            shape = MaterialTheme.shapes.extraSmall,
            color = if (pane.isLocal) Color(0xFF1B5E20) else Color(0xFF0D47A1),
            modifier = Modifier.clickable(onClick = onToggleLocal)
        ) {
            Text(
                text = if (pane.isLocal) "L" else "R",
                color = Color.White,
                style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
            )
        }

        Spacer(Modifier.width(4.dp))

        // Path
        Text(
            text = pane.path,
            color = if (isActive) Teal else DimWhite,
            style = TextStyle(fontSize = 10.sp, fontFamily = FontFamily.Monospace),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        // Navigate up
        if (pane.path != "/" && pane.path.length > 1) {
            IconButton(onClick = onNavigateUp, modifier = Modifier.size(28.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Up", tint = DimWhite, modifier = Modifier.size(14.dp))
            }
        }

        // Select all / clear
        IconButton(
            onClick = if (allSelected) onClearSelection else onSelectAll,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                if (anySelected) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                "Select all",
                tint = if (anySelected) Teal else DimWhite,
                modifier = Modifier.size(14.dp)
            )
        }
    }

    // Selection count indicator
    if (anySelected) {
        Text(
            text = "${pane.selectedPaths.size} selected",
            color = Teal,
            style = TextStyle(fontSize = 9.sp),
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF003040))
                .padding(horizontal = 6.dp, vertical = 1.dp)
        )
    }
}

@Composable
private fun PaneFileRow(
    file: FileEntry,
    selected: Boolean,
    onToggleSelect: () -> Unit,
    onNavigate: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (selected) SelectedBg else Color.Transparent)
            .clickable(onClick = if (file.isDirectory) onNavigate else onToggleSelect)
            .padding(start = 4.dp, end = 4.dp, top = 5.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox for selection
        Icon(
            imageVector = if (selected) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
            contentDescription = null,
            tint = if (selected) Teal else DimWhite,
            modifier = Modifier
                .size(16.dp)
                .clickable(onClick = onToggleSelect)
        )

        Spacer(Modifier.width(4.dp))

        // File/folder icon
        Icon(
            imageVector = if (file.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
            contentDescription = null,
            tint = if (file.isDirectory) Teal else DimWhite,
            modifier = Modifier.size(14.dp)
        )

        Spacer(Modifier.width(4.dp))

        // Name + size
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = file.name,
                color = Color.White,
                style = TextStyle(fontSize = 11.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!file.isDirectory) {
                Text(
                    text = formatSize(file.size),
                    color = DimWhite,
                    style = TextStyle(fontSize = 9.sp)
                )
            }
        }
    }
}

// ── Action bar ────────────────────────────────────────────────────────────────

@Composable
private fun DualPaneActionBar(
    hasSelection: Boolean,
    isTransferring: Boolean,
    onCopy: () -> Unit,
    onMove: () -> Unit,
    onZip: (String) -> Unit,
    onTar: (String) -> Unit,
    onDelete: () -> Unit
) {
    var showZipDialog by remember { mutableStateOf(false) }
    var showTarDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var archiveName by remember { mutableStateOf("archive") }

    Surface(color = TerminalToolbarBackground, tonalElevation = 4.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ActionBtn("Copy", Icons.Default.ContentCopy, hasSelection && !isTransferring) { onCopy() }
            ActionBtn("Move", Icons.Default.DriveFileMove, hasSelection && !isTransferring) { onMove() }
            ActionBtn("Zip", Icons.Default.FolderZip, hasSelection && !isTransferring) { archiveName = "archive"; showZipDialog = true }
            ActionBtn("Tar", Icons.Default.Archive, hasSelection && !isTransferring) { archiveName = "archive"; showTarDialog = true }
            ActionBtn("Del", Icons.Default.Delete, hasSelection && !isTransferring, color = Color(0xFFEF5350)) { showDeleteConfirm = true }
        }
    }

    // Zip dialog
    if (showZipDialog) {
        ArchiveNameDialog(
            title = "Create ZIP",
            suffix = ".zip",
            name = archiveName,
            onNameChange = { archiveName = it },
            onConfirm = { onZip(archiveName); showZipDialog = false },
            onDismiss = { showZipDialog = false }
        )
    }

    // Tar dialog
    if (showTarDialog) {
        ArchiveNameDialog(
            title = "Create TAR.GZ",
            suffix = ".tar.gz",
            name = archiveName,
            onNameChange = { archiveName = it },
            onConfirm = { onTar(archiveName); showTarDialog = false },
            onDismiss = { showTarDialog = false }
        )
    }

    // Delete confirm dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete selected files?", style = TextStyle(fontSize = 15.sp)) },
            text = { Text("This action cannot be undone.", style = TextStyle(fontSize = 13.sp)) },
            confirmButton = {
                TextButton(
                    onClick = { onDelete(); showDeleteConfirm = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF5350))
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ActionBtn(
    label: String,
    icon: ImageVector,
    enabled: Boolean,
    color: Color = Teal,
    onClick: () -> Unit
) {
    val tint = if (enabled) color else DimWhite
    Column(
        modifier = Modifier
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, label, tint = tint, modifier = Modifier.size(18.dp))
        Text(label, color = tint, style = TextStyle(fontSize = 9.sp))
    }
}

@Composable
private fun ArchiveNameDialog(
    title: String,
    suffix: String,
    name: String,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = TextStyle(fontSize = 15.sp)) },
        text = {
            Column {
                Text("Output name:", style = TextStyle(fontSize = 12.sp, color = DimWhite))
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    singleLine = true,
                    suffix = { Text(suffix, color = DimWhite, fontSize = 12.sp) },
                    textStyle = TextStyle(fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = name.isNotBlank()) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ── Utilities ─────────────────────────────────────────────────────────────────

internal fun formatSize(bytes: Long): String = when {
    bytes >= 1_073_741_824L -> "%.1f GB".format(bytes / 1_073_741_824.0)
    bytes >= 1_048_576L -> "%.1f MB".format(bytes / 1_048_576.0)
    bytes >= 1_024L -> "%.1f KB".format(bytes / 1_024.0)
    else -> "$bytes B"
}
