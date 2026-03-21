package com.daremote.app.core.data.mapper

import com.daremote.app.core.domain.model.KeyType
import com.daremote.app.core.domain.model.SshKey
import com.daremote.app.core.model.SshKeyEntity

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
