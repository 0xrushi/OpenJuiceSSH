package com.daremote.app.core.data.repository

import com.daremote.app.core.data.mapper.toDomain
import com.daremote.app.core.data.mapper.toEntity
import com.daremote.app.core.database.dao.SnippetDao
import com.daremote.app.core.domain.model.Snippet
import com.daremote.app.core.domain.model.SnippetGroup
import com.daremote.app.core.domain.repository.SnippetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SnippetRepositoryImpl @Inject constructor(
    private val dao: SnippetDao
) : SnippetRepository {

    override fun getAllSnippets(): Flow<List<Snippet>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override fun getSnippetsByGroup(groupId: Long): Flow<List<Snippet>> =
        dao.getByGroup(groupId).map { list -> list.map { it.toDomain() } }

    override suspend fun getSnippetById(id: Long): Snippet? =
        dao.getById(id)?.toDomain()

    override suspend fun saveSnippet(snippet: Snippet): Long =
        dao.insert(snippet.toEntity())

    override suspend fun updateSnippet(snippet: Snippet) =
        dao.update(snippet.toEntity())

    override suspend fun deleteSnippet(snippet: Snippet) =
        dao.delete(snippet.toEntity())

    override suspend fun markUsed(id: Long) =
        dao.updateLastUsed(id, System.currentTimeMillis())

    override fun getAllGroups(): Flow<List<SnippetGroup>> =
        dao.getAllGroups().map { list -> list.map { it.toDomain() } }

    override suspend fun saveGroup(group: SnippetGroup): Long =
        dao.insertGroup(group.toEntity())

    override suspend fun deleteGroup(group: SnippetGroup) =
        dao.deleteGroup(group.toEntity())
}
