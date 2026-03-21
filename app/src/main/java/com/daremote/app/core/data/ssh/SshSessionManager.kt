package com.daremote.app.core.data.ssh

import android.util.Log
import com.daremote.app.core.domain.model.*
import com.daremote.app.core.security.CredentialManager
import com.daremote.app.core.security.SshKeyManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import net.schmizz.sshj.userauth.method.AuthNone
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.SocketFactory

@Singleton
class SshSessionManager @Inject constructor(
    private val credentialManager: CredentialManager,
    private val sshKeyManager: SshKeyManager
) {
    private val sessions = ConcurrentHashMap<Long, SSHClient>()
    private val jumpSessions = ConcurrentHashMap<Long, SSHClient>() // To keep jump hosts alive
    private val _statusMap = MutableStateFlow<Map<Long, ConnectionStatus>>(emptyMap())
    val statusMap: StateFlow<Map<Long, ConnectionStatus>> = _statusMap

    val activeCount: Int get() = sessions.count { it.value.isConnected }

    suspend fun connect(
        server: Server, 
        proxy: com.daremote.app.core.domain.model.Proxy? = null, 
        proxyKeyRef: String? = null
    ): SSHClient = withContext(Dispatchers.IO) {
        updateStatus(server.id, ConnectionStatus.CONNECTING)
        try {
            Log.d("SshSessionManager", "Connecting to ${server.host}:${server.port} as ${server.username}")
            val client = SSHClient()
            client.addHostKeyVerifier(PromiscuousVerifier())
            
            var isConnectedViaProxy = false

            if (proxy != null) {
                Log.d("SshSessionManager", "Using proxy: ${proxy.name} (${proxy.type})")
                when (proxy.type) {
                    ProxyType.SOCKS5 -> {
                        val socksProxy = Proxy(Proxy.Type.SOCKS, InetSocketAddress(proxy.host, proxy.port))
                        client.socketFactory = object : SocketFactory() {
                            override fun createSocket(): Socket = Socket(socksProxy)
                            override fun createSocket(host: String, port: Int): Socket = Socket(socksProxy).apply { connect(InetSocketAddress(host, port)) }
                            override fun createSocket(host: String, port: Int, localHost: InetAddress, localPort: Int): Socket = Socket(socksProxy).apply { connect(InetSocketAddress(host, port)) }
                            override fun createSocket(host: InetAddress, port: Int): Socket = Socket(socksProxy).apply { connect(InetSocketAddress(host, port)) }
                            override fun createSocket(host: InetAddress, port: Int, localAddress: InetAddress, localPort: Int): Socket = Socket(socksProxy).apply { connect(InetSocketAddress(host, port)) }
                        }
                    }
                    ProxyType.HTTP -> {
                        client.socketFactory = object : SocketFactory() {
                            override fun createSocket(): Socket = createHttpProxySocket(proxy.host, proxy.port, server.host, server.port)
                            override fun createSocket(host: String, port: Int): Socket = createHttpProxySocket(proxy.host, proxy.port, host, port)
                            override fun createSocket(host: String, port: Int, localHost: InetAddress, localPort: Int): Socket = createHttpProxySocket(proxy.host, proxy.port, host, port)
                            override fun createSocket(host: InetAddress, port: Int): Socket = createHttpProxySocket(proxy.host, proxy.port, host.hostAddress, port)
                            override fun createSocket(host: InetAddress, port: Int, localAddress: InetAddress, localPort: Int): Socket = createHttpProxySocket(proxy.host, proxy.port, host.hostAddress, port)
                        }
                    }
                    ProxyType.SSH -> {
                        val jumpClient = SSHClient()
                        jumpClient.addHostKeyVerifier(PromiscuousVerifier())
                        jumpClient.connect(proxy.host, proxy.port)
                        
                        when (proxy.authType) {
                            ProxyAuthType.PASSWORD -> {
                                jumpClient.authPassword(proxy.username, proxy.password)
                            }
                            ProxyAuthType.KEY -> {
                                val privateKeyStr = proxyKeyRef?.let {
                                    sshKeyManager.getPrivateKey(it)
                                } ?: proxy.password
                                
                                if (privateKeyStr != null) {
                                    val keyProvider = jumpClient.loadKeys(privateKeyStr, null, null)
                                    jumpClient.authPublickey(proxy.username, keyProvider)
                                } else {
                                    throw IllegalStateException("Proxy private key not found")
                                }
                            }
                            else -> {
                                if (proxy.username != null) {
                                    jumpClient.auth(proxy.username, AuthNone())
                                }
                            }
                        }
                        jumpSessions[server.id] = jumpClient
                        
                        val tunnel = jumpClient.newDirectConnection(server.host, server.port)
                        client.connectVia(tunnel)
                        isConnectedViaProxy = true
                    }
                }
            }

            if (!isConnectedViaProxy) {
                client.connect(server.host, server.port)
            }

            when (server.authType) {
                AuthType.PASSWORD -> {
                    val password = credentialManager.retrieve(server.credentialRef)
                        ?: throw IllegalStateException("Password not found for server ${server.name}")
                    client.authPassword(server.username, password)
                }
                AuthType.KEY, AuthType.KEY_PASSPHRASE -> {
                    val privateKeyStr = if (server.sshKeyId != null) {
                        sshKeyManager.getPrivateKey(server.credentialRef)
                    } else {
                        credentialManager.retrieve(server.credentialRef)
                    } ?: throw IllegalStateException("SSH key not found for server ${server.name}")

                    val passphrase = if (server.authType == AuthType.KEY_PASSPHRASE) {
                        credentialManager.retrieve("${server.credentialRef}_pass")
                    } else null

                    val keyProvider = client.loadKeys(privateKeyStr, passphrase, null)
                    client.authPublickey(server.username, keyProvider)
                }
            }

            sessions[server.id] = client
            updateStatus(server.id, ConnectionStatus.CONNECTED)
            Log.d("SshSessionManager", "Successfully connected to ${server.name}")
            client
        } catch (e: Exception) {
            Log.e("SshSessionManager", "Failed to connect to ${server.name}", e)
            updateStatus(server.id, ConnectionStatus.ERROR)
            jumpSessions.remove(server.id)?.disconnect()
            throw e
        }
    }

    private fun createHttpProxySocket(proxyHost: String, proxyPort: Int, targetHost: String, targetPort: Int): Socket {
        val socket = Socket()
        socket.connect(InetSocketAddress(proxyHost, proxyPort))
        val out = socket.getOutputStream()
        out.write("CONNECT $targetHost:$targetPort HTTP/1.1\r\n\r\n".toByteArray())
        out.flush()
        val inputStream = socket.getInputStream()
        val buffer = ByteArray(1024)
        val read = inputStream.read(buffer)
        if (read == -1) throw IOException("HTTP Proxy: unexpected EOF")
        val response = String(buffer, 0, read)
        if (!response.contains("200 OK") && !response.contains("200 Connection established")) {
            socket.close()
            throw IOException("HTTP Proxy connection failed: $response")
        }
        return socket
    }

    fun disconnect(serverId: Long) {
        sessions.remove(serverId)?.let { client ->
            try {
                if (client.isConnected) client.disconnect()
            } catch (_: Exception) { }
        }
        jumpSessions.remove(serverId)?.let { jumpClient ->
            try {
                if (jumpClient.isConnected) jumpClient.disconnect()
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
