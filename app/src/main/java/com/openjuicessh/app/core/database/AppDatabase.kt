package com.openjuicessh.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.openjuicessh.app.core.database.dao.AlertRuleDao
import com.openjuicessh.app.core.database.dao.ForwardingRuleDao
import com.openjuicessh.app.core.database.dao.ProxyDao
import com.openjuicessh.app.core.database.dao.ServerDao
import com.openjuicessh.app.core.database.dao.ServerGroupDao
import com.openjuicessh.app.core.database.dao.SnippetDao
import com.openjuicessh.app.core.database.dao.SshKeyDao
import com.openjuicessh.app.core.model.AlertRuleEntity
import com.openjuicessh.app.core.model.ForwardingRuleEntity
import com.openjuicessh.app.core.model.ProxyEntity
import com.openjuicessh.app.core.model.ServerEntity
import com.openjuicessh.app.core.model.ServerGroupEntity
import com.openjuicessh.app.core.model.SnippetEntity
import com.openjuicessh.app.core.model.SnippetGroupEntity
import com.openjuicessh.app.core.model.SshKeyEntity

@Database(
    entities = [
        ServerEntity::class,
        ServerGroupEntity::class,
        SshKeyEntity::class,
        SnippetEntity::class,
        SnippetGroupEntity::class,
        ForwardingRuleEntity::class,
        AlertRuleEntity::class,
        ProxyEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serverDao(): ServerDao
    abstract fun serverGroupDao(): ServerGroupDao
    abstract fun snippetDao(): SnippetDao
    abstract fun forwardingRuleDao(): ForwardingRuleDao
    abstract fun alertRuleDao(): AlertRuleDao
    abstract fun proxyDao(): ProxyDao
    abstract fun sshKeyDao(): SshKeyDao
}
