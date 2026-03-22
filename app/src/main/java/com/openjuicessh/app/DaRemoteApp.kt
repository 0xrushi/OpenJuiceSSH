package com.openjuicessh.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security
import javax.inject.Inject

@HiltAndroidApp
class DaRemoteApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        setupSecurityProviders()
        createNotificationChannels()
    }

    private fun setupSecurityProviders() {
        // Remove existing providers to avoid conflicts
        java.security.Security.removeProvider("BC")
        java.security.Security.removeProvider("EdDSA")
        
        // Insert ours at the highest priority
        java.security.Security.insertProviderAt(org.bouncycastle.jce.provider.BouncyCastleProvider(), 1)
        java.security.Security.insertProviderAt(net.i2p.crypto.eddsa.EdDSASecurityProvider(), 2)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)

        val sshChannel = NotificationChannel(
            CHANNEL_SSH,
            getString(R.string.ssh_notification_channel),
            NotificationManager.IMPORTANCE_LOW
        )
        manager.createNotificationChannel(sshChannel)

        val alertChannel = NotificationChannel(
            CHANNEL_ALERTS,
            getString(R.string.alert_notification_channel),
            NotificationManager.IMPORTANCE_HIGH
        )
        manager.createNotificationChannel(alertChannel)
    }

    companion object {
        const val CHANNEL_SSH = "ssh_connections"
        const val CHANNEL_ALERTS = "server_alerts"
    }
}
