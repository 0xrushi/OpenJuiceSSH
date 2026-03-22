package com.openjuicessh.app.core.domain.repository

import com.openjuicessh.app.core.domain.model.RemoteFile
import kotlinx.coroutines.flow.Flow

interface SftpRepository {
    suspend fun listDirectory(serverId: Long, path: String): Result<List<RemoteFile>>
    suspend fun download(serverId: Long, remotePath: String, localPath: String, onProgress: (Float) -> Unit): Result<Unit>
    suspend fun upload(serverId: Long, localPath: String, remotePath: String, onProgress: (Float) -> Unit): Result<Unit>
    suspend fun delete(serverId: Long, path: String): Result<Unit>
    suspend fun rename(serverId: Long, oldPath: String, newPath: String): Result<Unit>
    suspend fun mkdir(serverId: Long, path: String): Result<Unit>
    suspend fun chmod(serverId: Long, path: String, permissions: Int): Result<Unit>
    suspend fun stat(serverId: Long, path: String): Result<RemoteFile>
}
