package com.daremote.app.feature.proxies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daremote.app.core.domain.model.Proxy
import com.daremote.app.core.domain.repository.ProxyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProxyListState(
    val proxies: List<Proxy> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class ProxyListViewModel @Inject constructor(
    private val proxyRepository: ProxyRepository
) : ViewModel() {

    val state: StateFlow<ProxyListState> = proxyRepository.getAllProxies()
        .map { ProxyListState(proxies = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProxyListState(isLoading = true)
        )

    fun deleteProxy(proxy: Proxy) {
        viewModelScope.launch {
            proxyRepository.deleteProxy(proxy)
        }
    }
}
