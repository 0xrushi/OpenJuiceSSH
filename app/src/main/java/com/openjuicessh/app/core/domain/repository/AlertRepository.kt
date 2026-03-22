package com.openjuicessh.app.core.domain.repository

import com.openjuicessh.app.core.domain.model.AlertRule
import kotlinx.coroutines.flow.Flow

interface AlertRepository {
    fun getAllRules(): Flow<List<AlertRule>>
    fun getRulesByServer(serverId: Long): Flow<List<AlertRule>>
    suspend fun getEnabledRules(): List<AlertRule>
    suspend fun getRuleById(id: Long): AlertRule?
    suspend fun saveRule(rule: AlertRule): Long
    suspend fun updateRule(rule: AlertRule)
    suspend fun deleteRule(rule: AlertRule)
    suspend fun updateStatus(id: Long, status: String)
}
