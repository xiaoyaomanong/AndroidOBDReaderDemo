package com.example.obdreader.trips;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Date;

/**
 * Some code taken from https://github.com/wdkapps/FillUp
 */
public class TripLog {
    // 数据库版本号
    public static final int DATABASE_VERSION = 1;
    // 数据库名称
    public static final String DATABASE_NAME = "tripslog.db";
    //用于调试日志记录的标签字符串（此类的名称）
    private static final String TAG = TripLog.class.getName();
    // 数据库表名称
    private static final String RECORDS_TABLE = "Records";
    // SQL命令删除数据库
    public static final String[] DATABASE_DELETE = new String[]{
            "drop table if exists " + RECORDS_TABLE + ";",
    };
    // RECORDS_TABLE的列名称
    private static final String RECORD_ID = "id";
    private static final String RECORD_START_DATE = "startDate";
    private static final String RECORD_END_DATE = "endDate";
    private static final String RECORD_RPM_MAX = "rmpMax";
    private static final String RECORD_SPEED_MAX = "speedMax";
    private static final String RECORD_ENGINE_RUNTIME = "engineRuntime";
    /// SQL命令创建数据库
    public static final String[] DATABASE_CREATE = new String[]{
            "create table " + RECORDS_TABLE + " ( " +
                    RECORD_ID + " integer primary key autoincrement, " +
                    RECORD_START_DATE + " integer not null, " +
                    RECORD_END_DATE + " integer, " +
                    RECORD_SPEED_MAX + " integer, " +
                    RECORD_RPM_MAX + " integer, " +
                    RECORD_ENGINE_RUNTIME + " text" +
                    ");"
    };
    // RECORDS TABLE的所有列名称的数组
    private static final String[] RECORDS_TABLE_COLUMNS = new String[]{
            RECORD_ID,
            RECORD_START_DATE,
            RECORD_END_DATE,
            RECORD_SPEED_MAX,
            RECORD_ENGINE_RUNTIME,
            RECORD_RPM_MAX
    };
    //单例实例
    private static TripLog instance;
    private final Context context;
    // 用于打开和关闭数据库的帮助程序实例
    private final TripLogOpenHelper helper;
    private final SQLiteDatabase db;

    private TripLog(Context context) {
        this.context = context;
        this.helper = new TripLogOpenHelper(this.context);
        this.db = helper.getWritableDatabase();
    }

    /**
     * 描述：
     * 返回一个实例，并在必要时创建它。
     *
     * @return GasLog - singleton instance.
     */
    public static TripLog getInstance(Context context) {
        if (instance == null) {
            instance = new TripLog(context);
        }
        return instance;
    }

    /**
     * 描述：
     * 检验断言的便捷方法。
     *
     * @param assertion - an asserted boolean condition.
     * @param tag       - a tag String identifying the calling method.
     * @param msg       - an error message to display/log.
     * @throws RuntimeException if the assertion is false
     */
    private void ASSERT(boolean assertion, String tag, String msg) {
        if (!assertion) {
            String assert_msg = "ASSERT failed: " + msg;
            android.util.Log.e(tag, assert_msg);
            throw new RuntimeException(assert_msg);
        }
    }

    public TripRecord startTrip() {
        final String tag = TAG + ".createRecord()";

        try {
            TripRecord record = new TripRecord();
            long rowID = db.insertOrThrow(RECORDS_TABLE, null, getContentValues(record));
            record.setID((int) rowID);
            return record;
        } catch (SQLiteConstraintException e) {
            android.util.Log.e(tag, "SQLiteConstraintException: " + e.getMessage());
        } catch (SQLException e) {
            android.util.Log.e(tag, "SQLException: " + e.getMessage());
        }
        return null;
    }

    /**
     * 描述：
     * 更新日志中的行程记录。
     *
     * @param record - the TripRecord to update.
     * @return boolean flag indicating success/failure (true=success)
     */
    public boolean updateRecord(TripRecord record) {
        final String tag = TAG + ".updateRecord()";
        ASSERT((record.getID() != null), tag, "record id cannot be null");
        boolean success = false;
        try {
            ContentValues values = getContentValues(record);
            values.remove(RECORD_ID);
            String whereClause = RECORD_ID + "=" + record.getID();
            int count = db.update(RECORDS_TABLE, values, whereClause, null);
            success = (count > 0);
        } catch (SQLiteConstraintException e) {
            android.util.Log.e(tag, "SQLiteConstraintException: " + e.getMessage());
        } catch (SQLException e) {
            android.util.Log.e(tag, "SQLException: " + e.getMessage());
        }
        return success;
    }

