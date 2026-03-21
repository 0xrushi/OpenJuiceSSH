package com.daremote.app.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "snippets")
data class SnippetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val command: String,
    val description: String? = null,
    val groupId: Long? = null,
    val createdAt: Long,
    val lastUsedAt: Long? = null
)
