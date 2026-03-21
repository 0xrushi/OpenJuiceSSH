package com.daremote.app.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.daremote.app.core.model.ForwardingRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ForwardingRuleDao {

    @Query("SELECT * FROM forwarding_rules ORDER BY name ASC")
    fun getAll(): Flow<List<ForwardingRuleEntity>>

    @Query("SELECT * FROM forwarding_rules WHERE serverId = :serverId")
    fun getByServer(serverId: Long): Flow<List<ForwardingRuleEntity>>

    @Query("SELECT * FROM forwarding_rules WHERE autoConnect = 1")
    suspend fun getAutoConnect(): List<ForwardingRuleEntity>

    @Query("SELECT * FROM forwarding_rules WHERE id = :id")
    suspend fun getById(id: Long): ForwardingRuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: ForwardingRuleEntity): Long

    @Update
    suspend fun update(rule: ForwardingRuleEntity)

    @Delete
    suspend fun delete(rule: ForwardingRuleEntity)

    @Query("UPDATE forwarding_rules SET isActive = :active WHERE id = :id")
    suspend fun setActive(id: Long, active: Boolean)
}
