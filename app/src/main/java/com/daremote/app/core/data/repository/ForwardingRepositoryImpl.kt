package com.daremote.app.core.data.repository

import com.daremote.app.core.data.mapper.toDomain
import com.daremote.app.core.data.mapper.toEntity
import com.daremote.app.core.database.dao.ForwardingRuleDao
import com.daremote.app.core.domain.model.ForwardingRule
import com.daremote.app.core.domain.repository.ForwardingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ForwardingRepositoryImpl @Inject constructor(
    private val dao: ForwardingRuleDao
) : ForwardingRepository {

    override fun getAllRules(): Flow<List<ForwardingRule>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override fun getRulesByServer(serverId: Long): Flow<List<ForwardingRule>> =
        dao.getByServer(serverId).map { list -> list.map { it.toDomain() } }

    override suspend fun getRuleById(id: Long): ForwardingRule? =
        dao.getById(id)?.toDomain()

    override suspend fun getAutoConnectRules(): List<ForwardingRule> =
        dao.getAutoConnect().map { it.toDomain() }

    override suspend fun saveRule(rule: ForwardingRule): Long =
        dao.insert(rule.toEntity())

    override suspend fun updateRule(rule: ForwardingRule) =
        dao.update(rule.toEntity())

    override suspend fun deleteRule(rule: ForwardingRule) =
        dao.delete(rule.toEntity())

    override suspend fun setActive(id: Long, active: Boolean) =
        dao.setActive(id, active)
}
