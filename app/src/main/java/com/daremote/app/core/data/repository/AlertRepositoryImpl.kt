package com.daremote.app.core.data.repository

import com.daremote.app.core.database.dao.AlertRuleDao
import com.daremote.app.core.domain.model.AlertRule
import com.daremote.app.core.domain.model.AlertStatus
import com.daremote.app.core.domain.model.AlertType
import com.daremote.app.core.domain.repository.AlertRepository
import com.daremote.app.core.model.AlertRuleEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertRepositoryImpl @Inject constructor(
    private val dao: AlertRuleDao
) : AlertRepository {

    override fun getAllRules(): Flow<List<AlertRule>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override fun getRulesByServer(serverId: Long): Flow<List<AlertRule>> =
        dao.getByServer(serverId).map { list -> list.map { it.toDomain() } }

    override suspend fun getEnabledRules(): List<AlertRule> =
        dao.getEnabled().map { it.toDomain() }

    override suspend fun getRuleById(id: Long): AlertRule? =
        dao.getById(id)?.toDomain()

    override suspend fun saveRule(rule: AlertRule): Long =
        dao.insert(rule.toEntity())

    override suspend fun updateRule(rule: AlertRule) =
        dao.update(rule.toEntity())

    override suspend fun deleteRule(rule: AlertRule) =
        dao.delete(rule.toEntity())

    override suspend fun updateStatus(id: Long, status: String) =
        dao.updateStatus(id, System.currentTimeMillis(), status)

    private fun AlertRuleEntity.toDomain() = AlertRule(
        id = id, serverId = serverId,
        type = AlertType.valueOf(type),
        threshold = threshold,
        checkIntervalMinutes = checkIntervalMinutes,
        isEnabled = isEnabled,
        lastCheckedAt = lastCheckedAt,
        lastStatus = lastStatus?.let { AlertStatus.valueOf(it) }
    )

    private fun AlertRule.toEntity() = AlertRuleEntity(
        id = id, serverId = serverId,
        type = type.name,
        threshold = threshold,
        checkIntervalMinutes = checkIntervalMinutes,
        isEnabled = isEnabled,
        lastCheckedAt = lastCheckedAt,
        lastStatus = lastStatus?.name
    )
}
