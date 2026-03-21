package com.daremote.app.feature.monitoring

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daremote.app.core.data.ssh.SshCommandExecutor
import com.daremote.app.core.domain.model.CpuStats
import com.daremote.app.core.domain.model.DiskStats
import com.daremote.app.core.domain.model.MemoryStats
import com.daremote.app.core.domain.model.SystemStats
import com.daremote.app.core.domain.repository.ServerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardState(
    val serverName: String = "",
    val serverId: Long = 0,
    val stats: SystemStats = SystemStats(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val commandExecutor: SshCommandExecutor,
    private val serverRepository: ServerRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val serverId: Long = savedStateHandle["serverId"] ?: 0L
    private val _state = MutableStateFlow(DashboardState(serverId = serverId))
    val state: StateFlow<DashboardState> = _state

    init {
        viewModelScope.launch {
            val server = serverRepository.getServerById(serverId)
            _state.update { it.copy(serverName = server?.name ?: "Unknown") }
        }
        startMonitoring()
    }

    private fun startMonitoring() {
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    val cpu = parseCpuStats()
                    val memory = parseMemoryStats()
                    val disks = parseDiskStats()
                    val uptime = commandExecutor.execute(serverId, "uptime -p").getOrNull()?.trim()

                    _state.update {
                        it.copy(
                            stats = SystemStats(cpu = cpu, memory = memory, disks = disks, uptime = uptime),
                            isLoading = false,
                            error = null
                        )
                    }
                } catch (e: Exception) {
                    _state.update { it.copy(error = e.message, isLoading = false) }
                }
                delay(3000)
            }
        }
    }

    private suspend fun parseCpuStats(): CpuStats? {
        val top = commandExecutor.execute(serverId, "top -bn1 | head -5").getOrNull() ?: return null
        val nproc = commandExecutor.execute(serverId, "nproc").getOrNull()?.trim()?.toIntOrNull() ?: 1
        val loadAvg = commandExecutor.execute(serverId, "cat /proc/loadavg").getOrNull()?.trim()

        val cpuLine = top.lines().find { it.contains("%Cpu") || it.contains("Cpu(s)") }
        val idle = cpuLine?.let {
            Regex("""(\d+\.?\d*)\s*id""").find(it)?.groupValues?.get(1)?.toFloatOrNull()
        } ?: 0f

        val loads = loadAvg?.split(" ")?.take(3)?.map { it.toFloatOrNull() ?: 0f } ?: listOf(0f, 0f, 0f)

        return CpuStats(
            usagePercent = 100f - idle,
            cores = nproc,
            loadAvg1 = loads.getOrElse(0) { 0f },
            loadAvg5 = loads.getOrElse(1) { 0f },
            loadAvg15 = loads.getOrElse(2) { 0f }
        )
    }

    private suspend fun parseMemoryStats(): MemoryStats? {
        val free = commandExecutor.execute(serverId, "free -m").getOrNull() ?: return null
        val memLine = free.lines().find { it.startsWith("Mem:") } ?: return null
        val swapLine = free.lines().find { it.startsWith("Swap:") }

        val memParts = memLine.trim().split(Regex("\\s+"))
        val swapParts = swapLine?.trim()?.split(Regex("\\s+"))

        return MemoryStats(
            totalMb = memParts.getOrNull(1)?.toLongOrNull() ?: 0,
            usedMb = memParts.getOrNull(2)?.toLongOrNull() ?: 0,
            freeMb = memParts.getOrNull(3)?.toLongOrNull() ?: 0,
            swapTotalMb = swapParts?.getOrNull(1)?.toLongOrNull() ?: 0,
            swapUsedMb = swapParts?.getOrNull(2)?.toLongOrNull() ?: 0
        )
    }

    private suspend fun parseDiskStats(): List<DiskStats> {
        val df = commandExecutor.execute(serverId, "df -h --output=target,size,used,pcent")
            .getOrNull() ?: return emptyList()

        return df.lines().drop(1).filter { it.isNotBlank() }.mapNotNull { line ->
            val parts = line.trim().split(Regex("\\s+"))
            if (parts.size >= 4) {
                DiskStats(
                    mountPoint = parts[0],
                    totalGb = parseSize(parts[1]),
                    usedGb = parseSize(parts[2]),
                    usedPercent = parts[3].removeSuffix("%").toFloatOrNull() ?: 0f
                )
            } else null
        }
    }

    private fun parseSize(s: String): Float {
        val num = s.filter { it.isDigit() || it == '.' }.toFloatOrNull() ?: 0f
        return when {
            s.endsWith("T") -> num * 1024
            s.endsWith("G") -> num
            s.endsWith("M") -> num / 1024
            else -> num
        }
    }
}
