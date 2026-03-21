package com.daremote.app.core.domain.repository

import com.daremote.app.core.domain.model.SshKey
import kotlinx.coroutines.flow.Flow

interface SshKeyRepository {
    fun getAllKeys(): Flow<List<SshKey>>
    suspend fun getKeyById(id: Long): SshKey?
    suspend fun saveKey(key: SshKey): Long
    suspend fun deleteKey(key: SshKey)
}
