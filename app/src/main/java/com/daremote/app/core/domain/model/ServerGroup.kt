package com.daremote.app.core.domain.model

data class ServerGroup(
    val id: Long = 0,
    val name: String,
    val color: Int,
    val sortOrder: Int = 0
)
