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
import com.daremote.app.core.model.SnippetEntity;
import com.daremote.app.core.model.SnippetGroupEntity;
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
public final class SnippetDao_Impl implements SnippetDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SnippetEntity> __insertionAdapterOfSnippetEntity;

  private final EntityInsertionAdapter<SnippetGroupEntity> __insertionAdapterOfSnippetGroupEntity;

  private final EntityDeletionOrUpdateAdapter<SnippetEntity> __deletionAdapterOfSnippetEntity;

  private final EntityDeletionOrUpdateAdapter<SnippetGroupEntity> __deletionAdapterOfSnippetGroupEntity;

  private final EntityDeletionOrUpdateAdapter<SnippetEntity> __updateAdapterOfSnippetEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateLastUsed;

  public SnippetDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSnippetEntity = new EntityInsertionAdapter<SnippetEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `snippets` (`id`,`name`,`command`,`description`,`groupId`,`createdAt`,`lastUsedAt`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SnippetEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getCommand());
        if (entity.getDescription() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getDescription());
        }
        if (entity.getGroupId() == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, entity.getGroupId());
        }
        statement.bindLong(6, entity.getCreatedAt());
        if (entity.getLastUsedAt() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getLastUsedAt());
        }
      }
    };
    this.__insertionAdapterOfSnippetGroupEntity = new EntityInsertionAdapter<SnippetGroupEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `snippet_groups` (`id`,`name`,`sortOrder`) VALUES (nullif(?, 0),?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SnippetGroupEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindLong(3, entity.getSortOrder());
      }
    };
    this.__deletionAdapterOfSnippetEntity = new EntityDeletionOrUpdateAdapter<SnippetEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `snippets` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SnippetEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__deletionAdapterOfSnippetGroupEntity = new EntityDeletionOrUpdateAdapter<SnippetGroupEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `snippet_groups` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SnippetGroupEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfSnippetEntity = new EntityDeletionOrUpdateAdapter<SnippetEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `snippets` SET `id` = ?,`name` = ?,`command` = ?,`description` = ?,`groupId` = ?,`createdAt` = ?,`lastUsedAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SnippetEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getCommand());
        if (entity.getDescription() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getDescription());
        }
        if (entity.getGroupId() == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, entity.getGroupId());
        }
        statement.bindLong(6, entity.getCreatedAt());
        if (entity.getLastUsedAt() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getLastUsedAt());
        }
        statement.bindLong(8, entity.getId());
      }
    };
    this.__preparedStmtOfUpdateLastUsed = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE snippets SET lastUsedAt = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final SnippetEntity snippet, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfSnippetEntity.insertAndReturnId(snippet);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertGroup(final SnippetGroupEntity group,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfSnippetGroupEntity.insertAndReturnId(group);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final SnippetEntity snippet, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfSnippetEntity.handle(snippet);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteGroup(final SnippetGroupEntity group,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfSnippetGroupEntity.handle(group);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final SnippetEntity snippet, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfSnippetEntity.handle(snippet);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateLastUsed(final long id, final long timestamp,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateLastUsed.acquire();
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
          __preparedStmtOfUpdateLastUsed.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<SnippetEntity>> getAll() {
    final String _sql = "SELECT * FROM snippets ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"snippets"}, new Callable<List<SnippetEntity>>() {
      @Override
      @NonNull
      public List<SnippetEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCommand = CursorUtil.getColumnIndexOrThrow(_cursor, "command");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfGroupId = CursorUtil.getColumnIndexOrThrow(_cursor, "groupId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfLastUsedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUsedAt");
          final List<SnippetEntity> _result = new ArrayList<SnippetEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SnippetEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpCommand;
            _tmpCommand = _cursor.getString(_cursorIndexOfCommand);
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final Long _tmpGroupId;
            if (_cursor.isNull(_cursorIndexOfGroupId)) {
              _tmpGroupId = null;
            } else {
              _tmpGroupId = _cursor.getLong(_cursorIndexOfGroupId);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final Long _tmpLastUsedAt;
            if (_cursor.isNull(_cursorIndexOfLastUsedAt)) {
              _tmpLastUsedAt = null;
            } else {
              _tmpLastUsedAt = _cursor.getLong(_cursorIndexOfLastUsedAt);
            }
            _item = new SnippetEntity(_tmpId,_tmpName,_tmpCommand,_tmpDescription,_tmpGroupId,_tmpCreatedAt,_tmpLastUsedAt);
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
  public Flow<List<SnippetEntity>> getByGroup(final long groupId) {
    final String _sql = "SELECT * FROM snippets WHERE groupId = ? ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, groupId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"snippets"}, new Callable<List<SnippetEntity>>() {
      @Override
      @NonNull
      public List<SnippetEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCommand = CursorUtil.getColumnIndexOrThrow(_cursor, "command");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfGroupId = CursorUtil.getColumnIndexOrThrow(_cursor, "groupId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfLastUsedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUsedAt");
          final List<SnippetEntity> _result = new ArrayList<SnippetEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SnippetEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpCommand;
            _tmpCommand = _cursor.getString(_cursorIndexOfCommand);
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final Long _tmpGroupId;
            if (_cursor.isNull(_cursorIndexOfGroupId)) {
              _tmpGroupId = null;
            } else {
              _tmpGroupId = _cursor.getLong(_cursorIndexOfGroupId);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final Long _tmpLastUsedAt;
            if (_cursor.isNull(_cursorIndexOfLastUsedAt)) {
              _tmpLastUsedAt = null;
            } else {
              _tmpLastUsedAt = _cursor.getLong(_cursorIndexOfLastUsedAt);
            }
            _item = new SnippetEntity(_tmpId,_tmpName,_tmpCommand,_tmpDescription,_tmpGroupId,_tmpCreatedAt,_tmpLastUsedAt);
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
  public Object getById(final long id, final Continuation<? super SnippetEntity> $completion) {
    final String _sql = "SELECT * FROM snippets WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<SnippetEntity>() {
      @Override
      @Nullable
      public SnippetEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCommand = CursorUtil.getColumnIndexOrThrow(_cursor, "command");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfGroupId = CursorUtil.getColumnIndexOrThrow(_cursor, "groupId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfLastUsedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUsedAt");
          final SnippetEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpCommand;
            _tmpCommand = _cursor.getString(_cursorIndexOfCommand);
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final Long _tmpGroupId;
            if (_cursor.isNull(_cursorIndexOfGroupId)) {
              _tmpGroupId = null;
            } else {
              _tmpGroupId = _cursor.getLong(_cursorIndexOfGroupId);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final Long _tmpLastUsedAt;
            if (_cursor.isNull(_cursorIndexOfLastUsedAt)) {
              _tmpLastUsedAt = null;
            } else {
              _tmpLastUsedAt = _cursor.getLong(_cursorIndexOfLastUsedAt);
            }
            _result = new SnippetEntity(_tmpId,_tmpName,_tmpCommand,_tmpDescription,_tmpGroupId,_tmpCreatedAt,_tmpLastUsedAt);
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

  @Override
  public Flow<List<SnippetGroupEntity>> getAllGroups() {
    final String _sql = "SELECT * FROM snippet_groups ORDER BY sortOrder ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"snippet_groups"}, new Callable<List<SnippetGroupEntity>>() {
      @Override
      @NonNull
      public List<SnippetGroupEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfSortOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "sortOrder");
          final List<SnippetGroupEntity> _result = new ArrayList<SnippetGroupEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SnippetGroupEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final int _tmpSortOrder;
            _tmpSortOrder = _cursor.getInt(_cursorIndexOfSortOrder);
            _item = new SnippetGroupEntity(_tmpId,_tmpName,_tmpSortOrder);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
