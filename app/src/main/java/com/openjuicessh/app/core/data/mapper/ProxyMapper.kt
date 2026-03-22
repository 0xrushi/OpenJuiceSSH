package com.openjuicessh.app.core.data.mapper

import com.openjuicessh.app.core.domain.model.Proxy
import com.openjuicessh.app.core.domain.model.ProxyAuthType
import com.openjuicessh.app.core.domain.model.ProxyType
import com.openjuicessh.app.core.model.ProxyEntity

fun ProxyEntity.toDomain(): Proxy = Proxy(
    id = id,
    name = name,
    type = ProxyType.valueOf(type),
    host = host,
    port = port,
    username = username,
    authType = ProxyAuthType.valueOf(authType),
    password = password,
    sshKeyId = sshKeyId
)

fun Proxy.toEntity(): ProxyEntity = ProxyEntity(
    id = id,
    name = name,
    type = type.name,
    host = host,
    port = port,
    username = username,
    authType = authType.name,
    password = password,
    sshKeyId = sshKeyId
)
