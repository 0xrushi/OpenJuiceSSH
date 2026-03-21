package com.daremote.app.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "proxies")
data class ProxyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String,
    val host: String,
    val port: Int,
    val username: String? = null,
    val authType: String,
    val password: String? = null,
    val sshKeyId: Long? = null
)
