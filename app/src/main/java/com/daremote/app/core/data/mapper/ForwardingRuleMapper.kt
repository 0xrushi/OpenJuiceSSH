package com.daremote.app.core.data.mapper

import com.daremote.app.core.domain.model.ForwardingRule
import com.daremote.app.core.domain.model.ForwardingType
import com.daremote.app.core.model.ForwardingRuleEntity

fun ForwardingRuleEntity.toDomain(): ForwardingRule = ForwardingRule(
    id = id,
    serverId = serverId,
    type = ForwardingType.valueOf(type),
    name = name,
    localHost = localHost,
    localPort = localPort,
    remoteHost = remoteHost,
    remotePort = remotePort,
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
    autoConnect = autoConnect,
    isActive = isActive,
    createdAt = createdAt
)
