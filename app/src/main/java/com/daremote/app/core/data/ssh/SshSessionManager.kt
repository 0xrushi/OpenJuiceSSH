package com.daremote.app.core.data.ssh

import com.daremote.app.core.domain.model.AuthType
import com.daremote.app.core.domain.model.ConnectionStatus
import com.daremote.app.core.domain.model.Server
import com.daremote.app.core.security.CredentialManager
import com.daremote.app.core.security.SshKeyManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.common.DisconnectReason
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import net.schmizz.sshj.userauth.keyprovider.KeyProvider
import java.io.StringReader
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SshSessionManager @Inject constructor(
    private val credentialManager: CredentialManager,
    private val sshKeyManager: SshKeyManager
) {
    private val sessions = ConcurrentHashMap<Long, SSHClient>()
    private val _statusMap = MutableStateFlow<Map<Long, ConnectionStatus>>(emptyMap())
    val statusMap: StateFlow<Map<Long, ConnectionStatus>> = _statusMap

    val activeCount: Int get() = sessions.count { it.value.isConnected }

    suspend fun connect(server: Server): SSHClient = withContext(Dispatchers.IO) {
        updateStatus(server.id, ConnectionStatus.CONNECTING)
        try {
            val client = SSHClient()
            client.addHostKeyVerifier(PromiscuousVerifier())
            client.connect(server.host, server.port)

            when (server.authType) {
                AuthType.PASSWORD -> {
                    val password = credentialManager.retrieve(server.credentialRef)
                        ?: throw IllegalStateException("Password not found for server ${server.name}")
                    client.authPassword(server.username, password)
                }
                AuthType.KEY, AuthType.KEY_PASSPHRASE -> {
                    val privateKeyStr = server.sshKeyId?.let { keyId ->
                        // Key is stored via SshKeyManager
                        sshKeyManager.getPrivateKey(server.credentialRef)
                    } ?: credentialManager.retrieve(server.credentialRef)
                    ?: throw IllegalStateException("SSH key not found for server ${server.name}")

                    val keyProvider = client.loadKeys(privateKeyStr, null, null)
                    client.authPublickey(server.username, keyProvider)
                }
            }

            sessions[server.id] = client
            updateStatus(server.id, ConnectionStatus.CONNECTED)
            client
        } catch (e: Exception) {
            updateStatus(server.id, ConnectionStatus.ERROR)
            throw e
        }
    }

    fun disconnect(serverId: Long) {
        sessions.remove(serverId)?.let { client ->
            try {
                if (client.isConnected) client.disconnect()
            } catch (_: Exception) { }
        }
        updateStatus(serverId, ConnectionStatus.DISCONNECTED)
    }

    fun disconnectAll() {
        sessions.keys.toList().forEach { disconnect(it) }
    }

    fun getSession(serverId: Long): SSHClient? = sessions[serverId]

    fun isConnected(serverId: Long): Boolean =
        sessions[serverId]?.isConnected == true

    fun getStatus(serverId: Long): ConnectionStatus =
        _statusMap.value[serverId] ?: ConnectionStatus.DISCONNECTED

    private fun updateStatus(serverId: Long, status: ConnectionStatus) {
        _statusMap.value = _statusMap.value.toMutableMap().apply {
            put(serverId, status)
        }
    }
}
