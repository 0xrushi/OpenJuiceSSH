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
import com.daremote.app.core.model.AlertRuleEntity;
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
public final class AlertRuleDao_Impl implements AlertRuleDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<AlertRuleEntity> __insertionAdapterOfAlertRuleEntity;

  private final EntityDeletionOrUpdateAdapter<AlertRuleEntity> __deletionAdapterOfAlertRuleEntity;

  private final EntityDeletionOrUpdateAdapter<AlertRuleEntity> __updateAdapterOfAlertRuleEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateStatus;

  public AlertRuleDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAlertRuleEntity = new EntityInsertionAdapter<AlertRuleEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `alert_rules` (`id`,`serverId`,`type`,`threshold`,`checkIntervalMinutes`,`isEnabled`,`lastCheckedAt`,`lastStatus`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AlertRuleEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getServerId());
        statement.bindString(3, entity.getType());
        statement.bindDouble(4, entity.getThreshold());
        statement.bindLong(5, entity.getCheckIntervalMinutes());
        final int _tmp = entity.isEnabled() ? 1 : 0;
        statement.bindLong(6, _tmp);
        if (entity.getLastCheckedAt() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getLastCheckedAt());
        }
        if (entity.getLastStatus() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getLastStatus());
        }
      }
    };
    this.__deletionAdapterOfAlertRuleEntity = new EntityDeletionOrUpdateAdapter<AlertRuleEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `alert_rules` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AlertRuleEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfAlertRuleEntity = new EntityDeletionOrUpdateAdapter<AlertRuleEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `alert_rules` SET `id` = ?,`serverId` = ?,`type` = ?,`threshold` = ?,`checkIntervalMinutes` = ?,`isEnabled` = ?,`lastCheckedAt` = ?,`lastStatus` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AlertRuleEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getServerId());
        statement.bindString(3, entity.getType());
        statement.bindDouble(4, entity.getThreshold());
        statement.bindLong(5, entity.getCheckIntervalMinutes());
        final int _tmp = entity.isEnabled() ? 1 : 0;
        statement.bindLong(6, _tmp);
        if (entity.getLastCheckedAt() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getLastCheckedAt());
        }
        if (entity.getLastStatus() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getLastStatus());
        }
        statement.bindLong(9, entity.getId());
      }
    };
    this.__preparedStmtOfUpdateStatus = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE alert_rules SET lastCheckedAt = ?, lastStatus = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final AlertRuleEntity rule, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfAlertRuleEntity.insertAndReturnId(rule);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final AlertRuleEntity rule, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfAlertRuleEntity.handle(rule);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final AlertRuleEntity rule, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfAlertRuleEntity.handle(rule);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateStatus(final long id, final long timestamp, final String status,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateStatus.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, timestamp);
        _argIndex = 2;
        _stmt.bindString(_argIndex, status);
        _argIndex = 3;
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
          __preparedStmtOfUpdateStatus.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<AlertRuleEntity>> getAll() {
    final String _sql = "SELECT * FROM alert_rules ORDER BY serverId ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"alert_rules"}, new Callable<List<AlertRuleEntity>>() {
      @Override
      @NonNull
      public List<AlertRuleEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfServerId = CursorUtil.getColumnIndexOrThrow(_cursor, "serverId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "threshold");
          final int _cursorIndexOfCheckIntervalMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "checkIntervalMinutes");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
          final int _cursorIndexOfLastCheckedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastCheckedAt");
          final int _cursorIndexOfLastStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "lastStatus");
          final List<AlertRuleEntity> _result = new ArrayList<AlertRuleEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AlertRuleEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpServerId;
            _tmpServerId = _cursor.getLong(_cursorIndexOfServerId);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final float _tmpThreshold;
            _tmpThreshold = _cursor.getFloat(_cursorIndexOfThreshold);
            final int _tmpCheckIntervalMinutes;
            _tmpCheckIntervalMinutes = _cursor.getInt(_cursorIndexOfCheckIntervalMinutes);
            final boolean _tmpIsEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp != 0;
            final Long _tmpLastCheckedAt;
            if (_cursor.isNull(_cursorIndexOfLastCheckedAt)) {
              _tmpLastCheckedAt = null;
            } else {
              _tmpLastCheckedAt = _cursor.getLong(_cursorIndexOfLastCheckedAt);
            }
            final String _tmpLastStatus;
            if (_cursor.isNull(_cursorIndexOfLastStatus)) {
              _tmpLastStatus = null;
            } else {
              _tmpLastStatus = _cursor.getString(_cursorIndexOfLastStatus);
            }
            _item = new AlertRuleEntity(_tmpId,_tmpServerId,_tmpType,_tmpThreshold,_tmpCheckIntervalMinutes,_tmpIsEnabled,_tmpLastCheckedAt,_tmpLastStatus);
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
  public Flow<List<AlertRuleEntity>> getByServer(final long serverId) {
    final String _sql = "SELECT * FROM alert_rules WHERE serverId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, serverId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"alert_rules"}, new Callable<List<AlertRuleEntity>>() {
      @Override
      @NonNull
      public List<AlertRuleEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfServerId = CursorUtil.getColumnIndexOrThrow(_cursor, "serverId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "threshold");
          final int _cursorIndexOfCheckIntervalMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "checkIntervalMinutes");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
          final int _cursorIndexOfLastCheckedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastCheckedAt");
          final int _cursorIndexOfLastStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "lastStatus");
          final List<AlertRuleEntity> _result = new ArrayList<AlertRuleEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AlertRuleEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpServerId;
            _tmpServerId = _cursor.getLong(_cursorIndexOfServerId);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final float _tmpThreshold;
            _tmpThreshold = _cursor.getFloat(_cursorIndexOfThreshold);
            final int _tmpCheckIntervalMinutes;
            _tmpCheckIntervalMinutes = _cursor.getInt(_cursorIndexOfCheckIntervalMinutes);
            final boolean _tmpIsEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp != 0;
            final Long _tmpLastCheckedAt;
            if (_cursor.isNull(_cursorIndexOfLastCheckedAt)) {
              _tmpLastCheckedAt = null;
            } else {
              _tmpLastCheckedAt = _cursor.getLong(_cursorIndexOfLastCheckedAt);
            }
            final String _tmpLastStatus;
            if (_cursor.isNull(_cursorIndexOfLastStatus)) {
              _tmpLastStatus = null;
            } else {
              _tmpLastStatus = _cursor.getString(_cursorIndexOfLastStatus);
            }
            _item = new AlertRuleEntity(_tmpId,_tmpServerId,_tmpType,_tmpThreshold,_tmpCheckIntervalMinutes,_tmpIsEnabled,_tmpLastCheckedAt,_tmpLastStatus);
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
  public Object getEnabled(final Continuation<? super List<AlertRuleEntity>> $completion) {
    final String _sql = "SELECT * FROM alert_rules WHERE isEnabled = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<AlertRuleEntity>>() {
      @Override
      @NonNull
      public List<AlertRuleEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfServerId = CursorUtil.getColumnIndexOrThrow(_cursor, "serverId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "threshold");
          final int _cursorIndexOfCheckIntervalMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "checkIntervalMinutes");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
          final int _cursorIndexOfLastCheckedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastCheckedAt");
          final int _cursorIndexOfLastStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "lastStatus");
          final List<AlertRuleEntity> _result = new ArrayList<AlertRuleEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AlertRuleEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpServerId;
            _tmpServerId = _cursor.getLong(_cursorIndexOfServerId);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final float _tmpThreshold;
            _tmpThreshold = _cursor.getFloat(_cursorIndexOfThreshold);
            final int _tmpCheckIntervalMinutes;
            _tmpCheckIntervalMinutes = _cursor.getInt(_cursorIndexOfCheckIntervalMinutes);
            final boolean _tmpIsEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp != 0;
            final Long _tmpLastCheckedAt;
            if (_cursor.isNull(_cursorIndexOfLastCheckedAt)) {
              _tmpLastCheckedAt = null;
            } else {
              _tmpLastCheckedAt = _cursor.getLong(_cursorIndexOfLastCheckedAt);
            }
            final String _tmpLastStatus;
            if (_cursor.isNull(_cursorIndexOfLastStatus)) {
              _tmpLastStatus = null;
            } else {
              _tmpLastStatus = _cursor.getString(_cursorIndexOfLastStatus);
            }
            _item = new AlertRuleEntity(_tmpId,_tmpServerId,_tmpType,_tmpThreshold,_tmpCheckIntervalMinutes,_tmpIsEnabled,_tmpLastCheckedAt,_tmpLastStatus);
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
  public Object getById(final long id, final Continuation<? super AlertRuleEntity> $completion) {
    final String _sql = "SELECT * FROM alert_rules WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<AlertRuleEntity>() {
      @Override
      @Nullable
      public AlertRuleEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfServerId = CursorUtil.getColumnIndexOrThrow(_cursor, "serverId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "threshold");
          final int _cursorIndexOfCheckIntervalMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "checkIntervalMinutes");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
          final int _cursorIndexOfLastCheckedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastCheckedAt");
          final int _cursorIndexOfLastStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "lastStatus");
          final AlertRuleEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpServerId;
            _tmpServerId = _cursor.getLong(_cursorIndexOfServerId);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final float _tmpThreshold;
            _tmpThreshold = _cursor.getFloat(_cursorIndexOfThreshold);
            final int _tmpCheckIntervalMinutes;
            _tmpCheckIntervalMinutes = _cursor.getInt(_cursorIndexOfCheckIntervalMinutes);
            final boolean _tmpIsEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp != 0;
            final Long _tmpLastCheckedAt;
            if (_cursor.isNull(_cursorIndexOfLastCheckedAt)) {
              _tmpLastCheckedAt = null;
            } else {
              _tmpLastCheckedAt = _cursor.getLong(_cursorIndexOfLastCheckedAt);
            }
            final String _tmpLastStatus;
            if (_cursor.isNull(_cursorIndexOfLastStatus)) {
              _tmpLastStatus = null;
            } else {
              _tmpLastStatus = _cursor.getString(_cursorIndexOfLastStatus);
            }
            _result = new AlertRuleEntity(_tmpId,_tmpServerId,_tmpType,_tmpThreshold,_tmpCheckIntervalMinutes,_tmpIsEnabled,_tmpLastCheckedAt,_tmpLastStatus);
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
