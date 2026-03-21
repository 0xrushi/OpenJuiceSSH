package com.daremote.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.daremote.app.core.database.dao.AlertRuleDao
import com.daremote.app.core.database.dao.ForwardingRuleDao
import com.daremote.app.core.database.dao.ServerDao
import com.daremote.app.core.database.dao.ServerGroupDao
import com.daremote.app.core.database.dao.SnippetDao
import com.daremote.app.core.model.AlertRuleEntity
import com.daremote.app.core.model.ForwardingRuleEntity
import com.daremote.app.core.model.ServerEntity
import com.daremote.app.core.model.ServerGroupEntity
import com.daremote.app.core.model.SnippetEntity
import com.daremote.app.core.model.SnippetGroupEntity
import com.daremote.app.core.model.SshKeyEntity

@Database(
    entities = [
        ServerEntity::class,
        ServerGroupEntity::class,
        SshKeyEntity::class,
        SnippetEntity::class,
        SnippetGroupEntity::class,
        ForwardingRuleEntity::class,
        AlertRuleEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serverDao(): ServerDao
    abstract fun serverGroupDao(): ServerGroupDao
    abstract fun snippetDao(): SnippetDao
    abstract fun forwardingRuleDao(): ForwardingRuleDao
    abstract fun alertRuleDao(): AlertRuleDao
}
