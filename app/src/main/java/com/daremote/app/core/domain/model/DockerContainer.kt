package com.daremote.app.core.domain.model

data class DockerContainer(
    val id: String,
    val name: String,
    val image: String,
    val status: String,
    val state: ContainerState,
    val ports: String,
    val cpuPercent: Float = 0f,
    val memUsage: String = "",
    val createdAt: String = ""
)

enum class ContainerState {
    RUNNING,
    STOPPED,
    PAUSED,
    RESTARTING,
    DEAD,
    CREATED,
    UNKNOWN
}
