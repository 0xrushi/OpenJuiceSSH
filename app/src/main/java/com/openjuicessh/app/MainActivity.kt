package com.openjuicessh.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import com.openjuicessh.app.core.ui.theme.DaRemoteTheme
import com.openjuicessh.app.feature.settings.SettingsViewModel
import com.openjuicessh.app.navigation.AppNavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsState by settingsViewModel.state.collectAsStateWithLifecycle()
            DaRemoteTheme(darkTheme = settingsState.darkMode) {
                AppNavGraph()
            }
        }
    }
}
