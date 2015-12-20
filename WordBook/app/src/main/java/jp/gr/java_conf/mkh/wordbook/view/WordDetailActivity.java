package jp.gr.java_conf.mkh.wordbook.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.SearchView;

import jp.gr.java_conf.mkh.wordbook.R;
import jp.gr.java_conf.mkh.wordbook.contentprovider.WordBookContentProvider;


/**
 * 単語詳細表示アクティビティ。
 */
public class WordDetailActivity extends Activity {

    /**
     * 選択されたアイテムのID
     */
    private Long id;

    /** 検索窓用ユーティリティ */
    private SearchViewUtil searchViewUtil = new SearchViewUtil(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        id = getIntent().getLongExtra(WordDetailFragment.ARG_ITEM_ID, -1);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // 横表示になった場合、今表示しているアイテムを再表示したいので、
            // 表示していたアイテムのIDを保存して、WordListアクティビティをスタート。
            Intent intent = new Intent(this, WordListActivity.class);
            intent.putExtra(WordDetailFragment.ARG_ITEM_ID, id);
            startActivity(intent);
        } else {
            setContentView(R.layout.activity_word_detail);
        }
        getLoaderManager().initLoader(SearchViewUtil.LOADER_MANAGER_ID, null, searchViewUtil);

        // アクションバーに「戻る」を表示。
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putLong(WordDetailFragment.ARG_ITEM_ID, id);
            WordDetailFragment fragment = new WordDetailFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .add(R.id.word_detail_container, fragment)
                    .commit();
        }
    }

    /**
     * メニューの作成
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_word_detail, menu);

        MenuItem item = menu.findItem(R.id.menu_delete);
        item.setOnMenuItemClickListener(
                new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        getContentResolver().delete(WordBookContentProvider.CONTENTS_URI_WORDBOOK, "_ID=?", new String[]{id.toString()});
                        Intent listIntent = new Intent(WordDetailActivity.this, WordListActivity.class);
                        startActivity(listIntent);
                        return true;
                    }
                }
        );
        item = menu.findItem(R.id.menu_update);
        item.setOnMenuItemClickListener(
                new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Intent intent = new Intent(WordDetailActivity.this, WordUpdateActivity.class);
                        intent.putExtra(WordDetailFragment.ARG_ITEM_ID, id);
                        intent.putExtra(WordUpdateFragment.ARG_UPDATE_MODE, true);
                        startActivity(intent);
                        return true;
                    }
                }
        );

        // SearchViewの設定
        item = menu.findItem(R.id.menu_search_on_action_bar);
        final SearchView searchView = searchViewUtil.setUpSearchView(this, item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                final String fQuery = query;
                ListAdapter adapter = searchViewUtil.processQueryTextSubmit(WordDetailActivity.this, fQuery);
                if (adapter == null) {
                    AlertDialog.Builder adb = new AlertDialog.Builder(WordDetailActivity.this);
                    searchViewUtil.setAlertDialogNoResult(adb).show();
                    return true;
                }
                new AlertDialog.Builder(WordDetailActivity.this)
                        .setTitle(R.string.search_result)
                        .setNegativeButton(R.string.cancel, null)
                        .setAdapter(adapter, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent detailIntent = new Intent(WordDetailActivity.this, WordDetailActivity.class);
                                detailIntent.putExtra(WordDetailFragment.ARG_ITEM_ID, searchViewUtil.getIdFromListDialog(WordDetailActivity.this, fQuery, which));
                                startActivity(detailIntent);
                            }
                        })
                        .show();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent listIntent = new Intent(this, WordListActivity.class);
            NavUtils.navigateUpTo(this, listIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onDestroy() {
        searchViewUtil.destroyLoaderManager(this);
        super.onDestroy();
    }

}
