package jp.gr.java_conf.mkh.wordbook.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;

import jp.gr.java_conf.mkh.wordbook.R;
import jp.gr.java_conf.mkh.wordbook.contentprovider.WordBookContentProvider;

/**
 * 履歴ボタン用のユーティリティ。
 */
public class HistoryButtonUtil implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * コンテキスト
     */
    private Context ctx;

    /**
     * カーソルアダプター
     */
    private SimpleCursorAdapter cursorAdapter;

    /**
     * LoaderManagerのID
     */
    public static final int LOADER_MANAGER_ID = 0;

    public HistoryButtonUtil(Context ctx) {
        this.ctx = ctx;
        this.cursorAdapter = null;
    }

    /**
     * リストダイアログの番号から、対応するカテゴリーを取得する。。
     *
     * @param activity アクティビティ
     * @param which    リストダイアログの何番目を表す番号
     * @return 対応するレコードのカテゴリー
     */
    String getIdFromListDialog(Activity activity, int which) {
        Cursor idCursor = activity.getContentResolver().query(
                WordBookContentProvider.CONTENTS_URI_WORDBOOK.
                        buildUpon()
                        .appendQueryParameter(WordBookContentProvider.QUERY_PARAMETER_DISTINCT_KEY, "true")
                        .appendQueryParameter(WordBookContentProvider.QUERY_PARAMETER_GROUP_BY_KEY, "CATEGORY")
                        .build(),
                new String[]{"_id", "CATEGORY"},
                null,
                null,
                "kana, word, _id");
        idCursor.moveToPosition(which);
        return idCursor.getString(idCursor.getColumnIndex("CATEGORY"));

    }

    /**
     * アダプターを取得する。
     *
     * @param activity アクティビティ
     * @return 検索結果がセットされたListAdapter。検索結果がない場合はnull、
     */
    ListAdapter getAdapter(Activity activity) {

        // 検索結果が存在するかどうか確認
        Cursor count = activity.getContentResolver().query(
                WordBookContentProvider.CONTENTS_URI_WORDBOOK
                        .buildUpon()
                        .appendQueryParameter(WordBookContentProvider.QUERY_PARAMETER_DISTINCT_KEY, "true")
                        .appendQueryParameter(WordBookContentProvider.QUERY_PARAMETER_GROUP_BY_KEY, "CATEGORY")
                        .build(),
                new String[]{"count(CATEGORY)"},
                null,
                null,
                "kana, word, _id");
        count.moveToNext();
        if (count.getInt(0) == 0) {
            return null;
        }

        // 検索結果があるので、検索を実行する。
        activity.getLoaderManager().restartLoader(LOADER_MANAGER_ID, null, this);
        cursorAdapter = new SimpleCursorAdapter(activity,
                android.R.layout.simple_list_item_1,
                null,
                new String[]{"CATEGORY"},
                new int[]{android.R.id.text1},
                SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        return cursorAdapter;
    }

    /**
     * アラートダイアログの設定。<br>
     * タイトル、メッセージ、OKボタンの設定。検索結果がなかった場合に使用する。
     *
     * @param builder 設定するアラートダイアログ
     * @return 設定済みのアラートダイアログ
     */
    AlertDialog.Builder setAlertDialog(AlertDialog.Builder builder) {
        builder.setTitle(R.string.history)
                .setMessage(R.string.no_history)
                .setPositiveButton(R.string.ok, null);
        return builder;
    }

    /**
     * アラートダイアログの設定。<br>
     * タイトル、メッセージ、キャンセルボタン、アイテム選択した時のコールバックの設定。検索結果があった場合に使用する。
     *
     * @param builder アラートダイアログのBuilder
     * @param adapter リスト表示に必要なアダプター
     * @param activity アクティビティ
     * @param editText 選択したアイテムを入力値として反映する先
     * @return 設定されたアラートダイアログのBuilder
     */
    AlertDialog.Builder setAlertDialog(AlertDialog.Builder builder, ListAdapter adapter, final Activity activity, final EditText editText) {
        return builder.setTitle(R.string.history).
                setNegativeButton(R.string.cancel, null).
                setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editText.setText(getIdFromListDialog(activity, which));
                    }
                });
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
                WordBookContentProvider.CONTENTS_URI_WORDBOOK
                        .buildUpon()
                        .appendQueryParameter(WordBookContentProvider.QUERY_PARAMETER_DISTINCT_KEY, "true")
                        .appendQueryParameter(WordBookContentProvider.QUERY_PARAMETER_GROUP_BY_KEY, "CATEGORY")
                        .build(),
                new String[]{"_id", "CATEGORY"},
                null,
                null,
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
