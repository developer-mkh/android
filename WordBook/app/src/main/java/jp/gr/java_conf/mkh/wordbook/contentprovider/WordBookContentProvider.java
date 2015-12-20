package jp.gr.java_conf.mkh.wordbook.contentprovider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

import jp.gr.java_conf.mkh.wordbook.entity.ViewInfoEntity;
import jp.gr.java_conf.mkh.wordbook.entity.WordBookEntity;

/**
 * データベースアダプター
 */
public class WordBookContentProvider extends ContentProvider {

    /** Authority */
    private static final String AUTHORITY = "jp.gr.java_conf.mkh.wordbook";

    /** 単語帳テーブルのテーブル名 */
    private static final String PATH_WORDBOOK = "wordbook";

    /** 画面表示情報保持テーブルのテーブル名 */
    private static final String PATH_VIEW_INFO = "view_info";

    /** 単語帳テーブルのURI ID */
    private static final int ID_WORDBOOK = 1;

    /** 画面表示情報保持テーブルのURI ID */
    private static final int ID_VIEW_INFO = 2;

    /** TRUE */
    public static final int TRUE = 1;
    /** FALSE */
    public static final int FALSE = 0;

    /** 単語帳テーブルの全カラム */
    public static final String[] WORD_BOOK_ALL_COLUMNS = {"_id", "WORD", "KANA", "CATEGORY", "MEANING", "TRAINING_TARGET", "OTHER", "INSERTED_DATE", "UPDATED_DATE"};

    /** 表示状態保存テーブルの全カラム */
    public static final String[] VIEW_INFO_ALL_COLUMNS = {"_id", "ORIENTATION", "SELECTED_ITEM_POSITION", "TOP_POSITION", "SHOW_ITEM_NUM"};

    /**
     * 単語帳テーブルのコンテンツURI。
     */
    public  static final Uri CONTENTS_URI_WORDBOOK = Uri.parse(
            "content://" + AUTHORITY + "/" + PATH_WORDBOOK );

    /**
     * 画面表示情報保持テーブルのコンテンツURI。
     */
    public  static final Uri CONTENTS_URI_VIEW_INFO = Uri.parse(
            "content://" + AUTHORITY + "/" + PATH_VIEW_INFO);

    /**
     * distinctしたいときに指定するクエリパラメータのキー。<br>
     * 値は、Boolean.valueOfで評価される。
     */
    public static final String QUERY_PARAMETER_DISTINCT_KEY = "limitDistinct";

    /**
     * group byしたいときに指定するクエリパラメータのキー
     */
    public static final String QUERY_PARAMETER_GROUP_BY_KEY = "limitGroupBy";

    /**
     * limitつけたいときに指定するクエリパラメータのキー
     */
    public static final String QUERY_PARAMETER_LIMIT_KEY = "keyLimit";

