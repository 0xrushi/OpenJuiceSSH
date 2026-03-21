package com.daremote.app.core.service

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.daremote.app.DaRemoteApp
import com.daremote.app.R
import com.daremote.app.core.data.ssh.SshCommandExecutor
import com.daremote.app.core.data.ssh.SshSessionManager
import com.daremote.app.core.domain.model.AlertStatus
import com.daremote.app.core.domain.model.AlertType
import com.daremote.app.core.domain.repository.AlertRepository
import com.daremote.app.core.domain.repository.ServerRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class AlertWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val alertRepository: AlertRepository,
    private val serverRepository: ServerRepository,
    private val sessionManager: SshSessionManager,
    private val commandExecutor: SshCommandExecutor
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val rules = alertRepository.getEnabledRules()
        if (rules.isEmpty()) return Result.success()

        val rulesByServer = rules.groupBy { it.serverId }

        for ((serverId, serverRules) in rulesByServer) {
            val server = serverRepository.getServerById(serverId) ?: continue

            try {
                if (!sessionManager.isConnected(serverId)) {
                    sessionManager.connect(server)
                }

                for (rule in serverRules) {
                    try {
                        val status = checkRule(serverId, rule.type, rule.threshold)
                        alertRepository.updateStatus(rule.id, status.name)

                        if (status == AlertStatus.ALERT) {
                            sendNotification(
                                "${rule.type.name} Alert: ${server.name}",
                                "${rule.type.name} exceeded ${rule.threshold.toInt()}% threshold"
                            )
                        }
                    } catch (e: Exception) {
                        alertRepository.updateStatus(rule.id, AlertStatus.ERROR.name)
                    }
                }

                sessionManager.disconnect(serverId)
            } catch (e: Exception) {
                serverRules.forEach { rule ->
                    alertRepository.updateStatus(rule.id, AlertStatus.ERROR.name)
                    if (rule.type == AlertType.PING) {
                        sendNotification(
                            "Server Down: ${server.name}",
                            "Cannot reach ${server.host}:${server.port}"
                        )
                    }
                }
            }
        }

        return Result.success()
    }

    private suspend fun checkRule(serverId: Long, type: AlertType, threshold: Float): AlertStatus {
        return when (type) {
            AlertType.CPU -> {
                val output = commandExecutor.execute(serverId, "top -bn1 | head -5").getOrThrow()
                val idle = Regex("""(\d+\.?\d*)\s*id""").find(output)
                    ?.groupValues?.get(1)?.toFloatOrNull() ?: 100f
                val usage = 100f - idle
                if (usage >= threshold) AlertStatus.ALERT else AlertStatus.OK
            }
            AlertType.MEMORY -> {
                val output = commandExecutor.execute(serverId, "free -m").getOrThrow()
                val memLine = output.lines().find { it.startsWith("Mem:") } ?: return AlertStatus.ERROR
                val parts = memLine.trim().split(Regex("\\s+"))
                val total = parts.getOrNull(1)?.toFloatOrNull() ?: return AlertStatus.ERROR
                val used = parts.getOrNull(2)?.toFloatOrNull() ?: return AlertStatus.ERROR
                val usage = (used / total) * 100
                if (usage >= threshold) AlertStatus.ALERT else AlertStatus.OK
            }
            AlertType.DISK -> {
                val output = commandExecutor.execute(serverId, "df -h / | tail -1").getOrThrow()
                val percent = Regex("""(\d+)%""").find(output)
                    ?.groupValues?.get(1)?.toFloatOrNull() ?: return AlertStatus.ERROR
                if (percent >= threshold) AlertStatus.ALERT else AlertStatus.OK
            }
            AlertType.PING -> {
                // If we got here, the connection succeeded
                AlertStatus.OK
            }
        }
    }

    private fun sendNotification(title: String, text: String) {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(applicationContext, DaRemoteApp.CHANNEL_ALERTS)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
