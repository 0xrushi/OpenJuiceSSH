package com.daremote.app.core.domain.model

data class Server(
    val id: Long = 0,
    val name: String,
    val host: String,
    val port: Int = 22,
    val username: String,
    val authType: AuthType,
    val credentialRef: String,
    val sshKeyId: Long? = null,
    val groupId: Long? = null,
    val fingerprint: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastConnectedAt: Long? = null,
    val proxyId: Long? = null,
    val sortOrder: Int = 0
)

enum class AuthType {
    PASSWORD,
    KEY,
    KEY_PASSPHRASE
}
