package com.daremote.app.core.database.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.daremote.app.core.model.ServerEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ServerDao_Impl implements ServerDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ServerEntity> __insertionAdapterOfServerEntity;

  private final EntityDeletionOrUpdateAdapter<ServerEntity> __deletionAdapterOfServerEntity;

  private final EntityDeletionOrUpdateAdapter<ServerEntity> __updateAdapterOfServerEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  private final SharedSQLiteStatement __preparedStmtOfUpdateLastConnected;

  public ServerDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfServerEntity = new EntityInsertionAdapter<ServerEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `servers` (`id`,`name`,`host`,`port`,`username`,`authType`,`credentialRef`,`sshKeyId`,`groupId`,`fingerprint`,`createdAt`,`lastConnectedAt`,`sortOrder`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ServerEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getHost());
        statement.bindLong(4, entity.getPort());
        statement.bindString(5, entity.getUsername());
        statement.bindString(6, entity.getAuthType());
        statement.bindString(7, entity.getCredentialRef());
        if (entity.getSshKeyId() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getSshKeyId());
        }
        if (entity.getGroupId() == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, entity.getGroupId());
        }
        if (entity.getFingerprint() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getFingerprint());
        }
        statement.bindLong(11, entity.getCreatedAt());
        if (entity.getLastConnectedAt() == null) {
          statement.bindNull(12);
        } else {
          statement.bindLong(12, entity.getLastConnectedAt());
        }
        statement.bindLong(13, entity.getSortOrder());
      }
    };
    this.__deletionAdapterOfServerEntity = new EntityDeletionOrUpdateAdapter<ServerEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `servers` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ServerEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfServerEntity = new EntityDeletionOrUpdateAdapter<ServerEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `servers` SET `id` = ?,`name` = ?,`host` = ?,`port` = ?,`username` = ?,`authType` = ?,`credentialRef` = ?,`sshKeyId` = ?,`groupId` = ?,`fingerprint` = ?,`createdAt` = ?,`lastConnectedAt` = ?,`sortOrder` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ServerEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getHost());
        statement.bindLong(4, entity.getPort());
        statement.bindString(5, entity.getUsername());
        statement.bindString(6, entity.getAuthType());
        statement.bindString(7, entity.getCredentialRef());
        if (entity.getSshKeyId() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getSshKeyId());
        }
        if (entity.getGroupId() == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, entity.getGroupId());
        }
        if (entity.getFingerprint() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getFingerprint());
        }
        statement.bindLong(11, entity.getCreatedAt());
        if (entity.getLastConnectedAt() == null) {
          statement.bindNull(12);
        } else {
          statement.bindLong(12, entity.getLastConnectedAt());
        }
        statement.bindLong(13, entity.getSortOrder());
        statement.bindLong(14, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM servers WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateLastConnected = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE servers SET lastConnectedAt = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final ServerEntity server, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfServerEntity.insertAndReturnId(server);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final ServerEntity server, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfServerEntity.handle(server);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final ServerEntity server, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfServerEntity.handle(server);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteById(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateLastConnected(final long id, final long timestamp,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateLastConnected.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, timestamp);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateLastConnected.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ServerEntity>> getAll() {
    final String _sql = "SELECT * FROM servers ORDER BY sortOrder ASC, name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"servers"}, new Callable<List<ServerEntity>>() {
      @Override
      @NonNull
      public List<ServerEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfHost = CursorUtil.getColumnIndexOrThrow(_cursor, "host");
          final int _cursorIndexOfPort = CursorUtil.getColumnIndexOrThrow(_cursor, "port");
          final int _cursorIndexOfUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "username");
          final int _cursorIndexOfAuthType = CursorUtil.getColumnIndexOrThrow(_cursor, "authType");
          final int _cursorIndexOfCredentialRef = CursorUtil.getColumnIndexOrThrow(_cursor, "credentialRef");
          final int _cursorIndexOfSshKeyId = CursorUtil.getColumnIndexOrThrow(_cursor, "sshKeyId");
          final int _cursorIndexOfGroupId = CursorUtil.getColumnIndexOrThrow(_cursor, "groupId");
          final int _cursorIndexOfFingerprint = CursorUtil.getColumnIndexOrThrow(_cursor, "fingerprint");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfLastConnectedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastConnectedAt");
          final int _cursorIndexOfSortOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "sortOrder");
          final List<ServerEntity> _result = new ArrayList<ServerEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ServerEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpHost;
            _tmpHost = _cursor.getString(_cursorIndexOfHost);
            final int _tmpPort;
            _tmpPort = _cursor.getInt(_cursorIndexOfPort);
            final String _tmpUsername;
            _tmpUsername = _cursor.getString(_cursorIndexOfUsername);
            final String _tmpAuthType;
            _tmpAuthType = _cursor.getString(_cursorIndexOfAuthType);
            final String _tmpCredentialRef;
            _tmpCredentialRef = _cursor.getString(_cursorIndexOfCredentialRef);
            final Long _tmpSshKeyId;
            if (_cursor.isNull(_cursorIndexOfSshKeyId)) {
              _tmpSshKeyId = null;
            } else {
              _tmpSshKeyId = _cursor.getLong(_cursorIndexOfSshKeyId);
            }
            final Long _tmpGroupId;
            if (_cursor.isNull(_cursorIndexOfGroupId)) {
              _tmpGroupId = null;
            } else {
              _tmpGroupId = _cursor.getLong(_cursorIndexOfGroupId);
            }
            final String _tmpFingerprint;
            if (_cursor.isNull(_cursorIndexOfFingerprint)) {
              _tmpFingerprint = null;
            } else {
              _tmpFingerprint = _cursor.getString(_cursorIndexOfFingerprint);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final Long _tmpLastConnectedAt;
            if (_cursor.isNull(_cursorIndexOfLastConnectedAt)) {
              _tmpLastConnectedAt = null;
            } else {
              _tmpLastConnectedAt = _cursor.getLong(_cursorIndexOfLastConnectedAt);
            }
            final int _tmpSortOrder;
            _tmpSortOrder = _cursor.getInt(_cursorIndexOfSortOrder);
            _item = new ServerEntity(_tmpId,_tmpName,_tmpHost,_tmpPort,_tmpUsername,_tmpAuthType,_tmpCredentialRef,_tmpSshKeyId,_tmpGroupId,_tmpFingerprint,_tmpCreatedAt,_tmpLastConnectedAt,_tmpSortOrder);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<ServerEntity>> getByGroup(final long groupId) {
    final String _sql = "SELECT * FROM servers WHERE groupId = ? ORDER BY sortOrder ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, groupId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"servers"}, new Callable<List<ServerEntity>>() {
      @Override
      @NonNull
      public List<ServerEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfHost = CursorUtil.getColumnIndexOrThrow(_cursor, "host");
          final int _cursorIndexOfPort = CursorUtil.getColumnIndexOrThrow(_cursor, "port");
          final int _cursorIndexOfUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "username");
          final int _cursorIndexOfAuthType = CursorUtil.getColumnIndexOrThrow(_cursor, "authType");
          final int _cursorIndexOfCredentialRef = CursorUtil.getColumnIndexOrThrow(_cursor, "credentialRef");
          final int _cursorIndexOfSshKeyId = CursorUtil.getColumnIndexOrThrow(_cursor, "sshKeyId");
          final int _cursorIndexOfGroupId = CursorUtil.getColumnIndexOrThrow(_cursor, "groupId");
          final int _cursorIndexOfFingerprint = CursorUtil.getColumnIndexOrThrow(_cursor, "fingerprint");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfLastConnectedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastConnectedAt");
          final int _cursorIndexOfSortOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "sortOrder");
          final List<ServerEntity> _result = new ArrayList<ServerEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ServerEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpHost;
            _tmpHost = _cursor.getString(_cursorIndexOfHost);
            final int _tmpPort;
            _tmpPort = _cursor.getInt(_cursorIndexOfPort);
            final String _tmpUsername;
            _tmpUsername = _cursor.getString(_cursorIndexOfUsername);
            final String _tmpAuthType;
            _tmpAuthType = _cursor.getString(_cursorIndexOfAuthType);
            final String _tmpCredentialRef;
            _tmpCredentialRef = _cursor.getString(_cursorIndexOfCredentialRef);
            final Long _tmpSshKeyId;
            if (_cursor.isNull(_cursorIndexOfSshKeyId)) {
              _tmpSshKeyId = null;
            } else {
              _tmpSshKeyId = _cursor.getLong(_cursorIndexOfSshKeyId);
            }
            final Long _tmpGroupId;
            if (_cursor.isNull(_cursorIndexOfGroupId)) {
              _tmpGroupId = null;
            } else {
              _tmpGroupId = _cursor.getLong(_cursorIndexOfGroupId);
            }
            final String _tmpFingerprint;
            if (_cursor.isNull(_cursorIndexOfFingerprint)) {
              _tmpFingerprint = null;
            } else {
              _tmpFingerprint = _cursor.getString(_cursorIndexOfFingerprint);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final Long _tmpLastConnectedAt;
            if (_cursor.isNull(_cursorIndexOfLastConnectedAt)) {
              _tmpLastConnectedAt = null;
            } else {
              _tmpLastConnectedAt = _cursor.getLong(_cursorIndexOfLastConnectedAt);
            }
            final int _tmpSortOrder;
            _tmpSortOrder = _cursor.getInt(_cursorIndexOfSortOrder);
            _item = new ServerEntity(_tmpId,_tmpName,_tmpHost,_tmpPort,_tmpUsername,_tmpAuthType,_tmpCredentialRef,_tmpSshKeyId,_tmpGroupId,_tmpFingerprint,_tmpCreatedAt,_tmpLastConnectedAt,_tmpSortOrder);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getById(final long id, final Continuation<? super ServerEntity> $completion) {
    final String _sql = "SELECT * FROM servers WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ServerEntity>() {
      @Override
      @Nullable
      public ServerEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfHost = CursorUtil.getColumnIndexOrThrow(_cursor, "host");
          final int _cursorIndexOfPort = CursorUtil.getColumnIndexOrThrow(_cursor, "port");
          final int _cursorIndexOfUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "username");
          final int _cursorIndexOfAuthType = CursorUtil.getColumnIndexOrThrow(_cursor, "authType");
          final int _cursorIndexOfCredentialRef = CursorUtil.getColumnIndexOrThrow(_cursor, "credentialRef");
          final int _cursorIndexOfSshKeyId = CursorUtil.getColumnIndexOrThrow(_cursor, "sshKeyId");
          final int _cursorIndexOfGroupId = CursorUtil.getColumnIndexOrThrow(_cursor, "groupId");
          final int _cursorIndexOfFingerprint = CursorUtil.getColumnIndexOrThrow(_cursor, "fingerprint");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfLastConnectedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastConnectedAt");
          final int _cursorIndexOfSortOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "sortOrder");
          final ServerEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpHost;
            _tmpHost = _cursor.getString(_cursorIndexOfHost);
            final int _tmpPort;
            _tmpPort = _cursor.getInt(_cursorIndexOfPort);
            final String _tmpUsername;
            _tmpUsername = _cursor.getString(_cursorIndexOfUsername);
            final String _tmpAuthType;
            _tmpAuthType = _cursor.getString(_cursorIndexOfAuthType);
            final String _tmpCredentialRef;
            _tmpCredentialRef = _cursor.getString(_cursorIndexOfCredentialRef);
            final Long _tmpSshKeyId;
            if (_cursor.isNull(_cursorIndexOfSshKeyId)) {
              _tmpSshKeyId = null;
            } else {
              _tmpSshKeyId = _cursor.getLong(_cursorIndexOfSshKeyId);
            }
            final Long _tmpGroupId;
            if (_cursor.isNull(_cursorIndexOfGroupId)) {
              _tmpGroupId = null;
            } else {
              _tmpGroupId = _cursor.getLong(_cursorIndexOfGroupId);
            }
            final String _tmpFingerprint;
            if (_cursor.isNull(_cursorIndexOfFingerprint)) {
              _tmpFingerprint = null;
            } else {
              _tmpFingerprint = _cursor.getString(_cursorIndexOfFingerprint);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final Long _tmpLastConnectedAt;
            if (_cursor.isNull(_cursorIndexOfLastConnectedAt)) {
              _tmpLastConnectedAt = null;
            } else {
              _tmpLastConnectedAt = _cursor.getLong(_cursorIndexOfLastConnectedAt);
            }
            final int _tmpSortOrder;
            _tmpSortOrder = _cursor.getInt(_cursorIndexOfSortOrder);
            _result = new ServerEntity(_tmpId,_tmpName,_tmpHost,_tmpPort,_tmpUsername,_tmpAuthType,_tmpCredentialRef,_tmpSshKeyId,_tmpGroupId,_tmpFingerprint,_tmpCreatedAt,_tmpLastConnectedAt,_tmpSortOrder);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
