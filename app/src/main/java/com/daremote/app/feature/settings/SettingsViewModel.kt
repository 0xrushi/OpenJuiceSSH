package com.daremote.app.feature.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore(name = "settings")

data class SettingsState(
    val biometricEnabled: Boolean = false,
    val terminalFontSize: Int = 13,
    val darkMode: Boolean = true,
    val defaultPort: Int = 22
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state

    private val biometricKey = booleanPreferencesKey("biometric_enabled")
    private val fontSizeKey = intPreferencesKey("terminal_font_size")
    private val darkModeKey = booleanPreferencesKey("dark_mode")
    private val defaultPortKey = intPreferencesKey("default_port")

    init {
        viewModelScope.launch {
            context.dataStore.data.collect { prefs ->
                _state.update {
                    it.copy(
                        biometricEnabled = prefs[biometricKey] ?: false,
                        terminalFontSize = prefs[fontSizeKey] ?: 13,
                        darkMode = prefs[darkModeKey] ?: true,
                        defaultPort = prefs[defaultPortKey] ?: 22
                    )
                }
            }
        }
    }

    fun setBiometric(enabled: Boolean) = save { it[biometricKey] = enabled }
    fun setFontSize(size: Int) = save { it[fontSizeKey] = size }
    fun setDarkMode(enabled: Boolean) = save { it[darkModeKey] = enabled }

    private fun save(block: suspend (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        viewModelScope.launch {
            context.dataStore.edit { block(it) }
        }
    }
}
