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
import net.schmizz.sshj.userauth.keyprovider.KeyPairWrapper
import com.hierynomus.sshj.key.KeyAlgorithms
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import org.bouncycastle.openssl.PEMKeyPair
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.crypto.util.PrivateKeyFactory
import org.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import java.io.IOException
import java.io.StringReader
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
        proxyKeyRef: String? = null,
        serverKeyRef: String? = null
    ): SSHClient = withContext(Dispatchers.IO) {
        updateStatus(server.id, ConnectionStatus.CONNECTING)
        try {
            Log.d("SshSessionManager", "Connecting to ${server.host}:${server.port} as ${server.username}")
            
            // Explicitly configure Ed25519 support in the client
            val config = net.schmizz.sshj.DefaultConfig()
            val keyAlgorithms = config.keyAlgorithms.toMutableList()
            keyAlgorithms.add(0, KeyAlgorithms.EdDSA25519())
            config.keyAlgorithms = keyAlgorithms
            Log.d("SshSessionManager", "Available key algorithms: ${config.keyAlgorithms.map { it.name }}")
            
            val client = SSHClient(config)
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
                        val jumpConfig = net.schmizz.sshj.DefaultConfig()
                        val jumpKeyAlgorithms = jumpConfig.keyAlgorithms.toMutableList()
                        jumpKeyAlgorithms.add(0, KeyAlgorithms.EdDSA25519())
                        jumpConfig.keyAlgorithms = jumpKeyAlgorithms
                        Log.d("SshSessionManager", "Proxy available key algorithms: ${jumpConfig.keyAlgorithms.map { it.name }}")
                        val jumpClient = SSHClient(jumpConfig)
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
                                    // If we have a key and a password, the password is used as the passphrase
                                    val passphrase = if (proxyKeyRef != null) proxy.password else null
                                    
                                    val keyProvider = try {
                                        val reader = java.io.StringReader(privateKeyStr)
                                        val pemParser = org.bouncycastle.openssl.PEMParser(reader)
                                        val obj = pemParser.readObject()
                                        
                                        Log.d("SshSessionManager", "Proxy PEM object type: ${obj?.javaClass?.name}")
                                        
                                        val converter = org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter().setProvider("BC")
                                        val keyPair = when (obj) {
                                            is org.bouncycastle.openssl.PEMKeyPair -> {
                                                converter.getKeyPair(obj)
                                            }
                                            is org.bouncycastle.asn1.pkcs.PrivateKeyInfo -> {
                                                val priv = converter.getPrivateKey(obj)
                                                Log.d("SshSessionManager", "Proxy parsed private key class: ${priv?.javaClass?.name}")
                                                
                                                when {
                                                    priv is net.i2p.crypto.eddsa.EdDSAPrivateKey -> {
                                                        val spec = net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec(priv.abyte, priv.params)
                                                        val pub = net.i2p.crypto.eddsa.EdDSAPublicKey(spec)
                                                        java.security.KeyPair(pub, priv)
                                                    }
                                                    priv is java.security.interfaces.RSAPrivateCrtKey -> {
                                                        val spec = java.security.spec.RSAPublicKeySpec(priv.modulus, priv.publicExponent)
                                                        val pub = java.security.KeyFactory.getInstance("RSA", "BC").generatePublic(spec)
                                                        java.security.KeyPair(pub, priv)
                                                    }
                                                    priv.algorithm == "Ed25519" || priv.algorithm == "EdDSA" -> {
                                                        val privInfo = PrivateKeyInfo.getInstance(obj)
                                                        val privKeyParams = PrivateKeyFactory.createKey(privInfo) as Ed25519PrivateKeyParameters
                                                        val pubKeyParams = privKeyParams.generatePublicKey()
                                                        val pubInfo = SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(pubKeyParams)
                                                        
                                                        // Convert BC Public Key to net.i2p EdDSAPublicKey for sshj compatibility
                                                        val bcPubKey = converter.getPublicKey(pubInfo)
                                                        val x509Spec = java.security.spec.X509EncodedKeySpec(bcPubKey.encoded)
                                                        val i2pPubKey = net.i2p.crypto.eddsa.EdDSAPublicKey(x509Spec)
                                                        
                                                        // Also convert BC Private Key to net.i2p EdDSAPrivateKey
                                                        val pkcs8Spec = java.security.spec.PKCS8EncodedKeySpec(priv.encoded)
                                                        val i2pPrivKey = net.i2p.crypto.eddsa.EdDSAPrivateKey(pkcs8Spec)
                                                        
                                                        java.security.KeyPair(i2pPubKey, i2pPrivKey)
                                                    }
                                                    else -> {
                                                        Log.w("SshSessionManager", "Proxy could not derive public key for ${priv?.javaClass?.name}")
                                                        java.security.KeyPair(null, priv)
                                                    }
                                                }
                                            }
                                            else -> null
                                        }
                                        
                                        val keyProvider = if (keyPair != null && keyPair.public != null) {
                                            val alg = keyPair.public.algorithm
                                            Log.d("SshSessionManager", "Proxy public key algorithm: $alg (${keyPair.public.javaClass.name})")
                                            if (alg.contains("Ed", ignoreCase = true) || keyPair.public is net.i2p.crypto.eddsa.EdDSAKey) {
                                                object : net.schmizz.sshj.userauth.keyprovider.KeyProvider {
                                                    override fun getPublic(): java.security.PublicKey = keyPair.public
                                                    override fun getPrivate(): java.security.PrivateKey = keyPair.private
                                                    override fun getType(): net.schmizz.sshj.common.KeyType = net.schmizz.sshj.common.KeyType.ED25519
                                                }
                                            } else {
                                                net.schmizz.sshj.userauth.keyprovider.KeyPairWrapper(keyPair)
                                            }
                                        } else {
                                            jumpClient.loadKeys(privateKeyStr, passphrase, null)
                                        }
                                        
                                        Log.d("SshSessionManager", "Final Proxy KeyProvider type: ${keyProvider.getType()}")
                                        keyProvider
                                    } catch (e: Exception) {
                                        Log.w("SshSessionManager", "Proxy key parsing failed: ${e.message}", e)
                                        jumpClient.loadKeys(privateKeyStr, passphrase, null)
                                    }
                                    
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
                    val privateKeyStr = serverKeyRef?.let { sshKeyManager.getPrivateKey(it) }
                        ?: credentialManager.retrieve(server.credentialRef)
                        ?: throw IllegalStateException("SSH key not found for server ${server.name}")

                    val passphrase = if (server.authType == AuthType.KEY_PASSPHRASE) {
                        if (serverKeyRef != null) {
                            credentialManager.retrieve(server.credentialRef)
                        } else {
                            credentialManager.retrieve("${server.credentialRef}_pass")
                        }
                    } else null

                    val keyProvider = try {
                        val reader = java.io.StringReader(privateKeyStr)
                        val pemParser = org.bouncycastle.openssl.PEMParser(reader)
                        val obj = pemParser.readObject()
                        
                        Log.d("SshSessionManager", "PEM object type: ${obj?.javaClass?.name}")
                        
                        val converter = org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter().setProvider("BC")
                        val keyPair = when (obj) {
                            is org.bouncycastle.openssl.PEMKeyPair -> {
                                converter.getKeyPair(obj)
                            }
                            is org.bouncycastle.asn1.pkcs.PrivateKeyInfo -> {
                                val priv = converter.getPrivateKey(obj)
                                Log.d("SshSessionManager", "Parsed private key class: ${priv?.javaClass?.name}")
                                
                                when {
                                    priv is net.i2p.crypto.eddsa.EdDSAPrivateKey -> {
                                        val spec = net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec(priv.abyte, priv.params)
                                        val pub = net.i2p.crypto.eddsa.EdDSAPublicKey(spec)
                                        java.security.KeyPair(pub, priv)
                                    }
                                    priv is java.security.interfaces.RSAPrivateCrtKey -> {
                                        val spec = java.security.spec.RSAPublicKeySpec(priv.modulus, priv.publicExponent)
                                        val pub = java.security.KeyFactory.getInstance("RSA", "BC").generatePublic(spec)
                                        java.security.KeyPair(pub, priv)
                                    }
                                    priv.algorithm == "Ed25519" || priv.algorithm == "EdDSA" -> {
                                        // This handles BCEdDSAPrivateKey and other BC EdDSA implementations
                                        // Derive public key by pre-computing the point from the private seed
                                        val privInfo = PrivateKeyInfo.getInstance(obj)
                                        val privKeyParams = PrivateKeyFactory.createKey(privInfo) as Ed25519PrivateKeyParameters
                                        val pubKeyParams = privKeyParams.generatePublicKey()
                                        val pubInfo = SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(pubKeyParams)
                                        
                                        // Convert BC Public Key to net.i2p EdDSAPublicKey for sshj compatibility
                                        val bcPubKey = converter.getPublicKey(pubInfo)
                                        val x509Spec = java.security.spec.X509EncodedKeySpec(bcPubKey.encoded)
                                        val i2pPubKey = net.i2p.crypto.eddsa.EdDSAPublicKey(x509Spec)
                                        
                                        // Also convert BC Private Key to net.i2p EdDSAPrivateKey
                                        val pkcs8Spec = java.security.spec.PKCS8EncodedKeySpec(priv.encoded)
                                        val i2pPrivKey = net.i2p.crypto.eddsa.EdDSAPrivateKey(pkcs8Spec)
                                        
                                        java.security.KeyPair(i2pPubKey, i2pPrivKey)
                                    }
                                    else -> {
                                        Log.w("SshSessionManager", "Could not derive public key for ${priv?.javaClass?.name}")
                                        java.security.KeyPair(null, priv)
                                    }
                                }
                            }
                            else -> null
                        }
                        
                        val keyProvider = if (keyPair != null && keyPair.public != null) {
                            val alg = keyPair.public.algorithm
                            Log.d("SshSessionManager", "Public key algorithm: $alg (${keyPair.public.javaClass.name})")
                            if (alg.contains("Ed", ignoreCase = true) || keyPair.public is net.i2p.crypto.eddsa.EdDSAKey) {
                                object : net.schmizz.sshj.userauth.keyprovider.KeyProvider {
                                    override fun getPublic(): java.security.PublicKey = keyPair.public
                                    override fun getPrivate(): java.security.PrivateKey = keyPair.private
                                    override fun getType(): net.schmizz.sshj.common.KeyType = net.schmizz.sshj.common.KeyType.ED25519
                                }
                            } else {
                                net.schmizz.sshj.userauth.keyprovider.KeyPairWrapper(keyPair)
                            }
                        } else {
                            client.loadKeys(privateKeyStr, passphrase, null)
                        }
                        
                        Log.d("SshSessionManager", "Final KeyProvider type: ${keyProvider.getType()}")
                        keyProvider
                    } catch (e: Exception) {
                        Log.w("SshSessionManager", "BC key parsing failed: ${e.message}", e)
                        client.loadKeys(privateKeyStr, passphrase, null)
                    }
                    
                    client.authPublickey(server.username, keyProvider)
                }
            }

            sessions[server.id] = client
            updateStatus(server.id, ConnectionStatus.CONNECTED)
            Log.d("SshSessionManager", "Successfully connected to ${server.name}")
            client
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("Auth fail", ignoreCase = true) == true -> 
                    "Authentication failed: Check if your public key is in ~/.ssh/authorized_keys and permissions are 600"
                e.message?.contains("Key exchange failed", ignoreCase = true) == true ->
                    "Key exchange failed: Server may not support the selected key algorithm"
                else -> e.message ?: "Unknown error"
            }
            Log.e("SshSessionManager", "Failed to connect to ${server.name}: $errorMessage", e)
            updateStatus(server.id, ConnectionStatus.ERROR)
            jumpSessions.remove(server.id)?.disconnect()
            throw Exception(errorMessage, e)
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