    /**
     * 描述：
     * 将TripRecord实例转换为一组键/值的便捷方法
     * SQLite访问方法使用的ContentValues实例中的对。
     *
     * @param record - the GasRecord to convert.
     * @return a ContentValues instance representing the specified GasRecord.
     */
    private ContentValues getContentValues(TripRecord record) {
        ContentValues values = new ContentValues();
        values.put(RECORD_ID, record.getID());
        values.put(RECORD_START_DATE, record.getStartDate().getTime());
        if (record.getEndDate() != null)
            values.put(RECORD_END_DATE, record.getEndDate().getTime());
        values.put(RECORD_RPM_MAX, record.getEngineRpmMax());
        values.put(RECORD_SPEED_MAX, record.getSpeedMax());
        if (record.getEngineRuntime() != null)
            values.put(RECORD_ENGINE_RUNTIME, record.getEngineRuntime());
        return values;
    }

    private void update() {
        String sql = "ALTER TABLE " + RECORDS_TABLE + " ADD COLUMN " + RECORD_ENGINE_RUNTIME + " integer;";
        db.execSQL(sql);
    }

    public java.util.List<TripRecord> readAllRecords() {
        final String tag = TAG + ".readAllRecords()";
        java.util.List<TripRecord> list = new ArrayList<>();
        android.database.Cursor cursor = null;

        try {
            String orderBy = RECORD_START_DATE;
            cursor = db.query(
                    RECORDS_TABLE,
                    RECORDS_TABLE_COLUMNS,
                    null,
                    null, null, null,
                    orderBy,
                    null
            );

            // 根据数据创建TripRecords列表
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        TripRecord record = getRecordFromCursor(cursor);
                        list.add(record);
                    } while (cursor.moveToNext());
                }
            }

        } catch (SQLException e) {
            android.util.Log.e(tag, "SQLException: " + e.getMessage());
            list.clear();
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    /**
     * 描述：
     * 从日志中删除指定的行程记录。
     *
     * @param id - the TripRecord to delete.
     * @return boolean flag indicating success/failure (true=success)
     */
    public boolean deleteTrip(long id) {

        final String tag = TAG + ".deleteRecord()";

        boolean success = false;

        try {
            String whereClause = RECORD_ID + "=" + id;
            String[] whereArgs = null;
            int count = db.delete(RECORDS_TABLE, whereClause, whereArgs);
            success = (count == 1);
        } catch (SQLException e) {
            android.util.Log.e(tag, "SQLException: " + e.getMessage());
        }

        return success;
    }

    /**
     * 描述：
     * 从读取的值创建TripRecord实例的便捷方法
     * 从数据库中。
     *
     * @param c - a Cursor containing results of a database query.
     * @return a GasRecord instance (null if no data).
     */
    private TripRecord getRecordFromCursor(android.database.Cursor c) {
        final String tag = TAG + ".getRecordFromCursor()";
        TripRecord record = null;
        if (c != null) {
            record = new TripRecord();
            int id = c.getInt(c.getColumnIndex(RECORD_ID));
            long startDate = c.getLong(c.getColumnIndex(RECORD_START_DATE));
            long endTime = c.getLong(c.getColumnIndex(RECORD_END_DATE));
            int engineRpmMax = c.getInt(c.getColumnIndex(RECORD_RPM_MAX));
            int speedMax = c.getInt(c.getColumnIndex(RECORD_SPEED_MAX));
            record.setID(id);
            record.setStartDate(new Date(startDate));
            record.setEndDate(new Date(endTime));
            record.setEngineRpmMax(engineRpmMax);
            record.setSpeedMax(speedMax);
            if (!c.isNull(c.getColumnIndex(RECORD_ENGINE_RUNTIME)))
                record.setEngineRuntime(c.getString(c.getColumnIndex(RECORD_ENGINE_RUNTIME)));
        }
        return record;
    }
}
