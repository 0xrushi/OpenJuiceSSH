package com.openjuicessh.app.core.domain.repository

import com.openjuicessh.app.core.domain.model.Snippet
import com.openjuicessh.app.core.domain.model.SnippetGroup
import kotlinx.coroutines.flow.Flow

interface SnippetRepository {
    fun getAllSnippets(): Flow<List<Snippet>>
    fun getSnippetsByGroup(groupId: Long): Flow<List<Snippet>>
    suspend fun getSnippetById(id: Long): Snippet?
    suspend fun saveSnippet(snippet: Snippet): Long
    suspend fun updateSnippet(snippet: Snippet)
    suspend fun deleteSnippet(snippet: Snippet)
    suspend fun markUsed(id: Long)

    fun getAllGroups(): Flow<List<SnippetGroup>>
    suspend fun saveGroup(group: SnippetGroup): Long
    suspend fun deleteGroup(group: SnippetGroup)
}
