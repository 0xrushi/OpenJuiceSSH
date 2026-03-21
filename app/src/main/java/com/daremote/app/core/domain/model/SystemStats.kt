package com.daremote.app.core.domain.model

data class SystemStats(
    val cpu: CpuStats? = null,
    val memory: MemoryStats? = null,
    val disks: List<DiskStats> = emptyList(),
    val network: List<NetworkStats> = emptyList(),
    val uptime: String? = null
)

data class CpuStats(
    val usagePercent: Float,
    val cores: Int,
    val loadAvg1: Float,
    val loadAvg5: Float,
    val loadAvg15: Float
)

data class MemoryStats(
    val totalMb: Long,
    val usedMb: Long,
    val freeMb: Long,
    val swapTotalMb: Long,
    val swapUsedMb: Long
)

data class DiskStats(
    val mountPoint: String,
    val totalGb: Float,
    val usedGb: Float,
    val usedPercent: Float
)

data class NetworkStats(
    val interfaceName: String,
    val rxBytes: Long,
    val txBytes: Long,
    val rxRate: Long = 0,
    val txRate: Long = 0
)

data class ProcessInfo(
    val pid: Int,
    val user: String,
    val cpuPercent: Float,
    val memPercent: Float,
    val command: String
)
