package com.daremote.app.core.data.repository

import com.daremote.app.core.data.mapper.toDomain
import com.daremote.app.core.data.mapper.toEntity
import com.daremote.app.core.database.dao.ServerDao
import com.daremote.app.core.database.dao.ServerGroupDao
import com.daremote.app.core.domain.model.Server
import com.daremote.app.core.domain.model.ServerGroup
import com.daremote.app.core.domain.repository.ServerRepository
import com.daremote.app.core.security.CredentialManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServerRepositoryImpl @Inject constructor(
    private val serverDao: ServerDao,
    private val serverGroupDao: ServerGroupDao,
    private val credentialManager: CredentialManager
) : ServerRepository {

    override fun getAllServers(): Flow<List<Server>> =
        serverDao.getAll().map { entities -> entities.map { it.toDomain() } }

    override fun getServersByGroup(groupId: Long): Flow<List<Server>> =
        serverDao.getByGroup(groupId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getServerById(id: Long): Server? =
        serverDao.getById(id)?.toDomain()

    override suspend fun saveServer(server: Server, password: String?): Long {
        val credRef = if (password != null) {
            credentialManager.store(password)
        } else {
            server.credentialRef
        }
        val entity = server.copy(credentialRef = credRef).toEntity()
        return serverDao.insert(entity)
    }

    override suspend fun updateServer(server: Server, password: String?) {
        if (password != null) {
            credentialManager.update(server.credentialRef, password)
        }
        serverDao.update(server.toEntity())
    }

    override suspend fun deleteServer(id: Long) {
        val server = serverDao.getById(id) ?: return
        credentialManager.delete(server.credentialRef)
        serverDao.deleteById(id)
    }

    override suspend fun updateLastConnected(id: Long) {
        serverDao.updateLastConnected(id, System.currentTimeMillis())
    }

    override fun getAllGroups(): Flow<List<ServerGroup>> =
        serverGroupDao.getAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun saveGroup(group: ServerGroup): Long =
        serverGroupDao.insert(group.toEntity())

    override suspend fun deleteGroup(group: ServerGroup) =
        serverGroupDao.delete(group.toEntity())
}
