package com.daremote.app.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.daremote.app.core.model.ServerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerDao {

    @Query("SELECT * FROM servers ORDER BY sortOrder ASC, name ASC")
    fun getAll(): Flow<List<ServerEntity>>

    @Query("SELECT * FROM servers WHERE groupId = :groupId ORDER BY sortOrder ASC")
    fun getByGroup(groupId: Long): Flow<List<ServerEntity>>

    @Query("SELECT * FROM servers WHERE id = :id")
    suspend fun getById(id: Long): ServerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(server: ServerEntity): Long

    @Update
    suspend fun update(server: ServerEntity)

    @Delete
    suspend fun delete(server: ServerEntity)

    @Query("DELETE FROM servers WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE servers SET lastConnectedAt = :timestamp WHERE id = :id")
    suspend fun updateLastConnected(id: Long, timestamp: Long)
}
