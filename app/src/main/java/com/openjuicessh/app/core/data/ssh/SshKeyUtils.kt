package com.openjuicessh.app.core.data.ssh

import com.hierynomus.sshj.key.KeyAlgorithms
import net.i2p.crypto.eddsa.EdDSAKey
import net.i2p.crypto.eddsa.EdDSAPrivateKey
import net.i2p.crypto.eddsa.EdDSAPublicKey
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec
import net.schmizz.sshj.common.KeyType
import net.schmizz.sshj.userauth.keyprovider.KeyProvider
import net.schmizz.sshj.userauth.keyprovider.KeyPairWrapper
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.util.PrivateKeyFactory
import org.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory
import org.bouncycastle.openssl.PEMKeyPair
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import java.io.StringReader
import java.security.KeyFactory
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey
import java.security.interfaces.RSAPrivateCrtKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.RSAPublicKeySpec
import java.security.spec.X509EncodedKeySpec

object SshKeyUtils {
    
    fun parseKeyPair(privateKeyStr: String, provider: String = "BC"): KeyPair? {
        val reader = StringReader(privateKeyStr)
        val pemParser = PEMParser(reader)
        val obj = pemParser.readObject() ?: return null
        
        val converter = JcaPEMKeyConverter().setProvider(provider)
        
        return when (obj) {
            is PEMKeyPair -> {
                converter.getKeyPair(obj)
            }
            is PrivateKeyInfo -> {
                val priv = converter.getPrivateKey(obj)
                
                when {
                    priv is EdDSAPrivateKey -> {
                        val spec = EdDSAPublicKeySpec(priv.abyte, priv.params)
                        val pub = EdDSAPublicKey(spec)
                        KeyPair(pub, priv)
                    }
                    priv is RSAPrivateCrtKey -> {
                        val spec = RSAPublicKeySpec(priv.modulus, priv.publicExponent)
                        val pub = KeyFactory.getInstance("RSA", provider).generatePublic(spec)
                        KeyPair(pub, priv)
                    }
                    priv.algorithm == "Ed25519" || priv.algorithm == "EdDSA" -> {
                        // Handle BCEdDSAPrivateKey
                        val privInfo = PrivateKeyInfo.getInstance(obj)
                        val privKeyParams = PrivateKeyFactory.createKey(privInfo) as Ed25519PrivateKeyParameters
                        val pubKeyParams = privKeyParams.generatePublicKey()
                        val pubInfo = SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(pubKeyParams)
                        
                        val bcPubKey = converter.getPublicKey(pubInfo)
                        val x509Spec = X509EncodedKeySpec(bcPubKey.encoded)
                        val i2pPubKey = EdDSAPublicKey(x509Spec)
                        
                        val pkcs8Spec = PKCS8EncodedKeySpec(priv.encoded)
                        val i2pPrivKey = EdDSAPrivateKey(pkcs8Spec)
                        
                        KeyPair(i2pPubKey, i2pPrivKey)
                    }
                    else -> KeyPair(null, priv)
                }
            }
            else -> null
        }
    }

    fun createKeyProvider(keyPair: KeyPair): KeyProvider? {
        if (keyPair.public == null) return null
        
        val alg = keyPair.public.algorithm
        return if (alg.contains("Ed", ignoreCase = true) || keyPair.public is EdDSAKey) {
            object : KeyProvider {
                override fun getPublic(): PublicKey = keyPair.public
                override fun getPrivate(): PrivateKey = keyPair.private
                override fun getType(): KeyType = KeyType.ED25519
            }
        } else {
            KeyPairWrapper(keyPair)
        }
    }
}
