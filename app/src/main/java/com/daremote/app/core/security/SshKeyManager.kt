package com.daremote.app.core.security

import com.daremote.app.core.domain.model.KeyType
import com.daremote.app.core.domain.model.SshKey
import net.schmizz.sshj.common.KeyType as SshjKeyType
import java.io.StringWriter
import java.security.KeyPair
import java.security.KeyPairGenerator
import javax.inject.Inject
import javax.inject.Singleton
import net.i2p.crypto.eddsa.KeyPairGenerator as EdDSAKeyPairGenerator

@Singleton
class SshKeyManager @Inject constructor(
    private val credentialManager: CredentialManager
) {
    fun generateKeyPair(name: String, type: KeyType, bits: Int = 4096): SshKey {
        val keyPair = when (type) {
            KeyType.RSA -> {
                val generator = KeyPairGenerator.getInstance("RSA")
                generator.initialize(bits)
                generator.generateKeyPair()
            }
            KeyType.ED25519 -> {
                val generator = EdDSAKeyPairGenerator()
                generator.generateKeyPair()
            }
            KeyType.ECDSA -> {
                val generator = KeyPairGenerator.getInstance("EC")
                generator.initialize(256)
                generator.generateKeyPair()
            }
        }

        val publicKeyString = encodePublicKey(keyPair, type)
        val privateKeyString = encodePrivateKey(keyPair, type)
        val privateKeyRef = credentialManager.store(privateKeyString)

        return SshKey(
            name = name,
            type = type,
            publicKey = publicKeyString,
            privateKeyRef = privateKeyRef,
            hasPassphrase = false
        )
    }

    fun getPrivateKey(ref: String): String? {
        return credentialManager.retrieve(ref)
    }

    fun deleteKey(privateKeyRef: String) {
        credentialManager.delete(privateKeyRef)
    }

    private fun encodePublicKey(keyPair: KeyPair, type: KeyType): String {
        val encoded = java.util.Base64.getEncoder().encodeToString(keyPair.public.encoded)
        val prefix = when (type) {
            KeyType.RSA -> "ssh-rsa"
            KeyType.ED25519 -> "ssh-ed25519"
            KeyType.ECDSA -> "ecdsa-sha2-nistp256"
        }
        return "$prefix $encoded"
    }

    private fun encodePrivateKey(keyPair: KeyPair, type: KeyType): String {
        return java.util.Base64.getEncoder().encodeToString(keyPair.private.encoded)
    }
}
