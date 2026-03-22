package com.openjuicessh.app.core.domain.repository

import com.openjuicessh.app.core.domain.model.ProcessInfo
import com.openjuicessh.app.core.domain.model.SystemStats
import kotlinx.coroutines.flow.Flow

interface MonitoringRepository {
    fun getSystemStats(serverId: Long, intervalMs: Long = 3000): Flow<SystemStats>
    suspend fun getProcessList(serverId: Long): Result<List<ProcessInfo>>
    suspend fun killProcess(serverId: Long, pid: Int, signal: Int = 9): Result<Unit>
}
