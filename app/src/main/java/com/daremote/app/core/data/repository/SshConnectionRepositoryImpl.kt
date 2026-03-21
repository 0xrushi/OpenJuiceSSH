package com.daremote.app.core.data.repository

import com.daremote.app.core.data.ssh.SshSessionManager
import com.daremote.app.core.domain.model.ConnectionStatus
import com.daremote.app.core.domain.model.Server
import com.daremote.app.core.domain.repository.SshConnectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SshConnectionRepositoryImpl @Inject constructor(
    private val sessionManager: SshSessionManager
) : SshConnectionRepository {

    override suspend fun connect(server: Server): Result<Unit> {
        return try {
            sessionManager.connect(server)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun disconnect(serverId: Long) {
        sessionManager.disconnect(serverId)
    }

    override fun getConnectionStatus(serverId: Long): Flow<ConnectionStatus> =
        sessionManager.statusMap.map { it[serverId] ?: ConnectionStatus.DISCONNECTED }

    override fun getConnectionStatuses(): Flow<Map<Long, ConnectionStatus>> =
        sessionManager.statusMap

    override fun getActiveConnectionCount(): Flow<Int> =
        sessionManager.statusMap.map { map ->
            map.count { it.value == ConnectionStatus.CONNECTED }
        }

    override fun isConnected(serverId: Long): Boolean =
        sessionManager.isConnected(serverId)
}
