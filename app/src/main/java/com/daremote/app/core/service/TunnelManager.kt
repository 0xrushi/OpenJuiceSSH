package com.daremote.app.core.service

import com.daremote.app.core.data.ssh.SshSessionManager
import com.daremote.app.core.domain.model.ForwardingRule
import com.daremote.app.core.domain.model.ForwardingType
import com.daremote.app.core.domain.model.TunnelState
import com.daremote.app.core.domain.repository.ForwardingRepository
import com.daremote.app.core.domain.repository.ServerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import net.schmizz.sshj.connection.channel.forwarded.RemotePortForwarder
import net.schmizz.sshj.connection.channel.forwarded.SocketForwardingConnectListener
import java.net.InetSocketAddress
import java.net.ServerSocket
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TunnelManager @Inject constructor(
    private val sessionManager: SshSessionManager,
    private val forwardingRepository: ForwardingRepository,
    private val serverRepository: ServerRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val tunnelJobs = mutableMapOf<Long, Job>()
    private val _tunnelStates = MutableStateFlow<Map<Long, TunnelState>>(emptyMap())
    val tunnelStates: StateFlow<Map<Long, TunnelState>> = _tunnelStates

    fun startTunnel(rule: ForwardingRule) {
        if (tunnelJobs.containsKey(rule.id)) return

        val job = scope.launch {
            var retryDelay = 1000L
            while (isActive) {
                updateState(rule.id, TunnelState.CONNECTING)
                try {
                    val server = serverRepository.getServerById(rule.serverId)
                        ?: throw IllegalStateException("Server not found")

                    if (!sessionManager.isConnected(rule.serverId)) {
                        sessionManager.connect(server)
                    }

                    val client = sessionManager.getSession(rule.serverId)
                        ?: throw IllegalStateException("No SSH session")

                    when (rule.type) {
                        ForwardingType.LOCAL -> {
                            val params = net.schmizz.sshj.connection.channel.direct.Parameters(
                                rule.localHost, rule.localPort,
                                rule.remoteHost ?: "127.0.0.1", rule.remotePort ?: rule.localPort
                            )
                            val serverSocket = ServerSocket()
                            serverSocket.reuseAddress = true
                            serverSocket.bind(InetSocketAddress(rule.localHost, rule.localPort))
                            val forwarder = client.newLocalPortForwarder(params, serverSocket)
                            updateState(rule.id, TunnelState.ACTIVE)
                            forwardingRepository.setActive(rule.id, true)
                            retryDelay = 1000L
                            forwarder.listen() // Blocks until closed
                        }
                        ForwardingType.REMOTE -> {
                            val rpf = client.remotePortForwarder
                            val forward = RemotePortForwarder.Forward(
                                rule.localPort,
                            )
                            rpf.bind(
                                forward,
                                SocketForwardingConnectListener(
                                    InetSocketAddress(
                                        rule.remoteHost ?: "127.0.0.1",
                                        rule.remotePort ?: rule.localPort
                                    )
                                )
                            )
                            updateState(rule.id, TunnelState.ACTIVE)
                            forwardingRepository.setActive(rule.id, true)
                            retryDelay = 1000L
                            // Keep alive until cancelled
                            while (isActive && sessionManager.isConnected(rule.serverId)) {
                                delay(5000)
                            }
                        }
                        ForwardingType.DYNAMIC -> {
                            // Dynamic forwarding (SOCKS) implementation is not provided out-of-the-box by sshj.
                            // It requires a SOCKS server implementation that opens a newDirectConnection for each request.
                            updateState(rule.id, TunnelState.ERROR)
                            throw UnsupportedOperationException("Dynamic port forwarding (SOCKS) is not yet implemented")
                        }
                    }
                } catch (e: Exception) {
                    updateState(rule.id, TunnelState.ERROR)
                }
                forwardingRepository.setActive(rule.id, false)
                delay(retryDelay)
                retryDelay = (retryDelay * 2).coerceAtMost(60_000L)
            }
        }
        tunnelJobs[rule.id] = job
    }

    fun stopTunnel(ruleId: Long) {
        tunnelJobs.remove(ruleId)?.cancel()
        updateState(ruleId, TunnelState.STOPPED)
        scope.launch { forwardingRepository.setActive(ruleId, false) }
    }

    fun stopAll() {
        tunnelJobs.keys.toList().forEach { stopTunnel(it) }
    }

    fun startAutoConnectTunnels() {
        scope.launch {
            val rules = forwardingRepository.getAutoConnectRules()
            rules.forEach { startTunnel(it) }
        }
    }

    private fun updateState(ruleId: Long, state: TunnelState) {
        _tunnelStates.value = _tunnelStates.value.toMutableMap().apply { put(ruleId, state) }
    }
}
