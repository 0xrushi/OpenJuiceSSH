package com.daremote.app.core.domain.repository

import com.daremote.app.core.domain.model.Proxy
import kotlinx.coroutines.flow.Flow

interface ProxyRepository {
    fun getAllProxies(): Flow<List<Proxy>>
    suspend fun getProxyById(id: Long): Proxy?
    suspend fun saveProxy(proxy: Proxy): Long
    suspend fun deleteProxy(proxy: Proxy)
}
