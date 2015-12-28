package jp.gr.java_conf.mkh.alarm.content;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import jp.gr.java_conf.mkh.alarm.AlarmActivity;
import jp.gr.java_conf.mkh.alarm.db.AlarmDBHelper;
import jp.gr.java_conf.mkh.alarm.model.Alarm;
import jp.gr.java_conf.mkh.alarm.model.Alarm.EnableCode;
import jp.gr.java_conf.mkh.alarm.receiver.AlarmReceiver.PlayMode;
import jp.gr.java_conf.mkh.alarm.util.Util;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * アラーム情報のプロバイダ
 *
 * @author mkh
 *
 */
public class AlarmProvider extends ContentProvider {

    private AlarmDBHelper dbHelper;

    private static final UriMatcher uriMatcher;

    /** ALARMテーブルから削除フラグ以外を取得する。 */
    private static final int ALARM = 1;
    /** ALARM_CONDITIONテーブルから取得する。 */
    private static final int ALARM_CONDITION = 2;
    /** 起動設定フラグを更新する。 */
    private static final int UPDATE_ENABLE = 3;
    /** 削除する。 */
    private static final int DELETE = 4;
    /** ALARMテーブルから起動設定以外を取得する。 */
    private static final int ALARM_WITH_DELETE_FLAG = 5;
    /** 削除フラグを更新する。 */
    private static final int UPDATE_DELETE = 6;
    /** 削除フラグをリセットする。 */
    private static final int RESET_DELETE = 7;
    /** rowidを取得する。 */
    private static final int ALARM_ROWID = 8;
    /** 休日判定 */
    private static final int SELCECT_HOLIDAY = 9;
    /** 休日テーブルに挿入する */
    private static final int INSERT_HOLIDAY = 10;
    /** 休日テーブルから削除する */
    private static final int DELETE_HOLIDAY = 11;
    /** 休日テーブルから全件取得する。 */
    private static final int SELECT_ALL_HOLIDAY = 12;
    /** アラームを追加する */
    private static final int INSERT_ALARM = 13;
    /** グループに属するアラーム情報を取得する。 */
    private static final int SELECT_ALARM_BELONG_TO_GROUP = 14;
    /** グループを全件取得する */
    private static final int SELECT_GROUP = 15;
    /** グループ追加のURI */
    private static final int INSERT_GROUP = 16;
    /** グループ更新のURI */
    private static final int UPDATE_GROUP = 17;
    /** グループ削除のURI */
    private static final int DELETE_GROUP = 18;
    /** グループ名指定のURI */
    private static final int SELECT_GROUP_BY_NAME = 19;
    /** アラームをグループへ追加するURI */
    private static final int INSERT_GROUP_LIST = 20;
    /** グループからアラームを削除するURI */
    private static final int DELETE_ALARM_FROM_GROUP = 21;
    /** アラームが属するグループ情報を取得する。 */
    private static final int SELECT_GROUP_BELONG_TO_ALARM = 22;
    /** グループ毎の有効、無効を更新する。 */
    private static final int UPDATE_GROUP_ENABLE = 23;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(AlarmProviderConsts.AUTHORITY, "alarm", ALARM);
        uriMatcher.addURI(AlarmProviderConsts.AUTHORITY, "alarm_with_delete_flag", ALARM_WITH_DELETE_FLAG);
        uriMatcher.addURI(AlarmProviderConsts.AUTHORITY, "alarm_condition", ALARM_CONDITION);
        uriMatcher.addURI(AlarmProviderConsts.AUTHORITY, "delete", DELETE);
        uriMatcher.addURI(AlarmProviderConsts.AUTHORITY, "update_enable", UPDATE_ENABLE);
        uriMatcher.addURI(AlarmProviderConsts.AUTHORITY, "update_delete", UPDATE_DELETE);
        uriMatcher.addURI(AlarmProviderConsts.AUTHORITY, "reset_delete", RESET_DELETE);
        uriMatcher.addURI(AlarmProviderConsts.AUTHORITY, "alarm_rowid_only", ALARM_ROWID);
        uriMatcher.addURI(AlarmProviderConsts.AUTHORITY, "select_holiday", SELCECT_HOLIDAY);
        uriMatcher.addURI(AlarmProviderConsts.AUTHORITY, "insert_holiday", INSERT_HOLIDAY);
        uriMatcher.addURI(AlarmProviderConsts.AUTHORITY, "delete_holiday", DELETE_HOLIDAY);
        uriMatcher.addURI(AlarmProviderConsts.AUTHORITY, "holiday", SELECT_ALL_HOLIDAY);
        uriMatcher.addURI(AlarmProviderConsts.AUTHORITY, "insert", INSERT_ALARM);
        uriMatcher.addURI(AlarmProviderConsts.AUTHORITY, "alarm_belong_to_group", SELECT_ALARM_BELONG_TO_GROUP);
        uriMatcher.addURI(AlarmProviderConsts.AUTHORITY, "group", SELECT_GROUP);
        uriMatcher.addURI(AlarmProviderConsts.AUTHORITY, "group_insert", INSERT_GROUP);
        uriMatcher.addURI(AlarmProviderConsts.AUTHORITY, "group_update", UPDATE_GROUP);
        uriMatcher.addURI(AlarmProviderConsts.AUTHORITY, "group_delete", DELETE_GROUP);
        uriMatcher.addURI(AlarmProviderConsts.AUTHORITY, "group_select", SELECT_GROUP_BY_NAME);
        uriMatcher.addURI(AlarmProviderConsts.AUTHORITY, "update_group_enable", UPDATE_GROUP_ENABLE);
        uriMatcher.addURI(AlarmProviderConsts.AUTHORITY, "insert_group_list", INSERT_GROUP_LIST);
        uriMatcher.addURI(AlarmProviderConsts.AUTHORITY, "delete_alarm_from_group", DELETE_ALARM_FROM_GROUP);
        uriMatcher.addURI(AlarmProviderConsts.AUTHORITY, "group_belong_to_alarm", SELECT_GROUP_BELONG_TO_ALARM);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new AlarmDBHelper(getContext(), AlarmActivity.DB_FILE);
        return true;
    }

    @Override
    public int delete(Uri uri, String s, String[] as) {

        int ret = 0;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Alarm alarm = null;
        switch (uriMatcher.match(uri)) {
        case DELETE:
            alarm = new Alarm(Integer.parseInt(as[3]), Integer.parseInt(as[4]));
            ret = dbHelper.deleteAlarm(db, alarm);
            getContext().getContentResolver()
                    .notifyChange(AlarmProviderConsts.CONTENT_URI_ALARM_WITH_DELETE_FLAG, null);
            getContext().getContentResolver().notifyChange(AlarmProviderConsts.CONTENT_URI_ALARM, null);
            getContext().getContentResolver().notifyChange(AlarmProviderConsts.CONTENT_URI_ALARM_BELONG_TO_GROUP, null);
            break;
        case DELETE_HOLIDAY:
            Calendar cal = Calendar.getInstance();
            cal.set(Integer.parseInt(as[0]), Integer.parseInt(as[1]), Integer.parseInt(as[2]));
            ret = dbHelper.deleteHoliday(db, cal.getTime());
            getContext().getContentResolver().notifyChange(AlarmProviderConsts.CONTENT_URI_HOLIDAY, null);
            getContext().getContentResolver().notifyChange(AlarmProviderConsts.CONTENT_URI_SELECT_HOLIDAY, null);
            break;
        case DELETE_GROUP:
            ret = dbHelper.deleteGroup(db, as[0]);
            getContext().getContentResolver().notifyChange(AlarmProviderConsts.CONTENT_URI_GROUP, null);
            break;
        case DELETE_ALARM_FROM_GROUP:
            alarm = new Alarm(Integer.parseInt(as[3]), Integer.parseInt(as[4]));
            dbHelper.deleteAlarmFromGroup(db, alarm);
            getContext().getContentResolver().notifyChange(AlarmProviderConsts.CONTENT_URI_ALARM_BELONG_TO_GROUP, null);
            break;
        default:
            throw new IllegalArgumentException("wrong uri:" + uri);
        }

        return ret;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
        case ALARM:
            return AlarmProviderConsts.CONTENT_TYPE;
        case ALARM_CONDITION:
            return AlarmProviderConsts.CONTENT_ITEM_TYPE;
        default:
            throw new IllegalArgumentException("unknown URI:" + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentvalues) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Uri ret = null;
        long rowId;

        switch (uriMatcher.match(uri)) {
        case INSERT_ALARM:
            Alarm alarm = contentValuesToAlarm(contentvalues);
            rowId = dbHelper.insert(db, alarm);
            getContext().getContentResolver().notifyChange(uri, null);
            ret = ContentUris.withAppendedId(AlarmProviderConsts.CONTENT_URI_ALARM, rowId);
            getContext().getContentResolver()
                    .notifyChange(AlarmProviderConsts.CONTENT_URI_ALARM_WITH_DELETE_FLAG, null);
            getContext().getContentResolver().notifyChange(AlarmProviderConsts.CONTENT_URI_ALARM, null);
            getContext().getContentResolver().notifyChange(AlarmProviderConsts.CONTENT_URI_ALARM_BELONG_TO_GROUP, null);
            break;
        case INSERT_HOLIDAY:
            Calendar cal = Calendar.getInstance();
            cal.set(contentvalues.getAsInteger(AlarmProviderConsts.YEAR),
                    contentvalues.getAsInteger(AlarmProviderConsts.MONTH),
                    contentvalues.getAsInteger(AlarmProviderConsts.DAY));
            rowId = dbHelper.insertHoliday(db, cal.getTime());
            ret = ContentUris.withAppendedId(AlarmProviderConsts.CONTENT_URI_HOLIDAY, rowId);
            getContext().getContentResolver().notifyChange(AlarmProviderConsts.CONTENT_URI_HOLIDAY, null);
            getContext().getContentResolver().notifyChange(AlarmProviderConsts.CONTENT_URI_SELECT_HOLIDAY, null);
            break;
        case INSERT_GROUP:
            rowId = dbHelper.insertGroup(db, contentvalues);
            ret = ContentUris.withAppendedId(AlarmProviderConsts.CONTENT_URI_GROUP, rowId);
            getContext().getContentResolver().notifyChange(AlarmProviderConsts.CONTENT_URI_GROUP, null);
            break;
        case INSERT_GROUP_LIST:
            rowId = dbHelper.insertGroupList(db, contentvalues);
            getContext().getContentResolver().notifyChange(AlarmProviderConsts.CONTENT_URI_ALARM_BELONG_TO_GROUP, null);
            break;
        default:
            throw new IllegalArgumentException("wrong uri:" + uri);
        }

        return ret;
    }

    /**
     * ContentValuesからAlarmクラスに変換する。
     *
     * @param contentValues
     *            変換元のContentValues
     * @return
     */
    private Alarm contentValuesToAlarm(ContentValues contentValues) {
        int hour = contentValues.getAsInteger(AlarmProviderConsts.HOUR);
        int min = contentValues.getAsInteger(AlarmProviderConsts.MIN);
        PlayMode playMode = Util.dbPlayModeToPlayMode(contentValues.getAsInteger(AlarmProviderConsts.PLAY_MODE));
        int soundResId = contentValues.getAsInteger(AlarmProviderConsts.RESOURCE_ID);
        String soundFileName = contentValues.getAsString(AlarmProviderConsts.FILE_PATH);
        List<EnableCode> code = new ArrayList<Alarm.EnableCode>();
        for (EnableCode val : EnableCode.values()) {
            if (contentValues.containsKey(val.toString())) {
                code.add(val);
            }
        }
        List<Integer> group = new ArrayList<Integer>();
        Cursor c = query(AlarmProviderConsts.CONTENT_URI_GROUP, null, null, null, null);
        while (c.moveToNext()) {
            if (contentValues.containsKey(Integer.toString(c.getInt(0)))) {
                group.add(Integer.valueOf(c.getInt(0)));
            }
        }
        int vol = contentValues.getAsInteger(AlarmProviderConsts.VOL);
        boolean isForce = Util.DbBooleanToBoolean((contentValues.getAsInteger(AlarmProviderConsts.FORCE_PLAY)));
        boolean isVibrate = Util.DbBooleanToBoolean(contentValues.getAsInteger(AlarmProviderConsts.VIBRATE));

        Alarm ret = new Alarm(code, group, hour, min, true, playMode, soundResId, soundFileName, vol, isForce,
                isVibrate);
        return ret;
    }

    @Override
    public Cursor query(Uri uri, String[] as, String s, String[] as1, String s1) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor ret = null;
        switch (uriMatcher.match(uri)) {
        case ALARM:
            ret = dbHelper.selectWithoutDeleteFlagFromAlarm(db);
            break;
        case ALARM_CONDITION:
            ret = dbHelper.selectAllFromAlarmCondition(db,
                    new Alarm(Integer.parseInt(as1[3]), Integer.parseInt(as1[4])));
            break;
        case ALARM_WITH_DELETE_FLAG:
            ret = dbHelper.selectWithoutEnableFromAlarm(db);
            break;
        case ALARM_ROWID:
            ret = dbHelper.selectRowId(db, new Alarm(Integer.parseInt(as1[0]), Integer.parseInt(as1[1])));
            break;
        case SELECT_ALL_HOLIDAY:
            ret = dbHelper.selectAllHoliday(db);
            break;
        case SELCECT_HOLIDAY:
            Calendar cal = Calendar.getInstance();
            cal.set(Integer.parseInt(as1[0]), Integer.parseInt(as1[1]), Integer.parseInt(as1[2]));
            ret = dbHelper.selectHoliday(db, cal.getTime());
            break;
        case SELECT_ALARM_BELONG_TO_GROUP:
            ret = dbHelper.selectAlarmBelongToGroup(db, as1[0]);
            break;
        case SELECT_GROUP:
            ret = dbHelper.selectAllGroup(db);
            break;
        case SELECT_GROUP_BY_NAME:
            ret = dbHelper.selectGroup(db, as1[0]);
            break;
        case SELECT_GROUP_BELONG_TO_ALARM:
            ret = dbHelper.selectGroupFromAlarm(db, new Alarm(Integer.parseInt(as1[3]), Integer.parseInt(as1[4])));
            break;
        default:
            throw new IllegalArgumentException("unknown URI:" + uri);
        }

        ret.setNotificationUri(getContext().getContentResolver(), uri);
        return ret;
    }

    @Override
    public int update(Uri uri, ContentValues contentvalues, String s, String[] as) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int ret;
        switch (uriMatcher.match(uri)) {
        case UPDATE_ENABLE:
            ret = dbHelper.updateEnable(db, contentvalues.getAsBoolean(AlarmProviderConsts.ENABLE), s);
            getContext().getContentResolver().notifyChange(AlarmProviderConsts.CONTENT_URI_ALARM_BELONG_TO_GROUP, null);
            getContext().getContentResolver().notifyChange(AlarmProviderConsts.CONTENT_URI_GROUP, null);
            break;
        case UPDATE_DELETE:
            ret = dbHelper.updateDeleteFlag(db, contentvalues.getAsBoolean(AlarmProviderConsts.DELETE), s);
            break;
        case RESET_DELETE:
            ret = dbHelper.resetDeleteFlag(db);
            break;
        case UPDATE_GROUP:
            ret = dbHelper.updateGroup(db, as[0], contentvalues);
            getContext().getContentResolver().notifyChange(AlarmProviderConsts.CONTENT_URI_GROUP, null);
            break;
        case UPDATE_GROUP_ENABLE:
            ret = dbHelper.updateGroupEnable(db, Long.parseLong(as[0]),
                    contentvalues.getAsBoolean(AlarmProviderConsts.ENABLE));
            getContext().getContentResolver().notifyChange(AlarmProviderConsts.CONTENT_URI_GROUP, null);
            break;
        default:
            throw new IllegalArgumentException("unknown URI:" + uri);
        }

        getContext().getContentResolver().notifyChange(AlarmProviderConsts.CONTENT_URI_ALARM, null);
        getContext().getContentResolver().notifyChange(AlarmProviderConsts.CONTENT_URI_ALARM_WITH_DELETE_FLAG, null);
        return ret;
    }

}
