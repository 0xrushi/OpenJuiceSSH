package com.openjuicessh.app.core.domain.model

data class Snippet(
    val id: Long = 0,
    val name: String,
    val command: String,
    val description: String? = null,
    val groupId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsedAt: Long? = null
)

data class SnippetGroup(
    val id: Long = 0,
    val name: String,
    val sortOrder: Int = 0
)
