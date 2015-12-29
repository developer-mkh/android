package jp.gr.java_conf.mkh.alarm.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import jp.gr.java_conf.mkh.alarm.R;
import jp.gr.java_conf.mkh.alarm.content.AlarmProviderConsts;
import jp.gr.java_conf.mkh.alarm.ctrl.AlarmCtrl;
import jp.gr.java_conf.mkh.alarm.model.Alarm;
import jp.gr.java_conf.mkh.alarm.model.Alarm.EnableCode;
import jp.gr.java_conf.mkh.alarm.receiver.AlarmReceiver.PlayMode;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Environment;

public class Util {

    /**
     * ALARMテーブルから1件取得するための条件を、文字列から作成する。<br>
     * 今のところ年月日は固定。文字列は"xx:xx ～"となっている想定。
     *
     * @param str
     *            条件を作り出す文字列。
     * @return 検索条件
     */
    public static String[] getConditoinForOneRecFromStr(String str) {
        String[] cond = str.split(":");
        String hour = cond[0];
        String min = cond[1].substring(0, 2);
        min = min.startsWith("0") ? min.substring(1) : min;
        String[] ret = { "1900", "1", "1", hour, min };
        return ret;
    }

    /**
     * 1ケタの数字の左に0を付け加える。<br>
     * 2ケタ以上の場合は何もしない
     *
     * @param n
     *            入力値
     * @return 入力値が1ケタの場合、左側に0が付け足された文字列。その他の場合は、そのままの文字列。
     */
    public static String padZero(int n) {
        return n < 10 ? "0" + String.valueOf(n) : String.valueOf(n);
    }

    /**
     * グループ名とそのグループ内での表示位置からAlarmインスタンスを作成する。
     *
     * @param childPosition
     *            グループ内での表示位置
     * @param context
     *            コンテキスト
     * @param groupName
     *            グループ名
     * @return Alarmインスタンス
     */
    public static Alarm makeAlarmFromGroupName(int childPosition, Context context, String groupName) {
        ContentResolver cr = context.getContentResolver();
        Cursor groupCursor = cr.query(AlarmProviderConsts.CONTENT_URI_SELECT_GROUP, null, null,
                new String[] { groupName }, null);
        groupCursor.moveToFirst();
        String groupId = groupCursor.getString(0);
        Cursor alarmCursor = cr.query(AlarmProviderConsts.CONTENT_URI_ALARM_BELONG_TO_GROUP, null, null,
                new String[] { groupId }, null);
        alarmCursor.moveToPosition(childPosition);
        Alarm alarm = Util.makeAlarm(context, alarmCursor, null);
        return alarm;
    }

    /**
     * DBからAlarmインスタンスを作成する。
     *
     * @param context
     *            コンテキスト
     * @param isEnable
     *            このアラームが有効か。指定しない(null)場合、DBの情報が設定される
     * @param position
     *            カーソルの何番目から作成するか
     * @return Alarmインスタンス
     */
    public static Alarm makeAlarm(Context context, Boolean isEnable, int position) {
        Cursor cursor = context.getContentResolver().query(AlarmProviderConsts.CONTENT_URI_ALARM, null, null, null,
                null);
        cursor.moveToPosition(position);

        return makeAlarm(context, cursor, isEnable);
    }

    /**
     * DBからAlarmインスタンスを作成する。
     *
     * @param context
     *            コンテキスト
     * @param cursor
     *            DBから値を取得するためのカーソル
     * @param isEnable
     *            このアラームが有効か。指定しない(null)場合、DBの情報が設定される
     * @return Alarmインスタンス
     */
    public static Alarm makeAlarm(Context context, Cursor cursor, Boolean isEnable) {

        int year = cursor.getInt(1);
        int month = cursor.getInt(2);
        int day = cursor.getInt(3);
        int hour = cursor.getInt(4);
        int min = cursor.getInt(5);
        if (isEnable == null) {
            isEnable = cursor.getInt(6) == AlarmProviderConsts.TRUE ? true : false;
        }
        PlayMode mode = Util.dbPlayModeToPlayMode(cursor.getInt(7));
        int resId = cursor.getInt(8);
        String filePath = cursor.getString(9);
        int vol = cursor.getInt(10);
        boolean isForce = DbBooleanToBoolean(cursor.getInt(11));
        boolean isVibrate = DbBooleanToBoolean(cursor.getInt(12));

        String[] condition = { String.valueOf(year), String.valueOf(month), String.valueOf(day), String.valueOf(hour),
                String.valueOf(min) };

        ContentResolver contentResolver = context.getContentResolver();
        Cursor alarmCursor = contentResolver.query(AlarmProviderConsts.CONTENT_URI_ALARM_CONDITION, null, null,
                condition, null);
        List<EnableCode> alarmCondition = new ArrayList<Alarm.EnableCode>();
        while (alarmCursor.moveToNext()) {
            alarmCondition.add(dbCodeToEnableCode(alarmCursor.getInt(1)));
        }
        alarmCursor.close();

        Cursor groupCursor = getGroupList(contentResolver, cursor);
        List<Integer> groupList = new ArrayList<Integer>();
        while (groupCursor.moveToNext()) {
            groupList.add(groupCursor.getInt(0));
        }

        Alarm alarm = new Alarm(alarmCondition, groupList, hour, min, isEnable, mode, resId, filePath, vol, isForce,
                isVibrate);
        return alarm;
    }

