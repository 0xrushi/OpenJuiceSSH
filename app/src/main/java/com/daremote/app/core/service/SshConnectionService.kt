package com.daremote.app.core.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.daremote.app.DaRemoteApp
import com.daremote.app.MainActivity
import com.daremote.app.R
import com.daremote.app.core.data.ssh.SshSessionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SshConnectionService : Service() {

    @Inject lateinit var sessionManager: SshSessionManager
    @Inject lateinit var tunnelManager: TunnelManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, createNotification(0))
        tunnelManager.startAutoConnectTunnels()

        scope.launch {
            sessionManager.statusMap.collectLatest { statusMap ->
                val count = statusMap.count { it.value == com.daremote.app.core.domain.model.ConnectionStatus.CONNECTED }
                val notification = createNotification(count)
                val manager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
                manager.notify(NOTIFICATION_ID, notification)

                if (count == 0 && tunnelManager.tunnelStates.value.isEmpty()) {
                    stopSelf()
                }
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun createNotification(activeConnections: Int): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, DaRemoteApp.CHANNEL_SSH)
            .setContentTitle(getString(R.string.ssh_notification_title))
            .setContentText(getString(R.string.ssh_notification_text, activeConnections))
            .setSmallIcon(android.R.drawable.ic_menu_manage)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    companion object {
        const val NOTIFICATION_ID = 1001
    }
}
