package com.openjuicessh.app.core.domain.repository

import com.openjuicessh.app.core.domain.model.ForwardingRule
import com.openjuicessh.app.core.domain.model.TunnelState
import kotlinx.coroutines.flow.Flow

interface ForwardingRepository {
    fun getAllRules(): Flow<List<ForwardingRule>>
    fun getRulesByServer(serverId: Long): Flow<List<ForwardingRule>>
    suspend fun getRuleById(id: Long): ForwardingRule?
    suspend fun getAutoConnectRules(): List<ForwardingRule>
    suspend fun saveRule(rule: ForwardingRule): Long
    suspend fun updateRule(rule: ForwardingRule)
    suspend fun deleteRule(rule: ForwardingRule)
    suspend fun setActive(id: Long, active: Boolean)
}
