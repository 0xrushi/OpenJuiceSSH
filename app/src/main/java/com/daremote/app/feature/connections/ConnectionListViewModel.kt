package com.daremote.app.feature.connections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daremote.app.core.data.ssh.SshCommandExecutor
import com.daremote.app.core.domain.model.ConnectionStatus
import com.daremote.app.core.domain.model.Server
import com.daremote.app.core.domain.model.ServerGroup
import com.daremote.app.core.domain.repository.ServerRepository
import com.daremote.app.core.domain.repository.SshConnectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ServerCardStats(
    val cpuPercent: Float = 0f,
    val memUsedMb: Long = 0,
    val memTotalMb: Long = 0,
    val diskUsedGb: Float = 0f,
    val diskTotalGb: Float = 0f,
    val networkTxRate: Long = 0,
    val networkRxRate: Long = 0,
    val uptime: String = "0",
    val users: Int = 0,
    val loadAvg1: Float = 0f,
    val loadAvg5: Float = 0f,
    val loadAvg15: Float = 0f,
    val osName: String = ""
)

data class ConnectionListState(
    val servers: List<Server> = emptyList(),
    val groups: List<ServerGroup> = emptyList(),
    val connectionStatuses: Map<Long, ConnectionStatus> = emptyMap(),
    val selectedGroupId: Long? = null,
    val serverStats: Map<Long, ServerCardStats> = emptyMap(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val snackbarMessage: String? = null
)

@HiltViewModel
class ConnectionListViewModel @Inject constructor(
    private val serverRepository: ServerRepository,
    private val sshConnectionRepository: SshConnectionRepository,
    private val commandExecutor: SshCommandExecutor
) : ViewModel() {

    private val _selectedGroupId = MutableStateFlow<Long?>(null)
    private val _serverStats = MutableStateFlow<Map<Long, ServerCardStats>>(emptyMap())
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    private val monitoringJobs = mutableMapOf<Long, Job>()
    private val prevNetBytes = mutableMapOf<Long, Pair<Long, Long>>()
    private val prevNetTime = mutableMapOf<Long, Long>()
    // cpu fields: [user, nice, system, idle, iowait, irq, softirq, steal]
    private val prevCpuJiffies = mutableMapOf<Long, LongArray>()

    val state: StateFlow<ConnectionListState> = combine(
        serverRepository.getAllServers(),
        serverRepository.getAllGroups(),
        _selectedGroupId,
        sshConnectionRepository.getConnectionStatuses(),
        combine(_serverStats, _snackbarMessage) { a, b -> a to b }
    ) { servers, groups, selectedGroup, statuses, (stats, snackbar) ->
        val filteredServers = if (selectedGroup != null) {
            servers.filter { it.groupId == selectedGroup }
        } else servers

        ConnectionListState(
            servers = filteredServers,
            groups = groups,
            connectionStatuses = statuses,
            selectedGroupId = selectedGroup,
            serverStats = stats,
            isLoading = false,
            snackbarMessage = snackbar
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ConnectionListState())

    init {
        viewModelScope.launch {
            sshConnectionRepository.getConnectionStatuses().collect { statuses ->
                val connectedIds = statuses.filter { it.value == ConnectionStatus.CONNECTED }.keys
                connectedIds.filter { !monitoringJobs.containsKey(it) }.forEach { startMonitoringForServer(it) }
                monitoringJobs.keys.filter { it !in connectedIds }.forEach { stopMonitoringForServer(it) }
            }
        }
    }

    private fun startMonitoringForServer(serverId: Long) {
        monitoringJobs[serverId] = viewModelScope.launch(Dispatchers.IO) {
            val osName = commandExecutor.execute(
                serverId,
                "grep '^NAME=' /etc/os-release 2>/dev/null | cut -d'=' -f2 | tr -d '\"' || uname -o 2>/dev/null || echo ''"
            ).getOrNull()?.trim() ?: ""

            while (isActive) {
                try {
                    val stats = fetchServerStats(serverId, osName)
                    _serverStats.update { it + (serverId to stats) }
                } catch (_: Exception) {}
                delay(5000)
            }
        }
    }

    private fun stopMonitoringForServer(serverId: Long) {
        monitoringJobs[serverId]?.cancel()
        monitoringJobs.remove(serverId)
        prevNetBytes.remove(serverId)
        prevNetTime.remove(serverId)
        prevCpuJiffies.remove(serverId)
        _serverStats.update { it - serverId }
    }

    private suspend fun fetchServerStats(serverId: Long, osName: String): ServerCardStats {
        val loadAvgRaw = commandExecutor.execute(serverId, "cat /proc/loadavg").getOrNull()?.trim() ?: ""
        val loads = loadAvgRaw.split(" ").take(3).map { it.toFloatOrNull() ?: 0f }

        val cpuPercent = parseCpuPercent(serverId)

        val memRaw = commandExecutor.execute(serverId, "free -m | grep '^Mem:'").getOrNull()?.trim() ?: ""
        val memParts = memRaw.split(Regex("\\s+"))
        val memTotal = memParts.getOrNull(1)?.toLongOrNull() ?: 0
        val memUsed = memParts.getOrNull(2)?.toLongOrNull() ?: 0

        val dfRaw = commandExecutor.execute(serverId, "df -BG / | tail -1").getOrNull()?.trim() ?: ""
        val dfParts = dfRaw.split(Regex("\\s+"))
        val diskTotal = dfParts.getOrNull(1)?.removeSuffix("G")?.toFloatOrNull() ?: 0f
        val diskUsed = dfParts.getOrNull(2)?.removeSuffix("G")?.toFloatOrNull() ?: 0f

        val netDevRaw = commandExecutor.execute(serverId, "cat /proc/net/dev").getOrNull() ?: ""
        val (rxBytes, txBytes) = parseNetworkBytes(netDevRaw)
        val now = System.currentTimeMillis()
        val prevBytes = prevNetBytes[serverId]
        val prevTime = prevNetTime[serverId]
        var rxRate = 0L
        var txRate = 0L
        if (prevBytes != null && prevTime != null) {
            val elapsed = (now - prevTime) / 1000f
            if (elapsed > 0) {
                rxRate = ((rxBytes - prevBytes.first) / elapsed).toLong().coerceAtLeast(0)
                txRate = ((txBytes - prevBytes.second) / elapsed).toLong().coerceAtLeast(0)
            }
        }
        prevNetBytes[serverId] = Pair(rxBytes, txBytes)
        prevNetTime[serverId] = now

        val uptimeRaw = commandExecutor.execute(serverId, "uptime").getOrNull()?.trim() ?: ""
        val uptime = parseUptimeShort(uptimeRaw)
        val users = Regex("""(\d+)\s+users?""").find(uptimeRaw)?.groupValues?.get(1)?.toIntOrNull() ?: 0

        return ServerCardStats(
            cpuPercent = cpuPercent,
            memUsedMb = memUsed,
            memTotalMb = memTotal,
            diskUsedGb = diskUsed,
            diskTotalGb = diskTotal,
            networkTxRate = txRate,
            networkRxRate = rxRate,
            uptime = uptime,
            users = users,
            loadAvg1 = loads.getOrElse(0) { 0f },
            loadAvg5 = loads.getOrElse(1) { 0f },
            loadAvg15 = loads.getOrElse(2) { 0f },
            osName = osName
        )
    }

    private suspend fun parseCpuPercent(serverId: Long): Float {
        val statLine = commandExecutor.execute(serverId, "head -1 /proc/stat")
            .getOrNull()?.trim() ?: return 0f
        // format: "cpu  user nice system idle iowait irq softirq steal ..."
        val fields = statLine.removePrefix("cpu").trim().split(Regex("\\s+"))
        if (fields.size < 4) return 0f
        val cur = LongArray(8) { fields.getOrNull(it)?.toLongOrNull() ?: 0L }
        val curIdle = cur[3] + cur[4] // idle + iowait
        val curTotal = cur.sum()

        val prev = prevCpuJiffies[serverId]
        prevCpuJiffies[serverId] = cur

        if (prev == null) return 0f
        val prevIdle = prev[3] + prev[4]
        val prevTotal = prev.sum()
        val deltaTotal = curTotal - prevTotal
        val deltaIdle = curIdle - prevIdle
        if (deltaTotal <= 0) return 0f
        return ((deltaTotal - deltaIdle).toFloat() / deltaTotal * 100f).coerceIn(0f, 100f)
    }

    private fun parseNetworkBytes(netDev: String): Pair<Long, Long> {
        var rxTotal = 0L
        var txTotal = 0L
        netDev.lines().drop(2).forEach { line ->
            val colonIdx = line.indexOf(':')
            if (colonIdx < 0) return@forEach
            val iface = line.substring(0, colonIdx).trim()
            if (iface == "lo") return@forEach
            val fields = line.substring(colonIdx + 1).trim().split(Regex("\\s+"))
            rxTotal += fields.getOrNull(0)?.toLongOrNull() ?: 0
            txTotal += fields.getOrNull(8)?.toLongOrNull() ?: 0
        }
        return Pair(rxTotal, txTotal)
    }

    private fun parseUptimeShort(uptime: String): String {
        val daysMatch = Regex("""(\d+)\s+days?""").find(uptime)
        val hoursMatch = Regex("""up\s+(\d+):(\d+)""").find(uptime)
        val minsMatch = Regex("""(\d+)\s+min""").find(uptime)
        return when {
            daysMatch != null -> "${daysMatch.groupValues[1]}+ D"
            hoursMatch != null -> "${hoursMatch.groupValues[1]} H"
            minsMatch != null -> "${minsMatch.groupValues[1]} M"
            else -> "0"
        }
    }

    fun selectGroup(groupId: Long?) {
        _selectedGroupId.value = groupId
    }

    fun connect(server: Server) {
        viewModelScope.launch {
            val result = sshConnectionRepository.connect(server)
            result.onSuccess {
                serverRepository.updateLastConnected(server.id)
            }.onFailure { e ->
                _snackbarMessage.value = "${server.name}: ${e.message}"
            }
        }
    }

    fun disconnect(serverId: Long) {
        viewModelScope.launch {
            try {
                sshConnectionRepository.disconnect(serverId)
            } catch (e: Exception) {
                _snackbarMessage.value = "Failed to disconnect: ${e.message}"
            }
        }
    }

    fun deleteServer(serverId: Long) {
        viewModelScope.launch {
            try {
                sshConnectionRepository.disconnect(serverId)
                serverRepository.deleteServer(serverId)
            } catch (e: Exception) {
                _snackbarMessage.value = "Failed to delete server: ${e.message}"
            }
        }
    }

    fun snackbarShown() {
        _snackbarMessage.value = null
    }
}
