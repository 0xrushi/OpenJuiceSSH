package com.daremote.app.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "server_groups")
data class ServerGroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val color: Int,
    val sortOrder: Int = 0
)
