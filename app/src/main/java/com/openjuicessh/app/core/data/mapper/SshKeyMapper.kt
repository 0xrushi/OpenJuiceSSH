package com.openjuicessh.app.core.data.mapper

import com.openjuicessh.app.core.domain.model.KeyType
import com.openjuicessh.app.core.domain.model.SshKey
import com.openjuicessh.app.core.model.SshKeyEntity

fun SshKeyEntity.toDomain(): SshKey = SshKey(
    id = id,
    name = name,
    type = KeyType.valueOf(type),
    publicKey = publicKey,
    privateKeyRef = privateKeyRef,
    hasPassphrase = hasPassphrase,
    createdAt = createdAt
)

fun SshKey.toEntity(): SshKeyEntity = SshKeyEntity(
    id = id,
    name = name,
    type = type.name,
    publicKey = publicKey,
    privateKeyRef = privateKeyRef,
    hasPassphrase = hasPassphrase,
    createdAt = createdAt
)
