package com.daremote.app.feature.snippets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daremote.app.core.data.ssh.SshCommandExecutor
import com.daremote.app.core.domain.model.Server
import com.daremote.app.core.domain.model.Snippet
import com.daremote.app.core.domain.repository.ServerRepository
import com.daremote.app.core.domain.repository.SnippetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SnippetListState(
    val snippets: List<Snippet> = emptyList(),
    val servers: List<Server> = emptyList(),
    val isLoading: Boolean = true,
    val executionResult: String? = null,
    val executingSnippetId: Long? = null
)

@HiltViewModel
class SnippetListViewModel @Inject constructor(
    private val snippetRepository: SnippetRepository,
    private val serverRepository: ServerRepository,
    private val commandExecutor: SshCommandExecutor
) : ViewModel() {

    private val _executionState = MutableStateFlow<Pair<Long?, String?>>(null to null)

    val state: StateFlow<SnippetListState> = combine(
        snippetRepository.getAllSnippets(),
        serverRepository.getAllServers(),
        _executionState
    ) { snippets, servers, (execId, execResult) ->
        SnippetListState(
            snippets = snippets,
            servers = servers,
            isLoading = false,
            executingSnippetId = execId,
            executionResult = execResult
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SnippetListState())

    fun executeSnippet(snippet: Snippet, serverId: Long) {
        viewModelScope.launch {
            _executionState.value = snippet.id to null
            snippetRepository.markUsed(snippet.id)

            val command = substituteVariables(snippet.command)
            val result = commandExecutor.execute(serverId, command)
            _executionState.value = snippet.id to (result.getOrElse { it.message ?: "Error" })
        }
    }

    fun clearExecution() {
        _executionState.value = null to null
    }

    fun deleteSnippet(snippet: Snippet) {
        viewModelScope.launch { snippetRepository.deleteSnippet(snippet) }
    }

    private fun substituteVariables(command: String): String {
        return command
            .replace("{{date}}", java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date()))
            .replace("{{timestamp}}", System.currentTimeMillis().toString())
    }
}
