package com.openjuicessh.app.feature.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openjuicessh.app.core.domain.model.AlertRule
import com.openjuicessh.app.core.domain.model.AlertType
import com.openjuicessh.app.core.domain.model.Server
import com.openjuicessh.app.core.domain.repository.AlertRepository
import com.openjuicessh.app.core.domain.repository.ServerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlertConfigState(
    val rules: List<AlertRule> = emptyList(),
    val servers: List<Server> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class AlertConfigViewModel @Inject constructor(
    private val alertRepository: AlertRepository,
    private val serverRepository: ServerRepository
) : ViewModel() {

    val state: StateFlow<AlertConfigState> = combine(
        alertRepository.getAllRules(),
        serverRepository.getAllServers()
    ) { rules, servers ->
        AlertConfigState(rules = rules, servers = servers, isLoading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AlertConfigState())

    fun addRule(serverId: Long, type: AlertType, threshold: Float) {
        viewModelScope.launch {
            alertRepository.saveRule(
                AlertRule(serverId = serverId, type = type, threshold = threshold)
            )
        }
    }

    fun deleteRule(rule: AlertRule) {
        viewModelScope.launch { alertRepository.deleteRule(rule) }
    }

    fun toggleRule(rule: AlertRule) {
        viewModelScope.launch {
            alertRepository.updateRule(rule.copy(isEnabled = !rule.isEnabled))
        }
    }
}