    /**
     * アラームの有効/無効を設定する。
     *
     * @param context
     *            コンテキスト
     * @param isEnable
     *            有効にするときtrue
     * @param position
     *            何番目のアラームを有効にするか。
     */
    public static void changeAlarmEnableDisable(Context context, boolean isEnable, int position) {
        Cursor cursor = context.getContentResolver().query(AlarmProviderConsts.CONTENT_URI_ALARM, null, null, null,
                null);
        cursor.moveToPosition(position);
        changeAlarmEnableDisable(context, cursor, isEnable);
    }

    /**
     * アラームの有効/無効を設定する。
     *
     * @param context
     *            コンテキスト
     * @param cursor
     *            有効にするアラームデータ
     * @param isEnable
     *            有効にするときtrue
     */
    public static void changeAlarmEnableDisable(Context context, Cursor cursor, boolean isEnable) {

        Alarm alarm = Util.makeAlarm(context, cursor, isEnable);
        changeAlarmEnableDisable(context, alarm);

    }

    /**
     * アラームの有効/無効を設定する。
     *
     * @param context コンテキスト
     * @param alarm 有効/無効にするアラーム。有効/無効はこの引数に設定されている値に従う。
     */
    public static void changeAlarmEnableDisable(Context context, Alarm alarm) {

        ContentResolver cr = context.getContentResolver();

        ContentValues cv = new ContentValues();
        cv.put(AlarmProviderConsts.ENABLE, alarm.isEnable());
        StringBuffer sb = new StringBuffer();
        sb.append(alarm.getHour()).append(":").append(Util.padZero(alarm.getMin()));
        cr.update(AlarmProviderConsts.CONTENT_URI_UPDATE_ENABLE, cv, sb.toString(), null);

        AlarmCtrl alarmCtrl = new AlarmCtrl();
        alarmCtrl.setAlarm(alarm, context);

    }

    /**
     * DBに格納されているコード値から、EnableCodeに変換する。
     *
     * @param code
     *            コード値
     * @return 対応するEnableCode
     */
    public static EnableCode dbCodeToEnableCode(int code) {
        EnableCode ret = null;
        switch (code) {
        case 0:
            ret = EnableCode.SUN;
            break;
        case 1:
            ret = EnableCode.MON;
            break;
        case 2:
            ret = EnableCode.TUE;
            break;
        case 3:
            ret = EnableCode.WED;
            break;
        case 4:
            ret = EnableCode.THU;
            break;
        case 5:
            ret = EnableCode.FRI;
            break;
        case 6:
            ret = EnableCode.SAT;
            break;
        case 7:
            ret = EnableCode.EveryDay;
            break;
        case 8:
            ret = EnableCode.Weekday;
            break;
        default:
            throw new IllegalArgumentException("invalid EnableCode:" + code);
        }
        return ret;
    }

    /**
     * EnableCodeをDBに格納するコード値に変換する。
     *
     * @param code
     *            コード
     * @return 対応するコード値
     */
    public static long enableCodeToDbCode(EnableCode code) {
        long ret = 0;
        switch (code) {
        case SUN:
            ret = 0;
            break;
        case MON:
            ret = 1;
            break;
        case TUE:
            ret = 2;
            break;
        case WED:
            ret = 3;
            break;
        case THU:
            ret = 4;
            break;
        case FRI:
            ret = 5;
            break;
        case SAT:
            ret = 6;
            break;
        case EveryDay:
            ret = 7;
            break;
        case Weekday:
            ret = 8;
            break;
        default:
            throw new IllegalArgumentException("invalid EnableCode:" + code);
        }
        return ret;
    }

