package com.daremote.app.feature.filemanager

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daremote.app.core.data.ssh.SftpClient
import com.daremote.app.core.domain.model.RemoteFile
import com.daremote.app.core.domain.repository.ServerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FileManagerState(
    val serverName: String = "",
    val currentPath: String = "/",
    val files: List<RemoteFile> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val transferProgress: Float? = null
)

@HiltViewModel
class FileManagerViewModel @Inject constructor(
    private val sftpClient: SftpClient,
    private val serverRepository: ServerRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val serverId: Long = savedStateHandle["serverId"] ?: 0L
    private val _state = MutableStateFlow(FileManagerState())
    val state: StateFlow<FileManagerState> = _state
    private val pathStack = mutableListOf("/")

    init {
        viewModelScope.launch {
            val server = serverRepository.getServerById(serverId)
            _state.update { it.copy(serverName = server?.name ?: "Unknown") }
            navigateTo("/")
        }
    }

    fun navigateTo(path: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val files = sftpClient.listDir(serverId, path)
                    .sortedWith(compareByDescending<RemoteFile> { it.isDirectory }.thenBy { it.name })
                if (path != pathStack.lastOrNull()) pathStack.add(path)
                _state.update { it.copy(currentPath = path, files = files, isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun navigateUp(): Boolean {
        if (pathStack.size <= 1) return false
        pathStack.removeLastOrNull()
        val parent = pathStack.lastOrNull() ?: "/"
        navigateTo(parent)
        return true
    }

    fun deleteFile(file: RemoteFile) {
        viewModelScope.launch {
            try {
                sftpClient.delete(serverId, file.path)
                navigateTo(_state.value.currentPath)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun rename(file: RemoteFile, newName: String) {
        viewModelScope.launch {
            try {
                val parent = _state.value.currentPath
                val newPath = if (parent.endsWith("/")) "$parent$newName" else "$parent/$newName"
                sftpClient.rename(serverId, file.path, newPath)
                navigateTo(parent)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun createDirectory(name: String) {
        viewModelScope.launch {
            try {
                val parent = _state.value.currentPath
                val path = if (parent.endsWith("/")) "$parent$name" else "$parent/$name"
                sftpClient.mkdir(serverId, path)
                navigateTo(parent)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun chmod(file: RemoteFile, permissions: Int) {
        viewModelScope.launch {
            try {
                sftpClient.chmod(serverId, file.path, permissions)
                navigateTo(_state.value.currentPath)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun download(file: RemoteFile, localPath: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(transferProgress = 0f) }
                sftpClient.download(serverId, file.path, localPath) { progress ->
                    _state.update { it.copy(transferProgress = progress) }
                }
                _state.update { it.copy(transferProgress = null) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, transferProgress = null) }
            }
        }
    }

    fun upload(localPath: String, fileName: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(transferProgress = 0f) }
                val parent = _state.value.currentPath
                val remotePath = if (parent.endsWith("/")) "$parent$fileName" else "$parent/$fileName"
                sftpClient.upload(serverId, localPath, remotePath) { progress ->
                    _state.update { it.copy(transferProgress = progress) }
                }
                _state.update { it.copy(transferProgress = null) }
                navigateTo(parent)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, transferProgress = null) }
            }
        }
    }
}
