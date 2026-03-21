package com.daremote.app.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ssh_keys")
data class SshKeyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String,
    val publicKey: String,
    val privateKeyRef: String,
    val hasPassphrase: Boolean,
    val createdAt: Long
)