    /** 指定したURIに対応する処理を判別するために使用する */
    private static UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, PATH_WORDBOOK, ID_WORDBOOK);
        uriMatcher.addURI(AUTHORITY, PATH_VIEW_INFO, ID_VIEW_INFO);
    }

    /** DBヘルパー */
    private DatabaseHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String tableName;
        switch (uriMatcher.match(uri)) {
            case ID_WORDBOOK:
                tableName = PATH_WORDBOOK;
                break;
            case ID_VIEW_INFO:
                tableName = PATH_VIEW_INFO;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI:" + uri);
        }

        boolean distinct = Boolean.valueOf(uri.getQueryParameter(QUERY_PARAMETER_DISTINCT_KEY));
        String groupBy = uri.getQueryParameter(QUERY_PARAMETER_GROUP_BY_KEY);
        String limit = uri.getQueryParameter(QUERY_PARAMETER_LIMIT_KEY);

        return dbHelper.query(distinct, tableName, projection, selection, selectionArgs, groupBy, sortOrder, limit);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        String tableName;
        switch (uriMatcher.match(uri)) {
            case ID_WORDBOOK:
                tableName = PATH_WORDBOOK;
                break;
            case ID_VIEW_INFO:
                tableName = PATH_VIEW_INFO;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI:" + uri);
        }

        long rowId = dbHelper.insert(tableName, values);

        // データに変更があったことを通知
        Uri ret = ContentUris.withAppendedId(uri, rowId);
        getContext().getContentResolver().notifyChange(ret, null);

        return ret;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String tableName;
        switch (uriMatcher.match(uri)) {
            case ID_WORDBOOK:
                tableName = PATH_WORDBOOK;
                break;
            case ID_VIEW_INFO:
                tableName = PATH_VIEW_INFO;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI:" + uri);
        }

        int ret = dbHelper.delete(tableName, selection, selectionArgs);

        // データに変更があったことを通知
        getContext().getContentResolver().notifyChange(uri, null);

        return ret;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String tableName;
        switch (uriMatcher.match(uri)) {
            case ID_WORDBOOK:
                tableName = PATH_WORDBOOK;
                break;
            case ID_VIEW_INFO:
                tableName = PATH_VIEW_INFO;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI:" + uri);
        }

        int ret = dbHelper.upDate(tableName, values, selection, selectionArgs);

        // データに変更があったことを通知
        getContext().getContentResolver().notifyChange(uri, null);

        return ret;
    }

    @Override
    public String getType(Uri uri) {
        String ret;
        switch (uriMatcher.match(uri)) {
            case ID_WORDBOOK:
                ret = "vnd.android.cursor.dir/vnd.mkh.wordbook";
                break;
            case ID_VIEW_INFO:
                ret = "vnd.android.cursor.dir/vnd.mkh.view_info";
                break;
            default:
                throw new IllegalArgumentException("Unknown URI:" + uri);
        }
        return ret;
    }

    /**
     * DBヘルパー
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        /**
         * データベースファイル名
         */
        private static final String DB_NAME = "wordBook";

        /**
         * DBバージョン
         */
        private static final int DB_VERSION = 1;

        /**
         * 単語帳テーブルCREATE文
         */
        private static final String CREATE_WORDBOOK_TABLE =
                "CREATE TABLE WORDBOOK (" +
                        "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "WORD," +
                        "KANA," +
                        "CATEGORY, " +
                        "MEANING," +
                        "TRAINING_TARGET INTEGER," +
                        "OTHER," +
                        "INSERTED_DATE," +
                        "UPDATED_DATE" +
                        ")";

        /**
         * 画面表示情報保持テーブルCREATE文
         */
        private static final String CREATE_VIEW_INFO_TABLE =
                "CREATE TABLE VIEW_INFO (" +
                        "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "ORIENTATION INTEGER, " +
                        "SELECTED_ITEM_POSITION INTEGER, " +
                        "TOP_POSITION INTEGER, " +
                        "SHOW_ITEM_NUM INTEGER" +
                        ")";

        /**
         * インデックス
         */
        private static final String CREATE_INDEX_WORD_INDEX_ON_WORDBOOK_WORD =
                "CREATE INDEX wordIndex on WORDBOOK(word)";
        private static final String CREATE_INDEX_MEANING_INDEX_ON_WORDBOOK_MEANING =
                "CREATE INDEX meaningIndex on WORDBOOK(meaning)";
        private static final String CREATE_INDEX_ORIENTATION_INDEX_ON_VIEW_INFO_ORIENTATION =
                "CREATE INDEX orientationIndex on VIEW_INFO(orientation)";

        /**
         * コンストラクタ
         */
        public DatabaseHelper(Context ctx) {
            super(ctx, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_WORDBOOK_TABLE);
            db.execSQL(CREATE_VIEW_INFO_TABLE);
            db.execSQL(CREATE_INDEX_WORD_INDEX_ON_WORDBOOK_WORD);
            db.execSQL(CREATE_INDEX_MEANING_INDEX_ON_WORDBOOK_MEANING);
            db.execSQL(CREATE_INDEX_ORIENTATION_INDEX_ON_VIEW_INFO_ORIENTATION);

            // 新規データの書き込み
            ViewInfoEntity entity = new ViewInfoEntity();
            entity.setOrientation(Configuration.ORIENTATION_LANDSCAPE);
            entity.setSelectedItemPosition(0);
            entity.setTopPosition(0);
            entity.setShowItemNum(0);
            db.insert("VIEW_INFO", null, entity.getContentValues());
            entity.setOrientation(Configuration.ORIENTATION_PORTRAIT);
            db.insert("VIEW_INFO", null, entity.getContentValues());
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            // 既存データの退避
            List<WordBookEntity> wordBookEntityList = new ArrayList<>();
            WordBookEntity wordBookEntity;
            Cursor c = db.query("WORDBOOK", WORD_BOOK_ALL_COLUMNS, null, null, null, null, "_id");
            while(c.moveToNext()) {
                wordBookEntity = new WordBookEntity();
                wordBookEntity.setWord(c.getString(1));
                wordBookEntity.setKana(c.getString(2));
                wordBookEntity.setCategory(c.getString(3));
                wordBookEntity.setMeaning(c.getString(4));
                wordBookEntity.setTrainingTarget(c.getInt(5));
                wordBookEntity.setOther(c.getString(6));
                wordBookEntity.setInsertedDate(c.getString(7));
                wordBookEntity.setUpdatedDate(c.getString(8));
                wordBookEntityList.add(wordBookEntity);
            }
            c.close();
            List<ViewInfoEntity> viewInfoEntityList = new ArrayList<>();
            ViewInfoEntity viewInfoEntity;
            c = db.query("VIEW_INFO", VIEW_INFO_ALL_COLUMNS, null, null, null, null, "_id");
            while(c.moveToNext()) {
                viewInfoEntity = new ViewInfoEntity();
                viewInfoEntity.setOrientation(c.getInt(c.getColumnIndex("ORIENTATION")));
                viewInfoEntity.setShowItemNum(c.getInt(c.getColumnIndex("SHOW_ITEM_NUM")));
                viewInfoEntity.setTopPosition(c.getInt(c.getColumnIndex("TOP_POSITION")));
                viewInfoEntity.setSelectedItemPosition(c.getInt(c.getColumnIndex("SELECTED_ITEM_POSITION")));
            }

            // 削除
            db.execSQL("DROP INDEX wordIndex");
            db.execSQL("DROP INDEX meaningIndex");
            db.execSQL("DROP INDEX orientationIndex");
            db.execSQL("DROP TABLE WORDBOOK");
            db.execSQL("DROP TABLE VIEW_INFO");

            // 新規作成
            onCreate(db);

            // 既存データの戻し
            int size = wordBookEntityList.size();
            for (int i = 0; i < size; i++) {
                db.insert("WORDBOOK", null, wordBookEntityList.get(i).getContentValues());
            }

            size = viewInfoEntityList.size();
            db.execSQL("DELETE FROM VIEW_INFO");
            for (int i = 0; i < size; i++) {
                db.insert("VIEW_INFO", null, viewInfoEntityList.get(i).getContentValues());
            }
        }

        /**
         * 検索する。
         * @param tableName テーブル名
         * @param projection 取得カラム
         * @param selection where句
         * @param selectionOrder where句の引数
         * @param sortOrder order By 句
         * @return カーソル
         */
        public Cursor query(String tableName, String[] projection, String selection, String[] selectionOrder, String sortOrder) {
            SQLiteDatabase db = getWritableDatabase();
            return  query(
                    false,
                    tableName,
                    projection,
                    selection,
                    selectionOrder,
                    sortOrder,
                    null);
        }

        /**
         * 検索する。
         * @param distinct distinctを指定するときtrue、しないときfalse
         * @param tableName テーブル名
         * @param projection 取得カラム
         * @param selection where句
         * @param selectionOrder where句の引数
         * @param sortOrder order By 句
         * @param limit limit文
         * @return カーソル
         */
        public Cursor query(boolean distinct, String tableName, String[] projection, String selection, String[] selectionOrder, String sortOrder, String limit) {
            SQLiteDatabase db = getWritableDatabase();
            return  db.query(
                    distinct,
                    tableName,
                    projection,
                    selection,
                    selectionOrder,
                    null,
                    null,
                    sortOrder,
                    limit);
        }

        /**
         * 検索する。
         * @param distinct distinctを指定するときtrue、しないときfalse
         * @param tableName テーブル名
         * @param projection 取得カラム
         * @param selection where句
         * @param selectionOrder where句の引数
         * @param groupBy group By 句
         * @param sortOrder order By 句
         * @param limit limit文
         * @return カーソル
         */
        public Cursor query(boolean distinct, String tableName, String[] projection, String selection, String[] selectionOrder, String groupBy, String sortOrder, String limit) {
            SQLiteDatabase db = getWritableDatabase();
            return  db.query(
                    distinct,
                    tableName,
                    projection,
                    selection,
                    selectionOrder,
                    groupBy,
                    null,
                    sortOrder,
                    limit);
        }

        /**
         * 単語を登録する。
         * @param tableName テーブル名
         * @param values 登録情報
         * @return 登録行数
         */
        public long insert(String tableName, ContentValues values) {
            SQLiteDatabase db = getWritableDatabase();
            long ret = db.insert(tableName, null, values);
            db.close();
            return ret;
        }

        /**
         * 削除する。
         * @param tableName 削除対象テーブル名
         * @param selection where句
         * @param selectionArgs where句の引数
         * @return 削除件数
         */
        public int delete(String tableName, String selection, String[] selectionArgs) {
            SQLiteDatabase db = getWritableDatabase();
            int ret = db.delete(tableName, selection, selectionArgs);
            db.close();
            return ret;
        }

        /**
         * 更新する。
         * @param tableName 更新対象テーブル名
         * @param values 変更内容
         * @param selection where句
         * @param selectionArgs where句の引数
         * @return 変更レコード数。
         */
        public int upDate(String tableName, ContentValues values, String selection, String[] selectionArgs) {
            SQLiteDatabase db = getWritableDatabase();
            int ret = db.update(tableName, values, selection, selectionArgs);
            db.close();
            return ret;
        }

    }
}
