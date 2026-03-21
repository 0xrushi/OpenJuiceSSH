package com.daremote.app.core.domain.repository

import com.daremote.app.core.domain.model.Server
import com.daremote.app.core.domain.model.ServerGroup
import kotlinx.coroutines.flow.Flow

interface ServerRepository {
    fun getAllServers(): Flow<List<Server>>
    fun getServersByGroup(groupId: Long): Flow<List<Server>>
    suspend fun getServerById(id: Long): Server?
    suspend fun saveServer(server: Server, password: String? = null): Long
    suspend fun updateServer(server: Server, password: String? = null)
    suspend fun deleteServer(id: Long)
    suspend fun updateLastConnected(id: Long)

    fun getAllGroups(): Flow<List<ServerGroup>>
    suspend fun saveGroup(group: ServerGroup): Long
    suspend fun deleteGroup(group: ServerGroup)
}
