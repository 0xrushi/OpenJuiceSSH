package com.daremote.app.core.di

import android.content.Context
import androidx.room.Room
import com.daremote.app.core.database.AppDatabase
import com.daremote.app.core.database.dao.AlertRuleDao
import com.daremote.app.core.database.dao.ForwardingRuleDao
import com.daremote.app.core.database.dao.ProxyDao
import com.daremote.app.core.database.dao.ServerDao
import com.daremote.app.core.database.dao.ServerGroupDao
import com.daremote.app.core.database.dao.SnippetDao
import com.daremote.app.core.database.dao.SshKeyDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "daremote.db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideServerDao(db: AppDatabase): ServerDao = db.serverDao()

    @Provides
    fun provideServerGroupDao(db: AppDatabase): ServerGroupDao = db.serverGroupDao()

    @Provides
    fun provideSnippetDao(db: AppDatabase): SnippetDao = db.snippetDao()

    @Provides
    fun provideForwardingRuleDao(db: AppDatabase): ForwardingRuleDao = db.forwardingRuleDao()

    @Provides
    fun provideAlertRuleDao(db: AppDatabase): AlertRuleDao = db.alertRuleDao()

    @Provides
    fun provideProxyDao(db: AppDatabase): ProxyDao = db.proxyDao()

    @Provides
    fun provideSshKeyDao(db: AppDatabase): SshKeyDao = db.sshKeyDao()
}
