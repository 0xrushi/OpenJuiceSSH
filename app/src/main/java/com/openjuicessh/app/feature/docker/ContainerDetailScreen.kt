package com.openjuicessh.app.feature.docker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.openjuicessh.app.core.ui.theme.TerminalBackground
import com.openjuicessh.app.core.ui.theme.TerminalGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContainerDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: ContainerDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.containerName.ifEmpty { state.containerId.take(12) }) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = state.selectedTab) {
                Tab(selected = state.selectedTab == 0, onClick = { viewModel.selectTab(0) },
                    text = { Text("Logs") })
                Tab(selected = state.selectedTab == 1, onClick = { viewModel.selectTab(1) },
                    text = { Text("Stats") })
            }

            when (state.selectedTab) {
                0 -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(TerminalBackground)
                            .padding(8.dp)
                    ) {
                        items(state.logs) { line ->
                            Text(
                                text = line,
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = TerminalGreen
                                )
                            )
                        }
                    }
                }
                1 -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = state.stats.ifEmpty { "Loading stats..." },
                            style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 14.sp),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}
