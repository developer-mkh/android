package jp.gr.java_conf.mkh.wordbook.view;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.app.ListFragment;

import android.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import jp.gr.java_conf.mkh.wordbook.R;
import jp.gr.java_conf.mkh.wordbook.contentprovider.WordBookContentProvider;
import jp.gr.java_conf.mkh.wordbook.util.Util;

/**
 * 詳細表示フラグメント。
 */
public class WordListFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * アイテムがクリックされた際のコールバック。
     */
    private Callbacks mCallbacks = null;

    /**
     * カーソルアダプター
     */
    private SimpleCursorAdapter cursorAdapter;

    /**
     * LoaderManagerのID
     */
    private static final int LOADER_MANAGER_ID = 0;

    /**
     * フラグメントマネージャが生成に使用するコンストラクタ。
     */
    public WordListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLoaderManager().initLoader(LOADER_MANAGER_ID, null, this);

        cursorAdapter = new SimpleCursorAdapter(
                getActivity(),
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

        setListAdapter(cursorAdapter);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // コールバックを実装しているか確認。
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        // コールバックを呼び出す。
        mCallbacks.onItemSelected(id, position);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    @Override
    public void onDestroyView() {
        getLoaderManager().destroyLoader(LOADER_MANAGER_ID);
        super.onDestroyView();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                this.getActivity(),
                WordBookContentProvider.CONTENTS_URI_WORDBOOK,
                new String[] {"_id", "WORD", "MEANING"},
                null,
                null,
                "kana, word");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Cursor old = cursorAdapter.swapCursor(data);
        if (old != null && !old.isClosed()) {
            old.close();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Cursor old = cursorAdapter.swapCursor(null);
        if (old != null && !old.isClosed()) {
            old.close();
        }
    }

    /**
     * アイテムが選択された際に呼ばれるコールバックのインターフェース。
     */
    public interface Callbacks {
        /**
         * アイテムが選択されたときに呼び出されるメソッド。
         *
         * @param id 選択されたアイテムのID
         * @param position 選択されたアイテムの位置
         */
        public void onItemSelected(long id, int position);
    }

}
