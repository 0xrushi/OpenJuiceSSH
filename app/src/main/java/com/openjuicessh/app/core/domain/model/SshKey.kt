package com.openjuicessh.app.core.domain.model

data class SshKey(
    val id: Long = 0,
    val name: String,
    val type: KeyType,
    val publicKey: String,
    val privateKeyRef: String,
    val hasPassphrase: Boolean,
    val createdAt: Long = System.currentTimeMillis()
)

enum class KeyType {
    RSA,
    ED25519,
    ECDSA
}
