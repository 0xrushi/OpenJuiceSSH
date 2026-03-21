package com.daremote.app.feature.forwarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daremote.app.core.domain.model.ForwardingRule
import com.daremote.app.core.domain.model.TunnelState
import com.daremote.app.core.domain.repository.ForwardingRepository
import com.daremote.app.core.service.TunnelManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ForwardingListState(
    val rules: List<ForwardingRule> = emptyList(),
    val tunnelStates: Map<Long, TunnelState> = emptyMap(),
    val isLoading: Boolean = true
)

@HiltViewModel
class ForwardingListViewModel @Inject constructor(
    private val forwardingRepository: ForwardingRepository,
    private val tunnelManager: TunnelManager
) : ViewModel() {

    val state: StateFlow<ForwardingListState> = combine(
        forwardingRepository.getAllRules(),
        tunnelManager.tunnelStates
    ) { rules, states ->
        ForwardingListState(rules = rules, tunnelStates = states, isLoading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ForwardingListState())

    fun toggleTunnel(rule: ForwardingRule) {
        val currentState = tunnelManager.tunnelStates.value[rule.id]
        if (currentState == TunnelState.ACTIVE || currentState == TunnelState.CONNECTING) {
            tunnelManager.stopTunnel(rule.id)
        } else {
            tunnelManager.startTunnel(rule)
        }
    }

    fun deleteRule(rule: ForwardingRule) {
        viewModelScope.launch {
            tunnelManager.stopTunnel(rule.id)
            forwardingRepository.deleteRule(rule)
        }
    }
}
