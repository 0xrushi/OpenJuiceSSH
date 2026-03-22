package com.openjuicessh.app.core.domain.model

data class RemoteFile(
    val path: String,
    val name: String,
    val size: Long,
    val permissions: String,
    val modifiedAt: Long,
    val isDirectory: Boolean,
    val owner: String,
    val group: String
)
