package com.daremote.app.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "snippet_groups")
data class SnippetGroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sortOrder: Int = 0
)
