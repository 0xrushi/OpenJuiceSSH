package com.openjuicessh.app.feature.monitoring

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProcesses: (Long) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${state.serverName} - Monitoring") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToProcesses(state.serverId) }) {
                        Icon(Icons.AutoMirrored.Filled.List, "Processes")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Uptime
                state.stats.uptime?.let { uptime ->
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = uptime,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                // CPU
                state.stats.cpu?.let { cpu ->
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("CPU", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { cpu.usagePercent / 100f },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${cpu.usagePercent.toInt()}%")
                                    Text("${cpu.cores} cores")
                                }
                                Text(
                                    "Load: ${cpu.loadAvg1} / ${cpu.loadAvg5} / ${cpu.loadAvg15}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                // Memory
                state.stats.memory?.let { mem ->
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Memory", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = {
                                        if (mem.totalMb > 0) mem.usedMb.toFloat() / mem.totalMb else 0f
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(4.dp))
                                Text("${mem.usedMb} MB / ${mem.totalMb} MB")
                                if (mem.swapTotalMb > 0) {
                                    Text(
                                        "Swap: ${mem.swapUsedMb} MB / ${mem.swapTotalMb} MB",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }

                // Disks
                items(state.stats.disks) { disk ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(disk.mountPoint, style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { disk.usedPercent / 100f },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(4.dp))
                            Text("${disk.usedGb}G / ${disk.totalGb}G (${disk.usedPercent.toInt()}%)")
                        }
                    }
                }

                state.error?.let { error ->
                    item {
                        Text(error, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
