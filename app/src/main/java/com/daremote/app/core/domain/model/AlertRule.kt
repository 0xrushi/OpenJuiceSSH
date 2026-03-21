package com.daremote.app.core.domain.model

data class AlertRule(
    val id: Long = 0,
    val serverId: Long,
    val type: AlertType,
    val threshold: Float,
    val checkIntervalMinutes: Int = 15,
    val isEnabled: Boolean = true,
    val lastCheckedAt: Long? = null,
    val lastStatus: AlertStatus? = null
)

enum class AlertType {
    CPU,
    MEMORY,
    DISK,
    PING
}

enum class AlertStatus {
    OK,
    ALERT,
    ERROR
}
