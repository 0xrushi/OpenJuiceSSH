package com.daremote.app.core.database;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.daremote.app.core.database.dao.AlertRuleDao;
import com.daremote.app.core.database.dao.AlertRuleDao_Impl;
import com.daremote.app.core.database.dao.ForwardingRuleDao;
import com.daremote.app.core.database.dao.ForwardingRuleDao_Impl;
import com.daremote.app.core.database.dao.ServerDao;
import com.daremote.app.core.database.dao.ServerDao_Impl;
import com.daremote.app.core.database.dao.ServerGroupDao;
import com.daremote.app.core.database.dao.ServerGroupDao_Impl;
import com.daremote.app.core.database.dao.SnippetDao;
import com.daremote.app.core.database.dao.SnippetDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile ServerDao _serverDao;

  private volatile ServerGroupDao _serverGroupDao;

  private volatile SnippetDao _snippetDao;

  private volatile ForwardingRuleDao _forwardingRuleDao;

  private volatile AlertRuleDao _alertRuleDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `servers` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `host` TEXT NOT NULL, `port` INTEGER NOT NULL, `username` TEXT NOT NULL, `authType` TEXT NOT NULL, `credentialRef` TEXT NOT NULL, `sshKeyId` INTEGER, `groupId` INTEGER, `fingerprint` TEXT, `createdAt` INTEGER NOT NULL, `lastConnectedAt` INTEGER, `sortOrder` INTEGER NOT NULL, FOREIGN KEY(`groupId`) REFERENCES `server_groups`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_servers_groupId` ON `servers` (`groupId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `server_groups` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `color` INTEGER NOT NULL, `sortOrder` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `ssh_keys` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `type` TEXT NOT NULL, `publicKey` TEXT NOT NULL, `privateKeyRef` TEXT NOT NULL, `hasPassphrase` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `snippets` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `command` TEXT NOT NULL, `description` TEXT, `groupId` INTEGER, `createdAt` INTEGER NOT NULL, `lastUsedAt` INTEGER)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `snippet_groups` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `sortOrder` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `forwarding_rules` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `serverId` INTEGER NOT NULL, `type` TEXT NOT NULL, `name` TEXT NOT NULL, `localHost` TEXT NOT NULL, `localPort` INTEGER NOT NULL, `remoteHost` TEXT, `remotePort` INTEGER, `autoConnect` INTEGER NOT NULL, `isActive` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, FOREIGN KEY(`serverId`) REFERENCES `servers`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_forwarding_rules_serverId` ON `forwarding_rules` (`serverId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `alert_rules` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `serverId` INTEGER NOT NULL, `type` TEXT NOT NULL, `threshold` REAL NOT NULL, `checkIntervalMinutes` INTEGER NOT NULL, `isEnabled` INTEGER NOT NULL, `lastCheckedAt` INTEGER, `lastStatus` TEXT, FOREIGN KEY(`serverId`) REFERENCES `servers`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_alert_rules_serverId` ON `alert_rules` (`serverId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '258791c4f8d60223790ff5bd4dadfd18')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `servers`");
        db.execSQL("DROP TABLE IF EXISTS `server_groups`");
        db.execSQL("DROP TABLE IF EXISTS `ssh_keys`");
        db.execSQL("DROP TABLE IF EXISTS `snippets`");
        db.execSQL("DROP TABLE IF EXISTS `snippet_groups`");
        db.execSQL("DROP TABLE IF EXISTS `forwarding_rules`");
        db.execSQL("DROP TABLE IF EXISTS `alert_rules`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsServers = new HashMap<String, TableInfo.Column>(13);
        _columnsServers.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServers.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServers.put("host", new TableInfo.Column("host", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServers.put("port", new TableInfo.Column("port", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServers.put("username", new TableInfo.Column("username", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServers.put("authType", new TableInfo.Column("authType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServers.put("credentialRef", new TableInfo.Column("credentialRef", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServers.put("sshKeyId", new TableInfo.Column("sshKeyId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServers.put("groupId", new TableInfo.Column("groupId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServers.put("fingerprint", new TableInfo.Column("fingerprint", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServers.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServers.put("lastConnectedAt", new TableInfo.Column("lastConnectedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServers.put("sortOrder", new TableInfo.Column("sortOrder", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysServers = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysServers.add(new TableInfo.ForeignKey("server_groups", "SET NULL", "NO ACTION", Arrays.asList("groupId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesServers = new HashSet<TableInfo.Index>(1);
        _indicesServers.add(new TableInfo.Index("index_servers_groupId", false, Arrays.asList("groupId"), Arrays.asList("ASC")));
        final TableInfo _infoServers = new TableInfo("servers", _columnsServers, _foreignKeysServers, _indicesServers);
        final TableInfo _existingServers = TableInfo.read(db, "servers");
        if (!_infoServers.equals(_existingServers)) {
          return new RoomOpenHelper.ValidationResult(false, "servers(com.daremote.app.core.model.ServerEntity).\n"
                  + " Expected:\n" + _infoServers + "\n"
                  + " Found:\n" + _existingServers);
        }
        final HashMap<String, TableInfo.Column> _columnsServerGroups = new HashMap<String, TableInfo.Column>(4);
        _columnsServerGroups.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServerGroups.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServerGroups.put("color", new TableInfo.Column("color", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServerGroups.put("sortOrder", new TableInfo.Column("sortOrder", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysServerGroups = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesServerGroups = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoServerGroups = new TableInfo("server_groups", _columnsServerGroups, _foreignKeysServerGroups, _indicesServerGroups);
        final TableInfo _existingServerGroups = TableInfo.read(db, "server_groups");
        if (!_infoServerGroups.equals(_existingServerGroups)) {
          return new RoomOpenHelper.ValidationResult(false, "server_groups(com.daremote.app.core.model.ServerGroupEntity).\n"
                  + " Expected:\n" + _infoServerGroups + "\n"
                  + " Found:\n" + _existingServerGroups);
        }
        final HashMap<String, TableInfo.Column> _columnsSshKeys = new HashMap<String, TableInfo.Column>(7);
        _columnsSshKeys.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSshKeys.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSshKeys.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSshKeys.put("publicKey", new TableInfo.Column("publicKey", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSshKeys.put("privateKeyRef", new TableInfo.Column("privateKeyRef", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSshKeys.put("hasPassphrase", new TableInfo.Column("hasPassphrase", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSshKeys.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSshKeys = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSshKeys = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSshKeys = new TableInfo("ssh_keys", _columnsSshKeys, _foreignKeysSshKeys, _indicesSshKeys);
        final TableInfo _existingSshKeys = TableInfo.read(db, "ssh_keys");
        if (!_infoSshKeys.equals(_existingSshKeys)) {
          return new RoomOpenHelper.ValidationResult(false, "ssh_keys(com.daremote.app.core.model.SshKeyEntity).\n"
                  + " Expected:\n" + _infoSshKeys + "\n"
                  + " Found:\n" + _existingSshKeys);
        }
        final HashMap<String, TableInfo.Column> _columnsSnippets = new HashMap<String, TableInfo.Column>(7);
        _columnsSnippets.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSnippets.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSnippets.put("command", new TableInfo.Column("command", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSnippets.put("description", new TableInfo.Column("description", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSnippets.put("groupId", new TableInfo.Column("groupId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSnippets.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSnippets.put("lastUsedAt", new TableInfo.Column("lastUsedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSnippets = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSnippets = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSnippets = new TableInfo("snippets", _columnsSnippets, _foreignKeysSnippets, _indicesSnippets);
        final TableInfo _existingSnippets = TableInfo.read(db, "snippets");
        if (!_infoSnippets.equals(_existingSnippets)) {
          return new RoomOpenHelper.ValidationResult(false, "snippets(com.daremote.app.core.model.SnippetEntity).\n"
                  + " Expected:\n" + _infoSnippets + "\n"
                  + " Found:\n" + _existingSnippets);
        }
        final HashMap<String, TableInfo.Column> _columnsSnippetGroups = new HashMap<String, TableInfo.Column>(3);
        _columnsSnippetGroups.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSnippetGroups.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSnippetGroups.put("sortOrder", new TableInfo.Column("sortOrder", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSnippetGroups = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSnippetGroups = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSnippetGroups = new TableInfo("snippet_groups", _columnsSnippetGroups, _foreignKeysSnippetGroups, _indicesSnippetGroups);
        final TableInfo _existingSnippetGroups = TableInfo.read(db, "snippet_groups");
        if (!_infoSnippetGroups.equals(_existingSnippetGroups)) {
          return new RoomOpenHelper.ValidationResult(false, "snippet_groups(com.daremote.app.core.model.SnippetGroupEntity).\n"
                  + " Expected:\n" + _infoSnippetGroups + "\n"
                  + " Found:\n" + _existingSnippetGroups);
        }
        final HashMap<String, TableInfo.Column> _columnsForwardingRules = new HashMap<String, TableInfo.Column>(11);
        _columnsForwardingRules.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsForwardingRules.put("serverId", new TableInfo.Column("serverId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsForwardingRules.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsForwardingRules.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsForwardingRules.put("localHost", new TableInfo.Column("localHost", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsForwardingRules.put("localPort", new TableInfo.Column("localPort", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsForwardingRules.put("remoteHost", new TableInfo.Column("remoteHost", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsForwardingRules.put("remotePort", new TableInfo.Column("remotePort", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsForwardingRules.put("autoConnect", new TableInfo.Column("autoConnect", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsForwardingRules.put("isActive", new TableInfo.Column("isActive", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsForwardingRules.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysForwardingRules = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysForwardingRules.add(new TableInfo.ForeignKey("servers", "CASCADE", "NO ACTION", Arrays.asList("serverId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesForwardingRules = new HashSet<TableInfo.Index>(1);
        _indicesForwardingRules.add(new TableInfo.Index("index_forwarding_rules_serverId", false, Arrays.asList("serverId"), Arrays.asList("ASC")));
        final TableInfo _infoForwardingRules = new TableInfo("forwarding_rules", _columnsForwardingRules, _foreignKeysForwardingRules, _indicesForwardingRules);
        final TableInfo _existingForwardingRules = TableInfo.read(db, "forwarding_rules");
        if (!_infoForwardingRules.equals(_existingForwardingRules)) {
          return new RoomOpenHelper.ValidationResult(false, "forwarding_rules(com.daremote.app.core.model.ForwardingRuleEntity).\n"
                  + " Expected:\n" + _infoForwardingRules + "\n"
                  + " Found:\n" + _existingForwardingRules);
        }
        final HashMap<String, TableInfo.Column> _columnsAlertRules = new HashMap<String, TableInfo.Column>(8);
        _columnsAlertRules.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAlertRules.put("serverId", new TableInfo.Column("serverId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAlertRules.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAlertRules.put("threshold", new TableInfo.Column("threshold", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAlertRules.put("checkIntervalMinutes", new TableInfo.Column("checkIntervalMinutes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAlertRules.put("isEnabled", new TableInfo.Column("isEnabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAlertRules.put("lastCheckedAt", new TableInfo.Column("lastCheckedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAlertRules.put("lastStatus", new TableInfo.Column("lastStatus", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAlertRules = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysAlertRules.add(new TableInfo.ForeignKey("servers", "CASCADE", "NO ACTION", Arrays.asList("serverId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesAlertRules = new HashSet<TableInfo.Index>(1);
        _indicesAlertRules.add(new TableInfo.Index("index_alert_rules_serverId", false, Arrays.asList("serverId"), Arrays.asList("ASC")));
        final TableInfo _infoAlertRules = new TableInfo("alert_rules", _columnsAlertRules, _foreignKeysAlertRules, _indicesAlertRules);
        final TableInfo _existingAlertRules = TableInfo.read(db, "alert_rules");
        if (!_infoAlertRules.equals(_existingAlertRules)) {
          return new RoomOpenHelper.ValidationResult(false, "alert_rules(com.daremote.app.core.model.AlertRuleEntity).\n"
                  + " Expected:\n" + _infoAlertRules + "\n"
                  + " Found:\n" + _existingAlertRules);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "258791c4f8d60223790ff5bd4dadfd18", "1b12b63a549288a25083baf763c27f4b");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "servers","server_groups","ssh_keys","snippets","snippet_groups","forwarding_rules","alert_rules");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `servers`");
      _db.execSQL("DELETE FROM `server_groups`");
      _db.execSQL("DELETE FROM `ssh_keys`");
      _db.execSQL("DELETE FROM `snippets`");
      _db.execSQL("DELETE FROM `snippet_groups`");
      _db.execSQL("DELETE FROM `forwarding_rules`");
      _db.execSQL("DELETE FROM `alert_rules`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(ServerDao.class, ServerDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ServerGroupDao.class, ServerGroupDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SnippetDao.class, SnippetDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ForwardingRuleDao.class, ForwardingRuleDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(AlertRuleDao.class, AlertRuleDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public ServerDao serverDao() {
    if (_serverDao != null) {
      return _serverDao;
    } else {
      synchronized(this) {
        if(_serverDao == null) {
          _serverDao = new ServerDao_Impl(this);
        }
        return _serverDao;
      }
    }
  }

  @Override
  public ServerGroupDao serverGroupDao() {
    if (_serverGroupDao != null) {
      return _serverGroupDao;
    } else {
      synchronized(this) {
        if(_serverGroupDao == null) {
          _serverGroupDao = new ServerGroupDao_Impl(this);
        }
        return _serverGroupDao;
      }
    }
  }

  @Override
  public SnippetDao snippetDao() {
    if (_snippetDao != null) {
      return _snippetDao;
    } else {
      synchronized(this) {
        if(_snippetDao == null) {
          _snippetDao = new SnippetDao_Impl(this);
        }
        return _snippetDao;
      }
    }
  }

  @Override
  public ForwardingRuleDao forwardingRuleDao() {
    if (_forwardingRuleDao != null) {
      return _forwardingRuleDao;
    } else {
      synchronized(this) {
        if(_forwardingRuleDao == null) {
          _forwardingRuleDao = new ForwardingRuleDao_Impl(this);
        }
        return _forwardingRuleDao;
      }
    }
  }

  @Override
  public AlertRuleDao alertRuleDao() {
    if (_alertRuleDao != null) {
      return _alertRuleDao;
    } else {
      synchronized(this) {
        if(_alertRuleDao == null) {
          _alertRuleDao = new AlertRuleDao_Impl(this);
        }
        return _alertRuleDao;
      }
    }
  }
}
