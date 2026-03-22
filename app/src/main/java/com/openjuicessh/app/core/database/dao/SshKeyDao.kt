package com.openjuicessh.app.core.database.dao

import androidx.room.*
import com.openjuicessh.app.core.model.SshKeyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SshKeyDao {
    @Query("SELECT * FROM ssh_keys ORDER BY name ASC")
    fun getAllKeys(): Flow<List<SshKeyEntity>>

    @Query("SELECT * FROM ssh_keys WHERE id = :id")
    suspend fun getKeyById(id: Long): SshKeyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKey(key: SshKeyEntity): Long

    @Update
    suspend fun updateKey(key: SshKeyEntity)

    @Delete
    suspend fun deleteKey(key: SshKeyEntity)
}
