package com.daremote.app.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.daremote.app.core.model.ServerGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerGroupDao {

    @Query("SELECT * FROM server_groups ORDER BY sortOrder ASC, name ASC")
    fun getAll(): Flow<List<ServerGroupEntity>>

    @Query("SELECT * FROM server_groups WHERE id = :id")
    suspend fun getById(id: Long): ServerGroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(group: ServerGroupEntity): Long

    @Update
    suspend fun update(group: ServerGroupEntity)

    @Delete
    suspend fun delete(group: ServerGroupEntity)
}
