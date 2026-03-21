package com.daremote.app.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
fun SettingsScreen(
    onNavigateToAlerts: () -> Unit,
    onNavigateToKeys: () -> Unit,
    onNavigateToBiometric: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Biometric
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToBiometric() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Fingerprint, null, modifier = Modifier.padding(end = 16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Biometric Lock", style = MaterialTheme.typography.bodyLarge)
                    Text("Require biometric to open app", style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = state.biometricEnabled, onCheckedChange = viewModel::setBiometric)
            }
            HorizontalDivider()

            // Dark mode
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(start = 40.dp)) {
                    Text("Dark Mode", style = MaterialTheme.typography.bodyLarge)
                }
                Switch(checked = state.darkMode, onCheckedChange = viewModel::setDarkMode)
            }
            HorizontalDivider()

            // SSH Keys
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToKeys() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Key, null, modifier = Modifier.padding(end = 16.dp))
                Text("SSH Key Manager", modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge)
                Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null)
            }
            HorizontalDivider()

            // Alerts
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToAlerts() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Notifications, null, modifier = Modifier.padding(end = 16.dp))
                Text("Alert Rules", modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge)
                Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null)
            }
            HorizontalDivider()

            // Terminal font size
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(start = 40.dp)) {
                    Text("Terminal Font Size", style = MaterialTheme.typography.bodyLarge)
                    Text("${state.terminalFontSize}sp", style = MaterialTheme.typography.bodySmall)
                }
                Row {
                    Text("-", modifier = Modifier
                        .clickable { if (state.terminalFontSize > 8) viewModel.setFontSize(state.terminalFontSize - 1) }
                        .padding(horizontal = 12.dp),
                        style = MaterialTheme.typography.headlineSmall)
                    Text("+", modifier = Modifier
                        .clickable { if (state.terminalFontSize < 24) viewModel.setFontSize(state.terminalFontSize + 1) }
                        .padding(horizontal = 12.dp),
                        style = MaterialTheme.typography.headlineSmall)
                }
            }
            HorizontalDivider()

            // About
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.padding(start = 40.dp)) {
                    Text("DaRemote", style = MaterialTheme.typography.bodyLarge)
                    Text("Version 1.0.0", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
