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
import com.daremote.app.core.model.ForwardingRuleEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
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
public final class ForwardingRuleDao_Impl implements ForwardingRuleDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ForwardingRuleEntity> __insertionAdapterOfForwardingRuleEntity;

  private final EntityDeletionOrUpdateAdapter<ForwardingRuleEntity> __deletionAdapterOfForwardingRuleEntity;

  private final EntityDeletionOrUpdateAdapter<ForwardingRuleEntity> __updateAdapterOfForwardingRuleEntity;

  private final SharedSQLiteStatement __preparedStmtOfSetActive;

  public ForwardingRuleDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfForwardingRuleEntity = new EntityInsertionAdapter<ForwardingRuleEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `forwarding_rules` (`id`,`serverId`,`type`,`name`,`localHost`,`localPort`,`remoteHost`,`remotePort`,`autoConnect`,`isActive`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ForwardingRuleEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getServerId());
        statement.bindString(3, entity.getType());
        statement.bindString(4, entity.getName());
        statement.bindString(5, entity.getLocalHost());
        statement.bindLong(6, entity.getLocalPort());
        if (entity.getRemoteHost() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getRemoteHost());
        }
        if (entity.getRemotePort() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getRemotePort());
        }
        final int _tmp = entity.getAutoConnect() ? 1 : 0;
        statement.bindLong(9, _tmp);
        final int _tmp_1 = entity.isActive() ? 1 : 0;
        statement.bindLong(10, _tmp_1);
        statement.bindLong(11, entity.getCreatedAt());
      }
    };
    this.__deletionAdapterOfForwardingRuleEntity = new EntityDeletionOrUpdateAdapter<ForwardingRuleEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `forwarding_rules` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ForwardingRuleEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfForwardingRuleEntity = new EntityDeletionOrUpdateAdapter<ForwardingRuleEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `forwarding_rules` SET `id` = ?,`serverId` = ?,`type` = ?,`name` = ?,`localHost` = ?,`localPort` = ?,`remoteHost` = ?,`remotePort` = ?,`autoConnect` = ?,`isActive` = ?,`createdAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ForwardingRuleEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getServerId());
        statement.bindString(3, entity.getType());
        statement.bindString(4, entity.getName());
        statement.bindString(5, entity.getLocalHost());
        statement.bindLong(6, entity.getLocalPort());
        if (entity.getRemoteHost() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getRemoteHost());
        }
        if (entity.getRemotePort() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getRemotePort());
        }
        final int _tmp = entity.getAutoConnect() ? 1 : 0;
        statement.bindLong(9, _tmp);
        final int _tmp_1 = entity.isActive() ? 1 : 0;
        statement.bindLong(10, _tmp_1);
        statement.bindLong(11, entity.getCreatedAt());
        statement.bindLong(12, entity.getId());
      }
    };
    this.__preparedStmtOfSetActive = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE forwarding_rules SET isActive = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final ForwardingRuleEntity rule,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfForwardingRuleEntity.insertAndReturnId(rule);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final ForwardingRuleEntity rule,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfForwardingRuleEntity.handle(rule);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final ForwardingRuleEntity rule,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfForwardingRuleEntity.handle(rule);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object setActive(final long id, final boolean active,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSetActive.acquire();
        int _argIndex = 1;
        final int _tmp = active ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
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
          __preparedStmtOfSetActive.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ForwardingRuleEntity>> getAll() {
    final String _sql = "SELECT * FROM forwarding_rules ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"forwarding_rules"}, new Callable<List<ForwardingRuleEntity>>() {
      @Override
      @NonNull
      public List<ForwardingRuleEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfServerId = CursorUtil.getColumnIndexOrThrow(_cursor, "serverId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfLocalHost = CursorUtil.getColumnIndexOrThrow(_cursor, "localHost");
          final int _cursorIndexOfLocalPort = CursorUtil.getColumnIndexOrThrow(_cursor, "localPort");
          final int _cursorIndexOfRemoteHost = CursorUtil.getColumnIndexOrThrow(_cursor, "remoteHost");
          final int _cursorIndexOfRemotePort = CursorUtil.getColumnIndexOrThrow(_cursor, "remotePort");
          final int _cursorIndexOfAutoConnect = CursorUtil.getColumnIndexOrThrow(_cursor, "autoConnect");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<ForwardingRuleEntity> _result = new ArrayList<ForwardingRuleEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ForwardingRuleEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpServerId;
            _tmpServerId = _cursor.getLong(_cursorIndexOfServerId);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpLocalHost;
            _tmpLocalHost = _cursor.getString(_cursorIndexOfLocalHost);
            final int _tmpLocalPort;
            _tmpLocalPort = _cursor.getInt(_cursorIndexOfLocalPort);
            final String _tmpRemoteHost;
            if (_cursor.isNull(_cursorIndexOfRemoteHost)) {
              _tmpRemoteHost = null;
            } else {
              _tmpRemoteHost = _cursor.getString(_cursorIndexOfRemoteHost);
            }
            final Integer _tmpRemotePort;
            if (_cursor.isNull(_cursorIndexOfRemotePort)) {
              _tmpRemotePort = null;
            } else {
              _tmpRemotePort = _cursor.getInt(_cursorIndexOfRemotePort);
            }
            final boolean _tmpAutoConnect;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAutoConnect);
            _tmpAutoConnect = _tmp != 0;
            final boolean _tmpIsActive;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp_1 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new ForwardingRuleEntity(_tmpId,_tmpServerId,_tmpType,_tmpName,_tmpLocalHost,_tmpLocalPort,_tmpRemoteHost,_tmpRemotePort,_tmpAutoConnect,_tmpIsActive,_tmpCreatedAt);
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
  public Flow<List<ForwardingRuleEntity>> getByServer(final long serverId) {
    final String _sql = "SELECT * FROM forwarding_rules WHERE serverId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, serverId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"forwarding_rules"}, new Callable<List<ForwardingRuleEntity>>() {
      @Override
      @NonNull
      public List<ForwardingRuleEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfServerId = CursorUtil.getColumnIndexOrThrow(_cursor, "serverId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfLocalHost = CursorUtil.getColumnIndexOrThrow(_cursor, "localHost");
          final int _cursorIndexOfLocalPort = CursorUtil.getColumnIndexOrThrow(_cursor, "localPort");
          final int _cursorIndexOfRemoteHost = CursorUtil.getColumnIndexOrThrow(_cursor, "remoteHost");
          final int _cursorIndexOfRemotePort = CursorUtil.getColumnIndexOrThrow(_cursor, "remotePort");
          final int _cursorIndexOfAutoConnect = CursorUtil.getColumnIndexOrThrow(_cursor, "autoConnect");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<ForwardingRuleEntity> _result = new ArrayList<ForwardingRuleEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ForwardingRuleEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpServerId;
            _tmpServerId = _cursor.getLong(_cursorIndexOfServerId);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpLocalHost;
            _tmpLocalHost = _cursor.getString(_cursorIndexOfLocalHost);
            final int _tmpLocalPort;
            _tmpLocalPort = _cursor.getInt(_cursorIndexOfLocalPort);
            final String _tmpRemoteHost;
            if (_cursor.isNull(_cursorIndexOfRemoteHost)) {
              _tmpRemoteHost = null;
            } else {
              _tmpRemoteHost = _cursor.getString(_cursorIndexOfRemoteHost);
            }
            final Integer _tmpRemotePort;
            if (_cursor.isNull(_cursorIndexOfRemotePort)) {
              _tmpRemotePort = null;
            } else {
              _tmpRemotePort = _cursor.getInt(_cursorIndexOfRemotePort);
            }
            final boolean _tmpAutoConnect;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAutoConnect);
            _tmpAutoConnect = _tmp != 0;
            final boolean _tmpIsActive;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp_1 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new ForwardingRuleEntity(_tmpId,_tmpServerId,_tmpType,_tmpName,_tmpLocalHost,_tmpLocalPort,_tmpRemoteHost,_tmpRemotePort,_tmpAutoConnect,_tmpIsActive,_tmpCreatedAt);
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
  public Object getAutoConnect(final Continuation<? super List<ForwardingRuleEntity>> $completion) {
    final String _sql = "SELECT * FROM forwarding_rules WHERE autoConnect = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ForwardingRuleEntity>>() {
      @Override
      @NonNull
      public List<ForwardingRuleEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfServerId = CursorUtil.getColumnIndexOrThrow(_cursor, "serverId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfLocalHost = CursorUtil.getColumnIndexOrThrow(_cursor, "localHost");
          final int _cursorIndexOfLocalPort = CursorUtil.getColumnIndexOrThrow(_cursor, "localPort");
          final int _cursorIndexOfRemoteHost = CursorUtil.getColumnIndexOrThrow(_cursor, "remoteHost");
          final int _cursorIndexOfRemotePort = CursorUtil.getColumnIndexOrThrow(_cursor, "remotePort");
          final int _cursorIndexOfAutoConnect = CursorUtil.getColumnIndexOrThrow(_cursor, "autoConnect");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<ForwardingRuleEntity> _result = new ArrayList<ForwardingRuleEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ForwardingRuleEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpServerId;
            _tmpServerId = _cursor.getLong(_cursorIndexOfServerId);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpLocalHost;
            _tmpLocalHost = _cursor.getString(_cursorIndexOfLocalHost);
            final int _tmpLocalPort;
            _tmpLocalPort = _cursor.getInt(_cursorIndexOfLocalPort);
            final String _tmpRemoteHost;
            if (_cursor.isNull(_cursorIndexOfRemoteHost)) {
              _tmpRemoteHost = null;
            } else {
              _tmpRemoteHost = _cursor.getString(_cursorIndexOfRemoteHost);
            }
            final Integer _tmpRemotePort;
            if (_cursor.isNull(_cursorIndexOfRemotePort)) {
              _tmpRemotePort = null;
            } else {
              _tmpRemotePort = _cursor.getInt(_cursorIndexOfRemotePort);
            }
            final boolean _tmpAutoConnect;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAutoConnect);
            _tmpAutoConnect = _tmp != 0;
            final boolean _tmpIsActive;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp_1 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new ForwardingRuleEntity(_tmpId,_tmpServerId,_tmpType,_tmpName,_tmpLocalHost,_tmpLocalPort,_tmpRemoteHost,_tmpRemotePort,_tmpAutoConnect,_tmpIsActive,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getById(final long id,
      final Continuation<? super ForwardingRuleEntity> $completion) {
    final String _sql = "SELECT * FROM forwarding_rules WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ForwardingRuleEntity>() {
      @Override
      @Nullable
      public ForwardingRuleEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfServerId = CursorUtil.getColumnIndexOrThrow(_cursor, "serverId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfLocalHost = CursorUtil.getColumnIndexOrThrow(_cursor, "localHost");
          final int _cursorIndexOfLocalPort = CursorUtil.getColumnIndexOrThrow(_cursor, "localPort");
          final int _cursorIndexOfRemoteHost = CursorUtil.getColumnIndexOrThrow(_cursor, "remoteHost");
          final int _cursorIndexOfRemotePort = CursorUtil.getColumnIndexOrThrow(_cursor, "remotePort");
          final int _cursorIndexOfAutoConnect = CursorUtil.getColumnIndexOrThrow(_cursor, "autoConnect");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final ForwardingRuleEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpServerId;
            _tmpServerId = _cursor.getLong(_cursorIndexOfServerId);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpLocalHost;
            _tmpLocalHost = _cursor.getString(_cursorIndexOfLocalHost);
            final int _tmpLocalPort;
            _tmpLocalPort = _cursor.getInt(_cursorIndexOfLocalPort);
            final String _tmpRemoteHost;
            if (_cursor.isNull(_cursorIndexOfRemoteHost)) {
              _tmpRemoteHost = null;
            } else {
              _tmpRemoteHost = _cursor.getString(_cursorIndexOfRemoteHost);
            }
            final Integer _tmpRemotePort;
            if (_cursor.isNull(_cursorIndexOfRemotePort)) {
              _tmpRemotePort = null;
            } else {
              _tmpRemotePort = _cursor.getInt(_cursorIndexOfRemotePort);
            }
            final boolean _tmpAutoConnect;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAutoConnect);
            _tmpAutoConnect = _tmp != 0;
            final boolean _tmpIsActive;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp_1 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new ForwardingRuleEntity(_tmpId,_tmpServerId,_tmpType,_tmpName,_tmpLocalHost,_tmpLocalPort,_tmpRemoteHost,_tmpRemotePort,_tmpAutoConnect,_tmpIsActive,_tmpCreatedAt);
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
