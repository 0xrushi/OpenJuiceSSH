package com.daremote.app.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.daremote.app.core.model.AlertRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertRuleDao {

    @Query("SELECT * FROM alert_rules ORDER BY serverId ASC")
    fun getAll(): Flow<List<AlertRuleEntity>>

    @Query("SELECT * FROM alert_rules WHERE serverId = :serverId")
    fun getByServer(serverId: Long): Flow<List<AlertRuleEntity>>

    @Query("SELECT * FROM alert_rules WHERE isEnabled = 1")
    suspend fun getEnabled(): List<AlertRuleEntity>

    @Query("SELECT * FROM alert_rules WHERE id = :id")
    suspend fun getById(id: Long): AlertRuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: AlertRuleEntity): Long

    @Update
    suspend fun update(rule: AlertRuleEntity)

    @Delete
    suspend fun delete(rule: AlertRuleEntity)

    @Query("UPDATE alert_rules SET lastCheckedAt = :timestamp, lastStatus = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, timestamp: Long, status: String)
}
