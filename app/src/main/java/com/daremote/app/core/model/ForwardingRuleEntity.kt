package com.daremote.app.core.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "forwarding_rules",
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
data class ForwardingRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val serverId: Long,
    val type: String,
    val name: String,
    val localHost: String = "127.0.0.1",
    val localPort: Int,
    val remoteHost: String? = null,
    val remotePort: Int? = null,
    val autoConnect: Boolean = false,
    val isActive: Boolean = false,
    val createdAt: Long
)
