package com.openjuicessh.app.core.data.mapper

import com.openjuicessh.app.core.domain.model.ForwardingRule
import com.openjuicessh.app.core.domain.model.ForwardingType
import com.openjuicessh.app.core.model.ForwardingRuleEntity

fun ForwardingRuleEntity.toDomain(): ForwardingRule = ForwardingRule(
    id = id,
    serverId = serverId,
    type = ForwardingType.valueOf(type),
    name = name,
    localHost = localHost,
    localPort = localPort,
    remoteHost = remoteHost,
    remotePort = remotePort,
    proxyId = proxyId,
    autoConnect = autoConnect,
    isActive = isActive,
    createdAt = createdAt
)

fun ForwardingRule.toEntity(): ForwardingRuleEntity = ForwardingRuleEntity(
    id = id,
    serverId = serverId,
    type = type.name,
    name = name,
    localHost = localHost,
    localPort = localPort,
    remoteHost = remoteHost,
    remotePort = remotePort,
    proxyId = proxyId,
    autoConnect = autoConnect,
    isActive = isActive,
    createdAt = createdAt
)
