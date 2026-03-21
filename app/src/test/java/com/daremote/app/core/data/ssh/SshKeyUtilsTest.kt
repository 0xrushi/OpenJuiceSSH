package com.daremote.app.core.data.ssh

import net.i2p.crypto.eddsa.EdDSAPrivateKey
import net.i2p.crypto.eddsa.EdDSAPublicKey
import net.schmizz.sshj.common.KeyType
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.jcajce.JcaPEMWriter
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.StringWriter
import java.security.KeyPairGenerator
import java.security.Security

class SshKeyUtilsTest {

    @Before
    fun setUp() {
        // Add BouncyCastle provider if not already present
        if (Security.getProvider("BC") == null) {
            Security.addProvider(BouncyCastleProvider())
        }
    }

    @Test
    fun `test parseKeyPair correctly converts Ed25519 key to i2p implementation`() {
        // 1. Generate a real Ed25519 key pair using BouncyCastle
        val kpg = KeyPairGenerator.getInstance("Ed25519", "BC")
        val keyPair = kpg.generateKeyPair()
        
        // 2. Convert to PEM string
        val sw = StringWriter()
        val pw = JcaPEMWriter(sw)
        pw.writeObject(PrivateKeyInfo.getInstance(keyPair.private.encoded))
        pw.flush()
        pw.close()
        val pemString = sw.toString()
        
        // 3. Parse it using our utility
        val parsedKeyPair = SshKeyUtils.parseKeyPair(pemString, "BC")
        
        // 4. Verify results
        assertNotNull("Parsed key pair should not be null", parsedKeyPair)
        assertNotNull("Public key should not be null", parsedKeyPair?.public)
        assertNotNull("Private key should not be null", parsedKeyPair?.private)
        
        // CRITICAL CHECK: Verify it's the i2p implementation that sshj expects
        assertTrue("Public key should be net_i2p_crypto_eddsa_EdDSAPublicKey", 
            parsedKeyPair?.public is EdDSAPublicKey)
        assertTrue("Private key should be net_i2p_crypto_eddsa_EdDSAPrivateKey", 
            parsedKeyPair?.private is EdDSAPrivateKey)
    }

    @Test
    fun `test createKeyProvider returns ED25519 type for EdDSA keys`() {
        // Generate a key pair
        val kpg = KeyPairGenerator.getInstance("Ed25519", "BC")
        val keyPair = kpg.generateKeyPair()
        
        // Parse it to get the i2p implementation
        val sw = StringWriter()
        val pw = JcaPEMWriter(sw)
        pw.writeObject(PrivateKeyInfo.getInstance(keyPair.private.encoded))
        pw.flush()
        val pemString = sw.toString()
        val i2pKeyPair = SshKeyUtils.parseKeyPair(pemString, "BC")!!
        
        // Create provider
        val provider = SshKeyUtils.createKeyProvider(i2pKeyPair)
        
        assertNotNull("Provider should not be null", provider)
        assertEquals("Provider type should be ED25519", KeyType.ED25519, provider?.getType())
    }
}
