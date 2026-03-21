package com.daremote.app.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.daremote.app.core.model.SnippetEntity
import com.daremote.app.core.model.SnippetGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SnippetDao {

    @Query("SELECT * FROM snippets ORDER BY name ASC")
    fun getAll(): Flow<List<SnippetEntity>>

    @Query("SELECT * FROM snippets WHERE groupId = :groupId ORDER BY name ASC")
    fun getByGroup(groupId: Long): Flow<List<SnippetEntity>>

    @Query("SELECT * FROM snippets WHERE id = :id")
    suspend fun getById(id: Long): SnippetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(snippet: SnippetEntity): Long

    @Update
    suspend fun update(snippet: SnippetEntity)

    @Delete
    suspend fun delete(snippet: SnippetEntity)

    @Query("UPDATE snippets SET lastUsedAt = :timestamp WHERE id = :id")
    suspend fun updateLastUsed(id: Long, timestamp: Long)

    // Snippet Groups
    @Query("SELECT * FROM snippet_groups ORDER BY sortOrder ASC")
    fun getAllGroups(): Flow<List<SnippetGroupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: SnippetGroupEntity): Long

    @Delete
    suspend fun deleteGroup(group: SnippetGroupEntity)
}
