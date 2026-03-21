package com.daremote.app.core.security

import com.daremote.app.core.domain.model.KeyType
import com.daremote.app.core.domain.model.SshKey
import net.schmizz.sshj.common.Buffer
import net.schmizz.sshj.common.KeyType as SshjKeyType
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.util.Base64
import org.bouncycastle.openssl.jcajce.JcaPEMWriter
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import java.io.StringWriter
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

    fun importKey(name: String, privateKeyContent: String): SshKey {
        val privateKeyRef = credentialManager.store(privateKeyContent)
        val type = when {
            privateKeyContent.contains("RSA PRIVATE KEY") -> KeyType.RSA
            privateKeyContent.contains("EC PRIVATE KEY") || privateKeyContent.contains("ECDSA") -> KeyType.ECDSA
            else -> KeyType.ED25519
        }
        return SshKey(
            name = name,
            type = type,
            publicKey = "",
            privateKeyRef = privateKeyRef,
            hasPassphrase = false
        )
    }

    fun deleteKey(privateKeyRef: String) {
        credentialManager.delete(privateKeyRef)
    }

    private fun encodePublicKey(keyPair: KeyPair, type: KeyType): String {
        // Encode in SSH wire format so it can be pasted directly into authorized_keys
        val buf = Buffer.PlainBuffer()
        val sshName = when (type) {
            KeyType.RSA -> "ssh-rsa"
            KeyType.ED25519 -> "ssh-ed25519"
            KeyType.ECDSA -> {
                // Determine curve size for ECDSA
                val pub = keyPair.public as java.security.interfaces.ECPublicKey
                val bitLen = pub.params.order.bitLength()
                "ecdsa-sha2-nistp$bitLen"
            }
        }
        
        val sshjType = SshjKeyType.fromKey(keyPair.public)
        sshjType.putPubKeyIntoBuffer(keyPair.public, buf)
        val b64 = Base64.getEncoder().encodeToString(buf.compactData)
        return "$sshName $b64"
    }

    private fun encodePrivateKey(keyPair: KeyPair, type: KeyType): String {
        // Use PKCS#8 encoding via BouncyCastle's PEM writer to ensure standard compatibility
        val sw = java.io.StringWriter()
        val pw = org.bouncycastle.openssl.jcajce.JcaPEMWriter(sw)
        pw.writeObject(org.bouncycastle.asn1.pkcs.PrivateKeyInfo.getInstance(keyPair.private.encoded))
        pw.flush()
        pw.close()
        return sw.toString()
    }
}
