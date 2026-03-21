package com.daremote.app.core.domain.repository

import com.daremote.app.core.domain.model.DockerContainer
import kotlinx.coroutines.flow.Flow

interface DockerRepository {
    suspend fun isDockerAvailable(serverId: Long): Boolean
    suspend fun listContainers(serverId: Long): Result<List<DockerContainer>>
    suspend fun startContainer(serverId: Long, containerId: String): Result<Unit>
    suspend fun stopContainer(serverId: Long, containerId: String): Result<Unit>
    suspend fun restartContainer(serverId: Long, containerId: String): Result<Unit>
    suspend fun removeContainer(serverId: Long, containerId: String): Result<Unit>
    fun getLogs(serverId: Long, containerId: String, tail: Int = 100): Flow<String>
    suspend fun getStats(serverId: Long, containerId: String): Result<DockerContainer>
    suspend fun composeUp(serverId: Long, composePath: String): Result<String>
    suspend fun composeDown(serverId: Long, composePath: String): Result<String>
}
