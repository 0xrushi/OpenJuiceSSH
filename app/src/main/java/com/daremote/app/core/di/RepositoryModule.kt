package com.daremote.app.core.di

import com.daremote.app.core.data.repository.AlertRepositoryImpl
import com.daremote.app.core.data.repository.ForwardingRepositoryImpl
import com.daremote.app.core.data.repository.ProxyRepositoryImpl
import com.daremote.app.core.data.repository.ServerRepositoryImpl
import com.daremote.app.core.data.repository.SnippetRepositoryImpl
import com.daremote.app.core.data.repository.SshConnectionRepositoryImpl
import com.daremote.app.core.data.repository.SshKeyRepositoryImpl
import com.daremote.app.core.domain.repository.AlertRepository
import com.daremote.app.core.domain.repository.ForwardingRepository
import com.daremote.app.core.domain.repository.ProxyRepository
import com.daremote.app.core.domain.repository.ServerRepository
import com.daremote.app.core.domain.repository.SnippetRepository
import com.daremote.app.core.domain.repository.SshConnectionRepository
import com.daremote.app.core.domain.repository.SshKeyRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindServerRepository(impl: ServerRepositoryImpl): ServerRepository

    @Binds
    @Singleton
    abstract fun bindSshConnectionRepository(impl: SshConnectionRepositoryImpl): SshConnectionRepository

    @Binds
    @Singleton
    abstract fun bindForwardingRepository(impl: ForwardingRepositoryImpl): ForwardingRepository

    @Binds
    @Singleton
    abstract fun bindSnippetRepository(impl: SnippetRepositoryImpl): SnippetRepository

    @Binds
    @Singleton
    abstract fun bindAlertRepository(impl: AlertRepositoryImpl): AlertRepository

    @Binds
    @Singleton
    abstract fun bindProxyRepository(impl: ProxyRepositoryImpl): ProxyRepository

    @Binds
    @Singleton
    abstract fun bindSshKeyRepository(impl: SshKeyRepositoryImpl): SshKeyRepository
}
