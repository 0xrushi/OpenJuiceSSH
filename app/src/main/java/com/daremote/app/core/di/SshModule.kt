package com.daremote.app.core.di

import com.daremote.app.core.data.ssh.SshSessionManager
import com.daremote.app.core.security.CredentialManager
import com.daremote.app.core.security.SshKeyManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SshModule {

    @Provides
    @Singleton
    fun provideSshSessionManager(
        credentialManager: CredentialManager,
        sshKeyManager: SshKeyManager
    ): SshSessionManager = SshSessionManager(credentialManager, sshKeyManager)
}
