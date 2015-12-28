/**
 *
 */
package jp.gr.java_conf.mkh.alarm.db;

import java.util.Calendar;
import java.util.Date;

import jp.gr.java_conf.mkh.alarm.content.AlarmProviderConsts;
import jp.gr.java_conf.mkh.alarm.model.Alarm;
import jp.gr.java_conf.mkh.alarm.model.Alarm.EnableCode;
import jp.gr.java_conf.mkh.alarm.util.Util;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

/**
 * DBアクセスのヘルパークラス。
 *
 * @author mkh
 *
 */
public class AlarmDBHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 2;

    private static final String CONDITON_FOR_ONE_ALARM = " YEAR = ? AND MONTH = ? AND DAY = ? AND HOUR = ? AND MIN = ?";

    private static final String GROUP_NAME_DEFAULT = "not in groups";

    /**
     * コンストラクタ。
     *
     * @param context
     *            コンテキスト
     * @param name
     *            DBファイル名
     */
    public AlarmDBHelper(Context context, String name) {
        super(context, name, null, DB_VERSION);
    }

    /**
     * {@inheritDoc}
     *
     * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
     */
    @Override
    public void onCreate(SQLiteDatabase sqlitedatabase) {
        sqlitedatabase.execSQL("CREATE TABLE ALARM("
                + "YEAR INTEGER, MONTH INTEGER, DAY INTEGER, HOUR INTEGER NOT NULL, MIN INTEGER NOT NULL,"
                + " ENABLE INTEGER NOT NULL, DELETE_FLAG INTEGER NOT NULL,"
                + " PLAY_MODE INTEGER NOT NULL, RESOURCE_ID INTEFER, FILE_PATH STRING, VOL INTEGER NOT NULL,"
                + " FORCE_PLAY INTEGER NOT NULL, VIBRATE INTEGER NOT NULL" + ");");
        sqlitedatabase
                .execSQL("CREATE TABLE ALARM_CONDITION("
                        + "YEAR INTEGER, MONTH INTEGER, DAY INTEGER, HOUR INTEGER NOT NULL, MIN INTEGER NOT NULL, CODE INTEGER NOT NULL"
                        + ");");

        sqlitedatabase
                .execSQL("CREATE TABLE HOLIDAY( YEAR INTEGER NOT NULL, MONTH INTEGER NOT NULL, DAY INTEGER NOT NULL);");

        sqlitedatabase.execSQL("CREATE TABLE GROUP_MASTER(NAME STRING NOT NULL, ENABLE INTGER);");
        sqlitedatabase
                .execSQL("CREATE TABLE GROUP_LIST(GROUP_ID INTEGER NOT NULL, YEAR INTEGER, MONTH INTEGER, DAY INTEGER, HOUR INTEGER NOT NULL, MIN INTEGER NOT NULL);");
        ContentValues val = new ContentValues();
        val.put("NAME", GROUP_NAME_DEFAULT);
        sqlitedatabase.insert("GROUP_MASTER", null, val);
    }

    /**
     * {@inheritDoc}
     *
     * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase,
     *      int, int)
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqlitedatabase, int i, int j) {
        sqlitedatabase.execSQL("CREATE TABLE GROUP_MASTER(NAME STRING NOT NULL, ENABLE INTGER);");
        ContentValues val = new ContentValues();
        val.put("NAME", GROUP_NAME_DEFAULT);
        long rowId = sqlitedatabase.insert("GROUP_MASTER", null, val);
        sqlitedatabase
                .execSQL("CREATE TABLE GROUP_LIST(GROUP_ID INTEGER NOT NULL, YEAR INTEGER, MONTH INTEGER, DAY INTEGER, HOUR INTEGER NOT NULL, MIN INTEGER NOT NULL);");
        Cursor cursor = sqlitedatabase.query("ALARM", new String[] { "YEAR", "MONTH", "DAY", "HOUR", "MIN" }, null,
                null, null, null, null);
        while (cursor.moveToNext()) {
            val.clear();
            val.put("GROUP_ID", rowId);
            val.put("YEAR", cursor.getInt(0));
            val.put("MONTH", cursor.getInt(1));
            val.put("DAY", cursor.getInt(2));
            val.put("HOUR", cursor.getInt(3));
            val.put("MIN", cursor.getInt(4));
            sqlitedatabase.insert("GROUP_LIST", null, val);
        }
    }

    /**
     * アラームをDBに登録する。
     *
     * @param db
     *            DBインスタンス
     * @param alarm
     *            登録対象アラーム
     * @return rowId
     */
    public long insert(SQLiteDatabase db, Alarm alarm) {
        long ret = 0;
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(AlarmProviderConsts.YEAR, 1900);
            values.put(AlarmProviderConsts.MONTH, 1);
            values.put(AlarmProviderConsts.DAY, 1);
            values.put(AlarmProviderConsts.HOUR, alarm.getHour());
            values.put(AlarmProviderConsts.MIN, alarm.getMin());
            values.put(AlarmProviderConsts.ENABLE, Util.BooleanToDbBoolean(alarm.isEnable()));
            values.put(AlarmProviderConsts.DELETE, AlarmProviderConsts.FALSE);
            values.put(AlarmProviderConsts.PLAY_MODE, Util.PlayModeToDbPlayMode(alarm.getMode()));
            values.put(AlarmProviderConsts.RESOURCE_ID, alarm.getResId());
            values.put(AlarmProviderConsts.FILE_PATH, alarm.getMusicFilePath());
            values.put(AlarmProviderConsts.VOL, alarm.getVol());
            values.put(AlarmProviderConsts.FORCE_PLAY, Util.BooleanToDbBoolean(alarm.isForcePlay()));
            values.put(AlarmProviderConsts.VIBRATE, Util.BooleanToDbBoolean(alarm.isVibrate()));
            ret = db.insert("ALARM", null, values);

            SQLiteStatement stmt = db.compileStatement("INSERT into ALARM_CONDITION values(?, ?, ?, ?, ?, ?);");

            for (EnableCode code : alarm.getEnableCode()) {
                stmt.bindLong(1, 1900);
                stmt.bindLong(2, 1);
                stmt.bindLong(3, 1);
                stmt.bindLong(4, alarm.getHour());
                stmt.bindLong(5, alarm.getMin());
                stmt.bindLong(6, Util.enableCodeToDbCode(code));
                stmt.executeInsert();
            }

            values.clear();
            values.put(AlarmProviderConsts.YEAR, 1900);
            values.put(AlarmProviderConsts.MONTH, 1);
            values.put(AlarmProviderConsts.DAY, 1);
            values.put(AlarmProviderConsts.HOUR, alarm.getHour());
            values.put(AlarmProviderConsts.MIN, alarm.getMin());
            for (Integer i : alarm.getGroupIdList()) {
                values.put(AlarmProviderConsts.GROUP, i);
                db.insert("GROUP_LIST", null, values);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return ret;
    }

    /**
     * 削除フラグ以外のアラームの基本情報をすべて取得する。
     *
     * @param db
     *            DBオブジェクト
     * @return アラーム基本情報のカーソル
     */
    public Cursor selectWithoutDeleteFlagFromAlarm(SQLiteDatabase db) {
        return db
                .rawQuery(
                        "SELECT rowid _id, YEAR, MONTH, DAY, HOUR, MIN, ENABLE, PLAY_MODE, RESOURCE_ID, FILE_PATH, VOL, FORCE_PLAY, VIBRATE FROM alarm"
                                + " ORDER BY YEAR, MONTH, DAY, HOUR, MIN;", null);
    }

    /**
     * 起動フラグ以外のアラームの基本情報をすべて取得する。
     *
     * @param db
     *            DBオブジェクト
     * @return アラーム基本情報のカーソル(起動フラグ除く)
     */
    public Cursor selectWithoutEnableFromAlarm(SQLiteDatabase db) {
        return db
                .rawQuery(
                        "SELECT rowid _id, YEAR, MONTH, DAY, HOUR, MIN, DELETE_FLAG, PLAY_MODE, RESOURCE_ID, FILE_PATH, VOL, FORCE_PLAY, VIBRATE FROM alarm"
                                + " ORDER BY YEAR, MONTH, DAY, HOUR, MIN;", null);
    }

    /**
     * 指定したアラームの起動条件を取得する。
     *
     * @param db
     *            DBオブジェクト
     * @param alarm
     *            検索条件(時間、分)
     * @return 起動条件のカーソル
     */
    public Cursor selectAllFromAlarmCondition(SQLiteDatabase db, Alarm alarm) {
        String[] condition = { String.valueOf(1900), String.valueOf(1), String.valueOf(1),
                String.valueOf(alarm.getHour()), String.valueOf(alarm.getMin()) };

        return db.rawQuery("SELECT rowid _id, CODE FROM alarm_condition WHERE " + CONDITON_FOR_ONE_ALARM
                + " ORDER BY CODE;", condition);
    }

    /**
     * 指定したアラームのrowidを取得する。
     *
     * @param db
     *            DBオブジェクト
     * @param alarm
     *            検索条件(時間、分)
     * @return 起動条件のカーソル
     */
    public Cursor selectRowId(SQLiteDatabase db, Alarm alarm) {
        String[] condition = { String.valueOf(1900), String.valueOf(1), String.valueOf(1),
                String.valueOf(alarm.getHour()), String.valueOf(alarm.getMin()) };

        return db.rawQuery("SELECT rowid _id FROM alarm WHERE " + CONDITON_FOR_ONE_ALARM, condition);
    }

    /**
     * アラームの有効/無効を更新する。
     *
     * @param db
     *            DBオブジェクト
     * @param enable
     *            有効/無効
     * @param condition
     *            更新条件。hh:mm～であること。
     * @return 更新件数
     */
    public int updateEnable(SQLiteDatabase db, boolean enable, String condition) {
        String[] cond = Util.getConditoinForOneRecFromStr(condition);
        ContentValues content = new ContentValues();
        if (enable) {
            content.put("ENABLE", AlarmProviderConsts.TRUE);
        } else {
            content.put("ENABLE", AlarmProviderConsts.FALSE);
        }

        return db.update("alarm", content, CONDITON_FOR_ONE_ALARM, cond);
    }

    /**
     * アラームの削除フラグを更新する。
     *
     * @param db
     *            DBオブジェクト
     * @param enable
     *            有効/無効
     * @param condition
     *            更新条件。hh:mm～であること。
     */
    public int updateDeleteFlag(SQLiteDatabase db, boolean enable, String condition) {
        String[] cond = Util.getConditoinForOneRecFromStr(condition);
        ContentValues content = new ContentValues();
        if (enable) {
            content.put("DELETE_FLAG", AlarmProviderConsts.TRUE);
        } else {
            content.put("DELETE_FLAG", AlarmProviderConsts.FALSE);
        }

        return db.update("alarm", content, CONDITON_FOR_ONE_ALARM, cond);
    }

    /**
     * アラームの削除フラグをリセットする。
     *
     * @param db
     *            DBオブジェクト
     */
    public int resetDeleteFlag(SQLiteDatabase db) {
        ContentValues content = new ContentValues();
        content.put("DELETE_FLAG", AlarmProviderConsts.FALSE);

        return db.update("alarm", content, null, null);
    }

    /**
     * アラームを削除する。
     *
     * @param db
     *            DBオブジェクト
     * @param alarm
     *            削除条件(時間、分)
     * @return 削除件数
     */
    public int deleteAlarm(SQLiteDatabase db, Alarm alarm) {

        String[] condition = { String.valueOf(1900), String.valueOf(1), String.valueOf(1),
                String.valueOf(alarm.getHour()), String.valueOf(alarm.getMin()) };

        int ret = 0;
        try {
            db.beginTransaction();
            ret = db.delete("alarm", CONDITON_FOR_ONE_ALARM, condition);
            db.delete("alarm_condition", CONDITON_FOR_ONE_ALARM, condition);
            db.delete("group_list", CONDITON_FOR_ONE_ALARM, condition);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return ret;
    }

    /**
     * 指定した条件に一致する休日のレコードのカーソルを返す。
     *
     * @param db
     *            DBオブジェクト
     * @param date
     *            対象日
     * @return カーソル
     */
    public Cursor selectHoliday(SQLiteDatabase db, Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        return db.query(
                "HOLIDAY",
                new String[] { "rowid" },
                "year = ? AND month = ? AND day = ?",
                new String[] { String.valueOf(cal.get(Calendar.YEAR)), String.valueOf(cal.get(Calendar.MONTH)),
                        String.valueOf(cal.get(Calendar.DAY_OF_MONTH)) }, null, null, null);
    }

    /**
     * 休日を全件取得する。
     *
     * @param db
     *            DBオブジェクト
     * @return 休日を全件取得したカーソル
     */
    public Cursor selectAllHoliday(SQLiteDatabase db) {
        return db.query("HOLIDAY", new String[] { "rowid _id", "YEAR", "MONTH", "DAY" }, null, null, null, null, null);
    }

    /**
     * 休日を削除する。
     *
     * @param db
     *            DBオブジェクト
     * @param date
     *            削除対象日
     * @return 削除件数
     */
    public int deleteHoliday(SQLiteDatabase db, Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        return db.delete(
                "HOLIDAY",
                "year = ? AND month = ? AND day = ?",
                new String[] { String.valueOf(cal.get(Calendar.YEAR)), String.valueOf(cal.get(Calendar.MONTH)),
                        String.valueOf(cal.get(Calendar.DAY_OF_MONTH)) });
    }

    /**
     * 休日を追加する。
     *
     * @param db
     *            DBインスタンス
     * @param date
     *            追加する日
     * @return rowId rowId
     */
    public long insertHoliday(SQLiteDatabase db, Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        ContentValues values = new ContentValues();
        values.put(AlarmProviderConsts.YEAR, cal.get(Calendar.YEAR));
        values.put(AlarmProviderConsts.MONTH, cal.get(Calendar.MONTH));
        values.put(AlarmProviderConsts.DAY, cal.get(Calendar.DAY_OF_MONTH));
        return db.insert("HOLIDAY", null, values);

    }

    /**
     * 全てのグループ名を取得する。
     *
     * @param db
     *            DBインスタンス
     * @return グループ名
     */
    public Cursor selectAllGroup(SQLiteDatabase db) {
        return db.query("GROUP_MASTER", new String[] { "rowid _id", "NAME", "ENABLE" }, null, null, null, null, "NAME");
    }

    /**
     * 指定したグループIDに属するアラーム情報を取得する。
     *
     * @param db
     *            DBインスタンス
     * @param groupId
     *            グループID
     * @return グループIDに属するアラーム情報
     */
    public Cursor selectAlarmBelongToGroup(SQLiteDatabase db, String groupId) {
        return db
                .rawQuery(
                        "SELECT a.rowid _id, a.YEAR, a.MONTH, a.DAY, a.HOUR, a.MIN, a.ENABLE, a.PLAY_MODE, a.RESOURCE_ID, a.FILE_PATH, a.VOL, a.FORCE_PLAY, a.VIBRATE FROM alarm a, group_list g "
                                + "WHERE a.YEAR = g.YEAR AND a.month = g.month AND a.day = g.day AND a.hour = g.hour AND a.min = g.min"
                                + " AND g.group_id = ? ORDER BY a.YEAR, a.MONTH, a.DAY, a.HOUR, a.MIN;",
                        new String[] { groupId });
    }

    /**
     * グループ名をキーにレコードを取り出す。
     *
     * @param db
     *            DBインスタンス
     * @param groupName
     *            グループ名
     * @return
     */
    public Cursor selectGroup(SQLiteDatabase db, String groupName) {
        return db.query("GROUP_MASTER", new String[] { "rowid", "NAME" }, "NAME = ?", new String[] { groupName }, null,
                null, null);
    }

    /**
     * グループを追加する。
     *
     * @param db
     *            DBインスタンス
     * @param groupName
     *            グループ名
     * @return rowId
     */
    public long insertGroup(SQLiteDatabase db, ContentValues groupName) {
        return db.insert("GROUP_MASTER", null, groupName);
    }

    /**
     * グループ名を更新する。
     *
     * @param db
     *            DBインスタンス
     * @param groupName
     *            グループ名
     * @return 更新行数
     */
    public int updateGroup(SQLiteDatabase db, String rowId, ContentValues groupName) {
        return db.update("GROUP_MASTER", groupName, "rowid = ?", new String[] { rowId });
    }

    /**
     * グループを削除する。
     *
     * @param db
     *            DBインスタンス
     * @param rowId
     *            ID
     * @return 削除件数
     */
    public int deleteGroup(SQLiteDatabase db, String rowId) {
        return db.delete("GROUP_MASTER", "rowid = ?", new String[] { rowId });
    }

    /**
     * グループ毎のアラーム有効/無効を更新する。
     *
     * @param db
     *            DBインスタンス
     * @param id
     *            検索キー
     * @param isEnable
     *            有効にするならtrue
     * @return 更新件数
     */
    public int updateGroupEnable(SQLiteDatabase db, long id, boolean isEnable) {
        ContentValues values = new ContentValues();
        values.put(AlarmProviderConsts.ENABLE, Util.BooleanToDbBoolean(isEnable));
        return db.update("GROUP_MASTER", values, "rowid = ?", new String[] { String.valueOf(id) });
    }

    /**
     * アラームをグループに追加する。
     *
     * @param db
     *            DBインスタンス
     * @param val
     *            追加内容
     * @return rowid
     */
    public long insertGroupList(SQLiteDatabase db, ContentValues val) {
        return db.insert("GROUP_LIST", null, val);
    }

    /**
     * グループリストから指定されたアラームを全て削除する。
     *
     * @param db
     *            DBインスタンス
     * @param alarm
     *            削除対象アラーム
     * @return 削除件数
     */
    public int deleteAlarmFromGroup(SQLiteDatabase db, Alarm alarm) {
        String[] condition = { String.valueOf(1900), String.valueOf(1), String.valueOf(1),
                String.valueOf(alarm.getHour()), String.valueOf(alarm.getMin()) };

        return db.delete("GROUP_LIST", CONDITON_FOR_ONE_ALARM, condition);
    }

    /**
     * アラームが属するグループのカーソルを取得する。
     *
     * @param db
     *            DBインスタンス
     * @param alarm
     *            グループを求めたいアラーム
     * @return 所属するグループのカーソル
     */
    public Cursor selectGroupFromAlarm(SQLiteDatabase db, Alarm alarm) {

        return db.rawQuery("SELECT gl.group_id, g.name FROM group_list gl, group_master g "
                + "WHERE gl.group_id = g.rowid AND gl.YEAR = ? AND gl.month = ? AND gl.day = ? AND gl.hour = ?"
                + "AND gl.min = ? ORDER BY gl.GROUP_ID",
                new String[] { "1900", "1", "1", String.valueOf(alarm.getHour()), String.valueOf(alarm.getMin()) });
    }
}
