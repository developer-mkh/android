package jp.gr.java_conf.mkh.wordbook.util;

import android.content.ContentResolver;
import android.database.Cursor;
import android.widget.ListView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import jp.gr.java_conf.mkh.wordbook.contentprovider.WordBookContentProvider;
import jp.gr.java_conf.mkh.wordbook.entity.ViewInfoEntity;
import jp.gr.java_conf.mkh.wordbook.entity.WordBookEntity;

/**
 * 雑多なユーティリティクラス。
 */
public class Util {

    /**
     * インスタンス化禁止
     */
    private Util() {
    }

    /**
     * 呼び出し時の日付を返す。
     *
     * @return 日付(yyyymmdd形式)
     */
    public static String getDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        return sdf.format(new Date());
    }

    public static String formatDate(String date, String format, String parseFormat)
            throws ParseException {
        SimpleDateFormat sdfForParse = new SimpleDateFormat(parseFormat);
        SimpleDateFormat sdfForFormat = new SimpleDateFormat(format);
        return sdfForFormat.format(sdfForParse.parse(date));
    }

    /**
     * 画面表示状態を保存する。
     *
     * @param orientation     画面の向き(Configurationクラス参照)
     * @param listView        保存対象のListView
     * @param contentResolver コンテンツリゾルバ
     */
    public static void saveViewInfo(int orientation, ListView listView, ContentResolver contentResolver) {
        ViewInfoEntity entity = new ViewInfoEntity();

        entity.setOrientation(orientation);
        entity.setSelectedItemPosition(listView.getSelectedItemPosition());
        entity.setTopPosition(listView.getFirstVisiblePosition());
        int showItemNum = 0;
        if (listView.getChildAt(0) != null) {
            showItemNum = listView.getChildAt(0).getTop();
        }
        entity.setShowItemNum(showItemNum);

        contentResolver.update(WordBookContentProvider.CONTENTS_URI_VIEW_INFO, entity.getContentValues(), "ORIENTATION=?", new String[]{String.valueOf(orientation)});
    }

    /**
     * 画面表示状態を読み込む。
     *
     * @param orientation     画面の向き(Configurationクラス参照)
     * @param listView        読み込み対象のListView
     * @param contentResolver コンテンツリゾルバ
     */
    public static void loadViewInfo(int orientation, ListView listView, ContentResolver contentResolver) {

        Cursor c = contentResolver.query(WordBookContentProvider.CONTENTS_URI_VIEW_INFO,
                WordBookContentProvider.VIEW_INFO_ALL_COLUMNS,
                "ORIENTATION=?",
                new String[]{String.valueOf(orientation)},
                null);

        try {
            if (c.moveToNext()) {
                listView.setSelection(c.getInt(c.getColumnIndex("SELECTED_ITEM_POSITION")));
                listView.setSelectionFromTop(c.getInt(c.getColumnIndex("TOP_POSITION")), c.getInt(c.getColumnIndex("SHOW_ITEM_NUM")));
            } else {
                throw new IllegalStateException("There is no saved viewInfo");
            }
        } finally {
            c.close();
        }
    }

    /**
     * カーソルからmoveToNextして得られる結果を、単語帳エンティティにして返す。
     *
     * @param c カーソル
     * @return 単語帳エンティティ。カーソルが空だった場合はnull。
     */
    public static WordBookEntity cursorToWordbookEntity(Cursor c) {
        WordBookEntity ret = new WordBookEntity();
        if (c.moveToNext()) {
            ret.setId(c.getColumnIndex("_id") >= 0 ? c.getLong(c.getColumnIndex("_id")) : null);
            ret.setKana(c.getColumnIndex("KANA") >= 0 ? c.getString(c.getColumnIndex("KANA")) : null);
            ret.setWord(c.getColumnIndex("WORD") >= 0 ? c.getString(c.getColumnIndex("WORD")) : null);
            ret.setCategory(c.getColumnIndex("CATEGORY") >= 0 ? c.getString(c.getColumnIndex("CATEGORY")) : null);
            ret.setMeaning(c.getColumnIndex("MEANING") >= 0 ? c.getString(c.getColumnIndex("MEANING")) : null);
            ret.setTrainingTarget(c.getColumnIndex("TRAINING_TARGET") >= 0 ? c.getInt(c.getColumnIndex("TRAINING_TARGET")) : -1);
            ret.setOther(c.getColumnIndex("OTHER") >= 0 ? c.getString(c.getColumnIndex("OTHER")) : null);
            ret.setInsertedDate(c.getColumnIndex("INSERTED_DATE") >= 0 ? c.getString(c.getColumnIndex("INSERTED_DATE")) : null);
            ret.setUpdatedDate(c.getColumnIndex("UPDATED_DATE") >= 0 ? c.getString(c.getColumnIndex("UPDATED_DATE")) : null);
        } else {
            ret = null;
        }
        return ret;
    }

    /**
     * SQLiteで使用する特殊文字を"$"でエスケープする。
     * @param query 対象文字列
     * @return エスケープ後文字列
     */
    public static String escapeQueryString(String query) {
        query = query.replaceAll("\\$", "\\$\\$");
        query = query.replaceAll("%", "\\$%");
        query = query.replaceAll("_", "\\$_");
        return query;
    }

    /**
     * 最初に出てくる"。"もしくは”.”までの部分文字列を返す。
     * "。"を優先する。どちらも見つからなかったら、そのまま返す。
     * @param string 対象文字列
     * @return 部分文字列
     */
    public static String getFirstSentence(String string) {
        int i = string.indexOf("。");
        if (i < 0) {
            i = string.indexOf(".");
        }

        if (i < 0) {
            // どちらの文字列も見つからなかったら、そのまま返す。
            return string;
        }
        return string.substring(0, i);
    }
}
