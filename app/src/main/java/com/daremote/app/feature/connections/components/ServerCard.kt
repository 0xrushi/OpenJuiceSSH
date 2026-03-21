package com.daremote.app.feature.connections.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daremote.app.core.domain.model.ConnectionStatus
import com.daremote.app.core.domain.model.Server
import com.daremote.app.core.ui.theme.StatusConnected
import com.daremote.app.core.ui.theme.StatusConnecting
import com.daremote.app.core.ui.theme.StatusDisconnected
import com.daremote.app.core.ui.theme.StatusError
import com.daremote.app.feature.connections.ServerCardStats

private val GaugeCpu = Color(0xFF66BB6A)
private val GaugeMem = Color(0xFFAAAAAA)
private val GaugeDisk = Color(0xFF4CAF50)
private val GaugeNet = Color(0xFFFFC107)
private val GaugeTrack = Color(0xFF2E2E2E)
private val ServerNameColor = Color(0xFF42A5F5)

@Composable
fun ServerCard(
    server: Server,
    status: ConnectionStatus,
    stats: ServerCardStats?,
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

    val statusColor = when (status) {
        ConnectionStatus.CONNECTED -> StatusConnected
        ConnectionStatus.CONNECTING -> StatusConnecting
        ConnectionStatus.DISCONNECTED -> StatusDisconnected
        ConnectionStatus.ERROR -> StatusError
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // --- Header row ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = server.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = ServerNameColor
                    )
                    Text(
                        text = "${server.username}@${server.host}:${server.port}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!stats?.osName.isNullOrBlank()) {
                    Text(
                        text = stats!!.osName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }

                if (status == ConnectionStatus.CONNECTED) {
                    IconButton(onClick = onTerminal, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Terminal, contentDescription = "Terminal", modifier = Modifier.size(20.dp))
                    }
                } else {
                    IconButton(onClick = onConnect, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Connect", modifier = Modifier.size(20.dp))
                    }
                }

                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(20.dp))
                }

                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", modifier = Modifier.size(20.dp))
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        if (status == ConnectionStatus.CONNECTED) {
                            DropdownMenuItem(
                                text = { Text("Disconnect") },
                                onClick = { showMenu = false; onDisconnect() }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = { showMenu = false; onDelete() },
                            leadingIcon = { Icon(Icons.Default.Delete, null) }
                        )
                    }
                }
            }

            // --- Monitoring gauges ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                GaugeColumn(
                    progress = (stats?.cpuPercent ?: 0f) / 100f,
                    topText = "${String.format("%.1f", stats?.cpuPercent ?: 0f)}%",
                    bottomText = "",
                    color = GaugeCpu,
                    label = "CPU"
                )
                GaugeColumn(
                    progress = if ((stats?.memTotalMb ?: 0L) > 0) stats!!.memUsedMb.toFloat() / stats.memTotalMb else 0f,
                    topText = formatMb(stats?.memUsedMb ?: 0L),
                    bottomText = formatMb(stats?.memTotalMb ?: 0L),
                    color = GaugeMem,
                    label = "MEM"
                )
                val diskProgress = if ((stats?.diskTotalGb ?: 0f) > 0f) (stats?.diskUsedGb ?: 0f) / stats!!.diskTotalGb else 0f
                GaugeColumn(
                    progress = diskProgress,
                    topText = formatGb(stats?.diskUsedGb ?: 0f),
                    bottomText = formatGb(stats?.diskTotalGb ?: 0f),
                    color = GaugeDisk,
                    label = "DISK"
                )
                val maxNetRate = 10L * 1024 * 1024
                val netMax = maxOf(stats?.networkTxRate ?: 0L, stats?.networkRxRate ?: 0L)
                GaugeColumn(
                    progress = (netMax.toFloat() / maxNetRate).coerceIn(0f, 1f),
                    topText = "↑${formatRate(stats?.networkTxRate ?: 0L)}",
                    bottomText = "↓${formatRate(stats?.networkRxRate ?: 0L)}",
                    color = GaugeNet,
                    label = "NETWORK"
                )
            }

            // --- Uptime / Users row ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "UPTIME: ${stats?.uptime ?: "0"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "USERS: ${stats?.users ?: 0}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // --- Load avg / status dot row ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val l1 = stats?.loadAvg1 ?: 0f
                val l5 = stats?.loadAvg5 ?: 0f
                val l15 = stats?.loadAvg15 ?: 0f
                Text(
                    text = String.format("%.2f", l1),
                    style = MaterialTheme.typography.labelSmall,
                    color = loadAvgColor(l1)
                )
                Text(
                    text = "  ${String.format("%.2f", l5)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = loadAvgColor(l5)
                )
                Text(
                    text = "  ${String.format("%.2f", l15)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = loadAvgColor(l15)
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.Circle,
                    contentDescription = status.name,
                    tint = statusColor,
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}

@Composable
private fun GaugeColumn(
    progress: Float,
    topText: String,
    bottomText: String,
    color: Color,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(62.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 5.dp.toPx()
                drawArc(
                    color = GaugeTrack,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth)
                )
                if (progress > 0f) {
                    drawArc(
                        color = color,
                        startAngle = -90f,
                        sweepAngle = 360f * progress.coerceIn(0f, 1f),
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = topText,
                    fontSize = 9.sp,
                    color = color,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                if (bottomText.isNotEmpty()) {
                    Text(
                        text = bottomText,
                        fontSize = 8.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
        Text(
            text = label,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatMb(mb: Long): String = when {
    mb >= 1024 -> String.format("%.1fG", mb / 1024f)
    else -> "${mb}M"
}

private fun formatGb(gb: Float): String = when {
    gb >= 1024 -> String.format("%.1fT", gb / 1024f)
    else -> String.format("%.1fG", gb)
}

private fun formatRate(bytes: Long): String = when {
    bytes >= 1_000_000 -> String.format("%.1fM", bytes / 1_000_000f)
    bytes >= 1_000 -> String.format("%.1fk", bytes / 1_000f)
    else -> "${bytes}B"
}

private fun loadAvgColor(value: Float): Color = when {
    value >= 2f -> Color(0xFFF44336)
    value >= 1f -> Color(0xFFFFC107)
    else -> Color(0xFFAAAAAA)
}
