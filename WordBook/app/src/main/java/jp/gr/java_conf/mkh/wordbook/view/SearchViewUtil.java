package jp.gr.java_conf.mkh.wordbook.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import jp.gr.java_conf.mkh.wordbook.R;
import jp.gr.java_conf.mkh.wordbook.contentprovider.WordBookContentProvider;
import jp.gr.java_conf.mkh.wordbook.util.Util;

/**
 * SearchView用のユーティリティ。
 */
public class SearchViewUtil implements LoaderManager.LoaderCallbacks<Cursor> {

    /** コンテキスト */
    private Context ctx;

    /** 検索語 */
    private String query;

    /** カーソルアダプター */
    private SimpleCursorAdapter cursorAdapter;

    /**
     * LoaderManagerのID
     */
    public static final int LOADER_MANAGER_ID = 0;

    /**
     * コンストラクタ
     * @param ctx コンテキスト
     */
    public SearchViewUtil (Context ctx) {
        this.ctx = ctx;
        this.cursorAdapter = null;
        this.query = "";
    }

    /**
     * アクションバーからの検索で、結果を選択した時の番号から、単語のIDを取得する。
     *
     * @param query 検索条件
     * @param which 選択したアイテムの番号
     * @return 対応する単語のID
     */
    long getIdFromListDialog(Activity activity, String query, int which) {
        // whichはDBのIDとは違う値なので、同条件で検索してIDを取得する。
        query = "%" + Util.escapeQueryString(query) + "%";
        Cursor c = activity.getContentResolver().query(WordBookContentProvider.CONTENTS_URI_WORDBOOK,
                new String[]{"_id"},
                "WORD like ? escape '$' ",
                new String[]{query},
                "kana, word, _id");

        c.moveToPosition(which);
        long ret = c.getLong(c.getColumnIndex("_id"));
        c.close();
        return ret;
    }

    /**
     * アクションバーの検索欄から検索する場合の処理。
     * コールバックで使われることを想定。
     *
     * @param query 検索文字列
     * @return 検索結果がセットされたListAdapter。検索結果がない場合はnull、
     */
    ListAdapter processQueryTextSubmit(Activity activity, String query) {
        this.query = "%" + Util.escapeQueryString(query) + "%";

        // 検索結果が存在するかどうか確認
        Cursor count = activity.getContentResolver().query(WordBookContentProvider.CONTENTS_URI_WORDBOOK,
                new String[] {"count(WORD)"},
                "WORD like ? escape '$' ",
                new String[] {this.query},
                null);
        count.moveToNext();
        if (count.getInt(0) == 0) {
            return null;
        }

        // 検索結果があるので、検索を実行する。
        activity.getLoaderManager().restartLoader(LOADER_MANAGER_ID, null, this);
        cursorAdapter = new SimpleCursorAdapter(activity,
                R.layout.list_content,
                null,
                new String[]{"WORD", "MEANING"},
                new int[]{R.id.itemWord, R.id.itemMeaning},
                SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        cursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                int targetColumnIndex = 2;
                if (columnIndex == targetColumnIndex) {
                    String str = Util.getFirstSentence(cursor.getString(targetColumnIndex));
                    ((TextView) view).setText(str);
                    return true;
                }
                return false;
            }
        });

        return cursorAdapter;
    }

    /**
     * アラートダイアログの設定。<br>
     * タイトル、メッセージ、OKボタンの設定。検索結果がなかった場合に使用する。
     * @param builder 設定するアラートダイアログ
     * @return 設定済みのアラートダイアログ
     */
    AlertDialog.Builder setAlertDialogNoResult(AlertDialog.Builder builder) {
        builder.setTitle(R.string.search_result)
                .setMessage(R.string.no_search_result)
                .setPositiveButton(R.string.ok, null);
        return builder;
    }

    /**
     * SearchViewの設定。
     *
     * @param item メニューアイテム(アクションビューにSearchViewを含んでいること)。
     * @return 設定後のSearchView
     */
    SearchView setUpSearchView(Activity activity,  MenuItem item) {
        SearchView ret = (SearchView) item.getActionView();
        ret.setIconifiedByDefault(true);
        ret.setQueryHint(activity.getResources().getString(R.string.search_view_hint));

        return ret;
    }

    /**
     * LoaderManagerを破棄する。
     * @param activity アクティビティ
     */
    void destroyLoaderManager(Activity activity) {
        activity.getLoaderManager().destroyLoader(LOADER_MANAGER_ID);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(ctx,
                WordBookContentProvider.CONTENTS_URI_WORDBOOK,
                new String[]{"_id", "WORD", "MEANING"},
                "WORD like ? escape '$' ",
                new String[]{query},
                "kana, word, _id");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (cursorAdapter != null) {
            cursorAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (cursorAdapter != null) {
            cursorAdapter.swapCursor(null);
        }
    }
}
