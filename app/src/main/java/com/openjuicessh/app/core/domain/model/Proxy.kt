package com.openjuicessh.app.core.domain.model

data class Proxy(
    val id: Long = 0,
    val name: String,
    val type: ProxyType,
    val host: String,
    val port: Int,
    val username: String? = null,
    val authType: ProxyAuthType = ProxyAuthType.NONE,
    val password: String? = null,
    val sshKeyId: Long? = null
)

enum class ProxyType {
    SSH,
    SOCKS5,
    HTTP
}

enum class ProxyAuthType {
    NONE,
    PASSWORD,
    KEY
}
