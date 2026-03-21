package com.daremote.app.core.data.repository

import com.daremote.app.core.data.mapper.toDomain
import com.daremote.app.core.data.mapper.toEntity
import com.daremote.app.core.database.dao.SshKeyDao
import com.daremote.app.core.domain.model.SshKey
import com.daremote.app.core.domain.repository.SshKeyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SshKeyRepositoryImpl @Inject constructor(
    private val sshKeyDao: SshKeyDao
) : SshKeyRepository {
    override fun getAllKeys(): Flow<List<SshKey>> {
        return sshKeyDao.getAllKeys().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getKeyById(id: Long): SshKey? {
        return sshKeyDao.getKeyById(id)?.toDomain()
    }

    override suspend fun saveKey(key: SshKey): Long {
        val entity = key.toEntity()
        return if (key.id == 0L) {
            sshKeyDao.insertKey(entity)
        } else {
            sshKeyDao.updateKey(entity)
            key.id
        }
    }

    override suspend fun deleteKey(key: SshKey) {
        sshKeyDao.deleteKey(key.toEntity())
    }
}