    /**
     * EnableCodeを表示用の文字列に変換する。
     *
     * @param code
     *            コード
     * @param ctx
     *            コンテキスト
     * @return 対応するコード値
     */
    public static String enableCodeToString(EnableCode code, Context ctx) {
        String ret;
        switch (code) {
        case SUN:
            ret = ctx.getResources().getString(R.string.sun);
            break;
        case MON:
            ret = ctx.getResources().getString(R.string.mon);
            break;
        case TUE:
            ret = ctx.getResources().getString(R.string.tue);
            break;
        case WED:
            ret = ctx.getResources().getString(R.string.wed);
            break;
        case THU:
            ret = ctx.getResources().getString(R.string.thu);
            break;
        case FRI:
            ret = ctx.getResources().getString(R.string.fri);
            break;
        case SAT:
            ret = ctx.getResources().getString(R.string.sat);
            break;
        case EveryDay:
            ret = ctx.getResources().getString(R.string.everyday);
            break;
        case Weekday:
            ret = ctx.getResources().getString(R.string.weekday);
            break;
        default:
            throw new IllegalArgumentException("invalid EnableCode:" + code);
        }
        return ret;
    }

    /**
     * EnableCodeを画面表示用の文字列に変換する。
     *
     * @param list
     *            EnableCodeのリスト
     * @param ctx
     *            コンテキスト
     * @return 文字列
     */
    public static String enableCodeToStringForView(List<EnableCode> list, Context ctx) {
        Collections.sort(list);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(enableCodeToString(list.get(i), ctx)).append(",");
        }
        return sb.length() > 0 ? sb.toString().substring(0, sb.length() - 1) : sb.toString();
    }

    /**
     * DBに格納されている値をPlayModeに変換する。
     *
     * @param dbPlayMode
     *            DBに格納されている値
     * @return PlayModeに変換した結果
     */
    public static PlayMode dbPlayModeToPlayMode(int dbPlayMode) {
        PlayMode ret = null;

        switch (dbPlayMode) {
        case 1:
            ret = PlayMode.PLAY_LOCAL_MUSIC_FILE;
            break;
        case 2:
            ret = PlayMode.PLAY_RESOURCE_MUSIC_FILE;
            break;
        case 3:
            ret = PlayMode.STOP_PLAY_MUSIC;
            break;
        default:
            throw new IllegalArgumentException("invalid PlayMode:" + dbPlayMode);
        }
        return ret;
    }

    /**
     * PlayModeの値をDBに格納するコードに変換する。
     *
     * @param mode
     *            PlayMode
     * @return 変換した値
     */
    public static int PlayModeToDbPlayMode(PlayMode mode) {
        int ret;
        switch (mode) {
        case PLAY_LOCAL_MUSIC_FILE:
            ret = 1;
            break;
        case PLAY_RESOURCE_MUSIC_FILE:
            ret = 2;
            break;
        case STOP_PLAY_MUSIC:
            ret = 3;
        default:
            throw new IllegalArgumentException("invalid PlayMode:" + mode);
        }

        return ret;
    }

    /**
     * booleanの値をDBに格納するコードに変換する。
     *
     * @param val
     *            変換元
     * @return 変換した値
     */
    public static int BooleanToDbBoolean(boolean val) {
        int ret = AlarmProviderConsts.FALSE;
        if (val) {
            ret = AlarmProviderConsts.TRUE;
        }
        return ret;
    }

    /**
     * DBに格納しているコードをbooleanの値に変換する。
     *
     * @param val
     *            変換元
     * @return 変換した値
     */
    public static boolean DbBooleanToBoolean(int val) {
        boolean ret = false;
        if (val == AlarmProviderConsts.TRUE) {
            ret = true;
        }
        return ret;
    }

    /**
     * 与えられた日が休日かどうか判定する。
     *
     * @param contentResolver
     *            コンテンツリゾルバ
     * @param year
     *            年
     * @param month
     *            月(0から始まる)
     * @param day
     *            日
     * @return 休日のときtrue
     */
    public static boolean isHoliday(ContentResolver contentResolver, int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        return isHoliday(contentResolver, cal.getTime());
    }

    /**
     * 与えられた日が休日かどうか判定する。
     *
     * @param contentResolver コンテンツリゾルバ
     * @param date
     *            判定対象年月日
     * @return 休日のときtrue
     */
    public static boolean isHoliday(ContentResolver contentResolver, Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        Cursor cursor = contentResolver.query(
                AlarmProviderConsts.CONTENT_URI_SELECT_HOLIDAY,
                null,
                null,
                new String[] { String.valueOf(cal.get(Calendar.YEAR)), String.valueOf(cal.get(Calendar.MONTH)),
                        String.valueOf(cal.get(Calendar.DAY_OF_MONTH)) }, null);

        return cursor.getCount() == 0 ? false : true;
    }

    /**
     * 付属のサウンドのリソースIDを表示名に変換する。
     *
     * @param ctx
     *            コンテキスト
     * @param soundResId
     *            リソースID
     * @return 表示名
     */
    public static String soundResIdToSoundName(Context ctx, int soundResId) {
        String ret = "";
        switch (soundResId) {
        case R.raw.alarm_001:
            ret = ctx.getResources().getStringArray(R.array.bundle_sound)[0];
            break;
        case R.raw.alarm_002:
            ret = ctx.getResources().getStringArray(R.array.bundle_sound)[1];
            break;
        case R.raw.alarm_003:
            ret = ctx.getResources().getStringArray(R.array.bundle_sound)[2];
            break;
        case R.raw.alarm_004:
            ret = ctx.getResources().getStringArray(R.array.bundle_sound)[3];
            break;
        case R.raw.alarm_005:
            ret = ctx.getResources().getStringArray(R.array.bundle_sound)[4];
            break;
        case R.raw.alarm_006:
            ret = ctx.getResources().getStringArray(R.array.bundle_sound)[5];
            break;
        case R.raw.alarm_007:
            ret = ctx.getResources().getStringArray(R.array.bundle_sound)[6];
            break;
        case R.raw.alarm_008:
            ret = ctx.getResources().getStringArray(R.array.bundle_sound)[7];
            break;
        }
        return ret;
    }

    /**
     * ファイルパスのディレクトリを取得する。SDカードの有無によって返却するパスを決定する。<br>
     * <ul>
     * <li>SDカードがある場合は、SDカード上のディレクトリを返却する。
     * <li>SDカードがない場合は、空文字を返却する。
     * </ul>
     *
     * @param claszz
     *            ディレクトリ名となるクラス
     * @return ディレクトリのフルパス
     */
    public static String getFileBasePath(Class<?> claszz) {
        String sdCardStatus = Environment.getExternalStorageState();
        String ret = "";

        if (Environment.MEDIA_MOUNTED.equalsIgnoreCase(sdCardStatus)) {
            ret = Environment.getExternalStorageDirectory().getPath() + "/" + claszz.getPackage().getName() + "/";
        }

        return ret;
    }

    /**
     * 起動条件を格納したListを作成する。
     *
     * @param context
     *            コンテキスト
     * @param cursor
     *            起動条件を検索するためのキー情報が格納されたカーソル
     * @return 起動条件を格納したList
     */
    public static List<EnableCode> makeConditionList(Context context, Cursor cursor) {
        String[] condition = { cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4),
                cursor.getString(5) };

        ContentResolver cr = context.getContentResolver();
        Cursor resultFromCondition = cr.query(AlarmProviderConsts.CONTENT_URI_ALARM_CONDITION, null, null, condition,
                null);

        List<EnableCode> list = new ArrayList<EnableCode>();
        while (resultFromCondition.moveToNext()) {
            list.add(Util.dbCodeToEnableCode(resultFromCondition.getInt(1)));
        }

        resultFromCondition.close();
        return list;
    }

    /**
     * 表示用のグループリストを作成する。
     *
     * @param context
     *            コンテキスト
     * @param cursor
     *            起動条件を検索するためのキー情報が格納されたカーソル
     * @return グループ名のリスト
     */
    public static List<String> makeGroupListForView(Context context, Cursor cursor) {

        Cursor resultFromCondition = getGroupList(context.getContentResolver(), cursor);
        List<String> groupName = new ArrayList<String>();
        while (resultFromCondition.moveToNext()) {
            groupName.add(resultFromCondition.getString(1));
        }

        resultFromCondition.close();

        return groupName;
    }

    /**
     * アラームが所属しているグループの情報のカーソルを返す。
     *
     * @param cr
     *            コンテンツリゾルバ
     * @param cursor
     *            起動条件を検索するためのキー情報が格納されたカーソル
     * @return グループ情報のカーソル
     */
    private static Cursor getGroupList(ContentResolver cr, Cursor cursor) {
        String[] condition = { cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4),
                cursor.getString(5) };

        return cr.query(AlarmProviderConsts.CONTENT_URI_GROUP_BELONG_TO_ALARM, null, null, condition, null);
    }

    /**
     * 使用中の端末がタブレットかどうか判定する。
     *
     * @param ctx
     *            コンテキスト
     * @return タブレットのときtrue
     */
    public static boolean isTabletMode(Context ctx) {
        return ctx.getResources().getBoolean(R.bool.is_tablet);
    }
}