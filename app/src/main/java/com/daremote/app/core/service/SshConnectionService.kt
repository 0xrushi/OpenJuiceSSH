package com.daremote.app.core.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.daremote.app.DaRemoteApp
import com.daremote.app.MainActivity
import com.daremote.app.R
import com.daremote.app.core.data.ssh.SshSessionManager
import com.daremote.app.core.data.ssh.TerminalSessionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SshConnectionService : Service() {

    @Inject lateinit var sessionManager: SshSessionManager
    @Inject lateinit var tunnelManager: TunnelManager
    @Inject lateinit var terminalSessionManager: TerminalSessionManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, createNotification(0))
        tunnelManager.startAutoConnectTunnels()

        scope.launch {
            combine(
                sessionManager.statusMap,
                tunnelManager.tunnelStates,
                terminalSessionManager.sessions
            ) { statusMap, tunnelStates, terminalSessions ->
                val connectedCount = statusMap.count { it.value == com.daremote.app.core.domain.model.ConnectionStatus.CONNECTED }
                val activeTunnels = tunnelStates.count { it.value == com.daremote.app.core.domain.model.TunnelState.ACTIVE }
                val activeTerminalSessions = terminalSessions.values.sumOf { it.size }
                
                connectedCount + activeTunnels + activeTerminalSessions
            }.collectLatest { totalCount ->
                val notification = createNotification(totalCount)
                val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(NOTIFICATION_ID, notification)

                if (totalCount == 0 && tunnelManager.tunnelStates.value.isEmpty()) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
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
        if (intent?.action == ACTION_DISCONNECT_ALL) {
            scope.launch {
                terminalSessionManager.closeAllSessions()
                sessionManager.disconnectAll()
                tunnelManager.stopAll()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun createNotification(activeConnections: Int): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val disconnectIntent = Intent(this, SshConnectionService::class.java).apply {
            action = ACTION_DISCONNECT_ALL
        }
        val disconnectPendingIntent = PendingIntent.getService(
            this, 1, disconnectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, DaRemoteApp.CHANNEL_SSH)
            .setContentTitle(getString(R.string.ssh_notification_title))
            .setContentText(getString(R.string.ssh_notification_text, activeConnections))
            .setSmallIcon(android.R.drawable.ic_menu_manage)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                getString(R.string.disconnect),
                disconnectPendingIntent
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    companion object {
        const val NOTIFICATION_ID = 1001
        const val ACTION_DISCONNECT_ALL = "com.daremote.app.ACTION_DISCONNECT_ALL"
    }
}
