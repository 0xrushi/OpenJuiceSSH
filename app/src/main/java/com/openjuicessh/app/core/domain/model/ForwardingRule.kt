package com.openjuicessh.app.core.domain.model

data class ForwardingRule(
    val id: Long = 0,
    val serverId: Long,
    val type: ForwardingType,
    val name: String,
    val localHost: String = "127.0.0.1",
    val localPort: Int,
    val remoteHost: String? = null,
    val remotePort: Int? = null,
    val proxyId: Long? = null,
    val autoConnect: Boolean = false,
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class ForwardingType {
    LOCAL,
    REMOTE,
    DYNAMIC
}

enum class TunnelState {
    ACTIVE,
    CONNECTING,
    ERROR,
    STOPPED
}
