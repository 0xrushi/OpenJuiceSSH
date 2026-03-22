package com.openjuicessh.app.core.data.mapper

import com.openjuicessh.app.core.domain.model.AuthType
import com.openjuicessh.app.core.domain.model.Server
import com.openjuicessh.app.core.domain.model.ServerGroup
import com.openjuicessh.app.core.model.ServerEntity
import com.openjuicessh.app.core.model.ServerGroupEntity

fun ServerEntity.toDomain(): Server = Server(
    id = id,
    name = name,
    host = host,
    port = port,
    username = username,
    authType = AuthType.valueOf(authType),
    credentialRef = credentialRef,
    sshKeyId = sshKeyId,
    groupId = groupId,
    fingerprint = fingerprint,
    createdAt = createdAt,
    lastConnectedAt = lastConnectedAt,
    proxyId = proxyId,
    sortOrder = sortOrder
)

fun Server.toEntity(): ServerEntity = ServerEntity(
    id = id,
    name = name,
    host = host,
    port = port,
    username = username,
    authType = authType.name,
    credentialRef = credentialRef,
    sshKeyId = sshKeyId,
    groupId = groupId,
    fingerprint = fingerprint,
    createdAt = createdAt,
    lastConnectedAt = lastConnectedAt,
    proxyId = proxyId,
    sortOrder = sortOrder
)

fun ServerGroupEntity.toDomain(): ServerGroup = ServerGroup(
    id = id,
    name = name,
    color = color,
    sortOrder = sortOrder
)

fun ServerGroup.toEntity(): ServerGroupEntity = ServerGroupEntity(
    id = id,
    name = name,
    color = color,
    sortOrder = sortOrder
)
