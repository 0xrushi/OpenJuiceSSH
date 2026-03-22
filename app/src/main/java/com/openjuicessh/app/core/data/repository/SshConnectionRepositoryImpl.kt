package com.openjuicessh.app.core.data.repository

import com.openjuicessh.app.core.data.ssh.SshSessionManager
import com.openjuicessh.app.core.domain.model.ConnectionStatus
import com.openjuicessh.app.core.domain.model.Server
import com.openjuicessh.app.core.domain.repository.ProxyRepository
import com.openjuicessh.app.core.domain.repository.SshConnectionRepository
import com.openjuicessh.app.core.domain.repository.SshKeyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SshConnectionRepositoryImpl @Inject constructor(
    private val sessionManager: SshSessionManager,
    private val proxyRepository: ProxyRepository,
    private val sshKeyRepository: SshKeyRepository
) : SshConnectionRepository {

    override suspend fun connect(server: Server): Result<Unit> {
        return try {
            val proxy = server.proxyId?.let { proxyRepository.getProxyById(it) }
            val proxyKeyRef = proxy?.sshKeyId?.let { sshKeyRepository.getKeyById(it)?.privateKeyRef }
            val serverKeyRef = server.sshKeyId?.let { sshKeyRepository.getKeyById(it)?.privateKeyRef }
            sessionManager.connect(server, proxy, proxyKeyRef, serverKeyRef)
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
