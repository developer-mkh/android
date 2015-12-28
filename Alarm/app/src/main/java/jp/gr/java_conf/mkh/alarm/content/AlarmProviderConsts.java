package jp.gr.java_conf.mkh.alarm.content;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * アラームコンテンツプロバイダの定数
 * @author mkh
 *
 */
public class AlarmProviderConsts implements BaseColumns {

    /** URIのauthority */
    public static final String AUTHORITY = "jp.gr.java_conf.mkh.alarm.provider.alarmprovider";
    /** アラームテーブル(削除フラグなし、起動設定あり)のURI */
    public static final Uri CONTENT_URI_ALARM = Uri.parse("content://" + AUTHORITY + "/alarm");
    /** アラームテーブル(削除フラグあり、起動設定なし)のURI */
    public static final Uri CONTENT_URI_ALARM_WITH_DELETE_FLAG = Uri.parse("content://" + AUTHORITY + "/alarm_with_delete_flag");
    /** アラームテーブル(rowid)のURI */
    public static final Uri CONTENT_URI_ALARM_ROWID_ONLY = Uri.parse("content://" + AUTHORITY + "/alarm_rowid_only");
    /** アラーム起動条件テーブルのURI */
    public static final Uri CONTENT_URI_ALARM_CONDITION = Uri.parse("content://" + AUTHORITY + "/alarm_condition");
    /** アラーム起動設定更新のURI */
    public static final Uri CONTENT_URI_UPDATE_ENABLE = Uri.parse("content://" + AUTHORITY + "/update_enable");
    /** アラーム削除フラグ更新のURI */
    public static final Uri CONTENT_URI_UPDATE_DELETE = Uri.parse("content://" + AUTHORITY + "/update_delete");
    /** アラーム追加のURI */
    public static final Uri CONTENT_URI_INSERT_NEW_ALARM = Uri.parse("content://" + AUTHORITY + "/insert");
    /** アラーム削除のURI */
    public static final Uri CONTENT_URI_DELETE = Uri.parse("content://" + AUTHORITY + "/delete");
    /** アラーム削除フラグのリセットのURI */
    public static final Uri CONTENT_URI_RESET_DELETE = Uri.parse("content://" + AUTHORITY + "/reset_delete");
    /** 休日テーブルのURI */
    public static final Uri CONTENT_URI_HOLIDAY = Uri.parse("content://" + AUTHORITY + "/holiday");
    /** 休日取得のURI */
    public static final Uri CONTENT_URI_SELECT_HOLIDAY = Uri.parse("content://" + AUTHORITY + "/select_holiday");
    /** 休日テーブル挿入のURI */
    public static final Uri CONTENT_URI_INSERT_HOLIDAY = Uri.parse("content://" + AUTHORITY + "/insert_holiday");
    /** 休日テーブル削除のURI */
    public static final Uri CONTENT_URI_DELETE_HOLIDAY = Uri.parse("content://" + AUTHORITY + "/delete_holiday");
    /** グループテーブルのURI */
    public static final Uri CONTENT_URI_GROUP = Uri.parse("content://" + AUTHORITY + "/group");
    /** グループ追加のURI */
    public static final Uri CONTENT_URI_INSERT_NEW_GROUP = Uri.parse("content://" + AUTHORITY + "/group_insert");
    /** グループ更新のURI */
    public static final Uri CONTENT_URI_UPDATE_GROUP = Uri.parse("content://" + AUTHORITY + "/group_update");
    /** グループ削除のURI */
    public static final Uri CONTENT_URI_DELETE_GROUP = Uri.parse("content://" + AUTHORITY + "/group_delete");
    /** グループ名指定の検索のURI */
    public static final Uri CONTENT_URI_SELECT_GROUP = Uri.parse("content://" + AUTHORITY + "/group_select");
    /** グループに属するアラームを取得するURI */
    public static final Uri CONTENT_URI_ALARM_BELONG_TO_GROUP = Uri.parse("content://" + AUTHORITY + "/alarm_belong_to_group");
    /** グループからアラームを削除するURI */
    public static final Uri CONTENT_URI_DELETE_ALARM_FROM_GROUP = Uri.parse("content://" + AUTHORITY + "/delete_alarm_from_group");
    /** グループ毎の有効、無効を更新するURI */
    public static final Uri CONTENT_URI_UPDATE_GROUP_ENABLE =  Uri.parse("content://" + AUTHORITY + "/update_group_enable");
    /** アラームをグループに追加するURI */
    public static final Uri CONTENT_URI_INSERT_GROUP_LIST = Uri.parse("content://" + AUTHORITY + "/insert_group_list");
    /** アラームが所属するグループのURI */
    public static final Uri CONTENT_URI_GROUP_BELONG_TO_ALARM = Uri.parse("content://" + AUTHORITY + "/group_belong_to_alarm");
    /** MIMEタイプ */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.developer.mkh.alarm";
    /** MIMEタイプ */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.developer.mkh.alarm";
    /** 年 */
    public static final String YEAR = "YEAR";
    /** 月 */
    public static final String MONTH = "MONTH";
    /** 日 */
    public static final String DAY = "DAY";
    /** 時間 */
    public static final String HOUR = "HOUR";
    /** 分 */
    public static final String MIN = "MIN";
    /** 有効/無効 */
    public static final String ENABLE = "ENABLE";
    /** 削除フラグ */
    public static final String DELETE = "DELETE_FLAG";
    /** 演奏モード */
    public static final String PLAY_MODE = "PLAY_MODE";
    /** 内蔵音源の場合のリソースID */
    public static final String RESOURCE_ID = "RESOURCE_ID";
    /** 外部音源の場合のファイルパス */
    public static final String FILE_PATH = "FILE_PATH";
    /** 起動条件 */
    public static final String CODE = "CODE";
    /** グループID */
    public static final String GROUP = "GROUP_ID";
    /** ボリューム */
    public static final String VOL = "VOL";
    /** マナーモードで音を出すか */
    public static final String FORCE_PLAY = "FORCE_PLAY";
    /** バイブを作動させるか */
    public static final String VIBRATE = "VIBRATE";
    /** グループ名 */
    public static final String GROUP_NAME = "NAME";
    /** true */
    public static final int TRUE = 1;
    /** false */
    public static final int FALSE = 0;

}
