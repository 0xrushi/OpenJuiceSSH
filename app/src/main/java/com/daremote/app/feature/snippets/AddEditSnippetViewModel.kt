package com.daremote.app.feature.snippets

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daremote.app.core.domain.model.Snippet
import com.daremote.app.core.domain.repository.SnippetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditSnippetState(
    val name: String = "",
    val command: String = "",
    val description: String = "",
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddEditSnippetViewModel @Inject constructor(
    private val snippetRepository: SnippetRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val snippetId: Long? = savedStateHandle.get<String>("snippetId")?.toLongOrNull()
    private val _state = MutableStateFlow(AddEditSnippetState())
    val state: StateFlow<AddEditSnippetState> = _state

    init {
        snippetId?.let { id ->
            viewModelScope.launch {
                snippetRepository.getSnippetById(id)?.let { snippet ->
                    _state.update {
                        it.copy(
                            name = snippet.name,
                            command = snippet.command,
                            description = snippet.description ?: "",
                            isEditing = true
                        )
                    }
                }
            }
        }
    }

    fun updateName(v: String) = _state.update { it.copy(name = v) }
    fun updateCommand(v: String) = _state.update { it.copy(command = v) }
    fun updateDescription(v: String) = _state.update { it.copy(description = v) }

    fun save() {
        val s = _state.value
        if (s.name.isBlank() || s.command.isBlank()) {
            _state.update { it.copy(error = "Name and command are required") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            try {
                val snippet = Snippet(
                    id = snippetId ?: 0,
                    name = s.name,
                    command = s.command,
                    description = s.description.ifBlank { null }
                )
                if (snippetId != null) snippetRepository.updateSnippet(snippet)
                else snippetRepository.saveSnippet(snippet)
                _state.update { it.copy(isSaving = false, saved = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }
}
