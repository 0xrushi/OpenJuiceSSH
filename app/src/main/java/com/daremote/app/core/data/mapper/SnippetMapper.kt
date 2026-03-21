package com.daremote.app.core.data.mapper

import com.daremote.app.core.domain.model.Snippet
import com.daremote.app.core.domain.model.SnippetGroup
import com.daremote.app.core.model.SnippetEntity
import com.daremote.app.core.model.SnippetGroupEntity

fun SnippetEntity.toDomain(): Snippet = Snippet(
    id = id,
    name = name,
    command = command,
    description = description,
    groupId = groupId,
    createdAt = createdAt,
    lastUsedAt = lastUsedAt
)

fun Snippet.toEntity(): SnippetEntity = SnippetEntity(
    id = id,
    name = name,
    command = command,
    description = description,
    groupId = groupId,
    createdAt = createdAt,
    lastUsedAt = lastUsedAt
)

fun SnippetGroupEntity.toDomain(): SnippetGroup = SnippetGroup(
    id = id,
    name = name,
    sortOrder = sortOrder
)

fun SnippetGroup.toEntity(): SnippetGroupEntity = SnippetGroupEntity(
    id = id,
    name = name,
    sortOrder = sortOrder
)
