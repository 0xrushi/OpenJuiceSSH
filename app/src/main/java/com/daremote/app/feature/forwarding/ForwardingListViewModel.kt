package com.daremote.app.feature.forwarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daremote.app.core.domain.model.ForwardingRule
import com.daremote.app.core.domain.model.TunnelState
import com.daremote.app.core.domain.repository.ForwardingRepository
import com.daremote.app.core.service.TunnelManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ForwardingListState(
    val rules: List<ForwardingRule> = emptyList(),
    val tunnelStates: Map<Long, TunnelState> = emptyMap(),
    val tunnelErrors: Map<Long, String> = emptyMap(),
    val isLoading: Boolean = true,
    val snackbarMessage: String? = null
)

@HiltViewModel
class ForwardingListViewModel @Inject constructor(
    private val forwardingRepository: ForwardingRepository,
    private val tunnelManager: TunnelManager
) : ViewModel() {

    private val _state = MutableStateFlow(ForwardingListState())
    val state: StateFlow<ForwardingListState> = _state

    init {
        viewModelScope.launch {
            combine(
                forwardingRepository.getAllRules(),
                tunnelManager.tunnelStates,
                tunnelManager.tunnelErrors
            ) { rules, states, errors ->
                Triple(rules, states, errors)
            }.collect { (rules, states, errors) ->
                val prevErrors = _state.value.tunnelErrors
                val newErrorEntry = errors.entries.firstOrNull { (id, msg) -> prevErrors[id] != msg }
                _state.update {
                    it.copy(
                        rules = rules,
                        tunnelStates = states,
                        tunnelErrors = errors,
                        isLoading = false,
                        snackbarMessage = newErrorEntry?.let { (_, msg) -> msg } ?: it.snackbarMessage
                    )
                }
            }
        }
    }

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

    fun snackbarShown() {
        _state.update { it.copy(snackbarMessage = null) }
    }
}
