package com.daremote.app.core.data.repository

import com.daremote.app.core.data.mapper.toDomain
import com.daremote.app.core.data.mapper.toEntity
import com.daremote.app.core.database.dao.ProxyDao
import com.daremote.app.core.domain.model.Proxy
import com.daremote.app.core.domain.repository.ProxyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProxyRepositoryImpl @Inject constructor(
    private val proxyDao: ProxyDao
) : ProxyRepository {
    override fun getAllProxies(): Flow<List<Proxy>> {
        return proxyDao.getAllProxies().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getProxyById(id: Long): Proxy? {
        return proxyDao.getProxyById(id)?.toDomain()
    }

    override suspend fun saveProxy(proxy: Proxy): Long {
        val entity = proxy.toEntity()
        return if (proxy.id == 0L) {
            proxyDao.insertProxy(entity)
        } else {
            proxyDao.updateProxy(entity)
            proxy.id
        }
    }

    override suspend fun deleteProxy(proxy: Proxy) {
        proxyDao.deleteProxy(proxy.toEntity())
    }
}
