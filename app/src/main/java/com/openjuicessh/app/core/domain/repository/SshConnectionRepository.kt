package com.openjuicessh.app.core.domain.repository

import com.openjuicessh.app.core.domain.model.ConnectionStatus
import com.openjuicessh.app.core.domain.model.Server
import kotlinx.coroutines.flow.Flow

interface SshConnectionRepository {
    suspend fun connect(server: Server): Result<Unit>
    suspend fun disconnect(serverId: Long)
    fun getConnectionStatus(serverId: Long): Flow<ConnectionStatus>
    fun getConnectionStatuses(): Flow<Map<Long, ConnectionStatus>>
    fun getActiveConnectionCount(): Flow<Int>
    fun isConnected(serverId: Long): Boolean
}
