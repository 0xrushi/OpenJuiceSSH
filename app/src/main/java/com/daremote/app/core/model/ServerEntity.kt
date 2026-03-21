package com.daremote.app.core.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "servers",
    foreignKeys = [
        ForeignKey(
            entity = ServerGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("groupId")]
)
data class ServerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val host: String,
    val port: Int = 22,
    val username: String,
    val authType: String,
    val credentialRef: String,
    val sshKeyId: Long? = null,
    val groupId: Long? = null,
    val fingerprint: String? = null,
    val createdAt: Long,
    val lastConnectedAt: Long? = null,
    val proxyId: Long? = null,
    val sortOrder: Int = 0
)
