package com.openjuicessh.app.core.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "alert_rules",
    foreignKeys = [
        ForeignKey(
            entity = ServerEntity::class,
            parentColumns = ["id"],
            childColumns = ["serverId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("serverId")]
)
data class AlertRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val serverId: Long,
    val type: String,
    val threshold: Float,
    val checkIntervalMinutes: Int = 15,
    val isEnabled: Boolean = true,
    val lastCheckedAt: Long? = null,
    val lastStatus: String? = null
)
