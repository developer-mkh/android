package jp.gr.java_conf.mkh.wordbook.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.SearchView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import jp.gr.java_conf.mkh.wordbook.R;
import jp.gr.java_conf.mkh.wordbook.component.UpdateComponent;
import jp.gr.java_conf.mkh.wordbook.contentprovider.WordBookContentProvider;
import jp.gr.java_conf.mkh.wordbook.entity.WordBookEntity;
import jp.gr.java_conf.mkh.wordbook.util.Util;


/**
 * 単語帳アクティビティ。
 * 単語帳フラグメントのコールバックメソッドも実装する。
 */
public class WordListActivity extends Activity
        implements WordListFragment.Callbacks {

    /**
     * 2ペインかどうかのフラグ。
     */
    private boolean mTwoPane;

    /** 検索窓用ユーティリティ */
    private SearchViewUtil searchViewUtil = new SearchViewUtil(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_word_list);
        if (findViewById(R.id.word_detail_container) != null) {
            // 詳細表示コンテナ領域があれば、2ペインとする。
            mTwoPane = true;

            ((WordListFragment) getFragmentManager()
                    .findFragmentById(R.id.word_list))
                    .setActivateOnItemClick(true);
        }

        long id = getIntent().getLongExtra(WordDetailFragment.ARG_ITEM_ID, -1);

        getLoaderManager().initLoader(SearchViewUtil.LOADER_MANAGER_ID, null, searchViewUtil);

        // 更新作業中であれば、更新画面を表示。
        boolean updateMode = getIntent().getBooleanExtra(WordUpdateFragment.ARG_UPDATE_MODE, false);
        if (mTwoPane & updateMode) {
            Bundle argument = new Bundle();
            argument.putLong(WordUpdateFragment.ARG_ITEM_ID, id);
            WordBookEntity inputData = (WordBookEntity) getIntent().getSerializableExtra(WordUpdateFragment.ARG_INPUT_DATA);
            if (inputData != null) {
                argument.putSerializable(WordUpdateFragment.ARG_INPUT_DATA, inputData);
            }
            WordUpdateFragment fragment = new WordUpdateFragment();
            fragment.setArguments(argument);
            getFragmentManager().beginTransaction()
                    .replace(R.id.word_detail_container, fragment)
                    .commit();
            return;
        } else if (updateMode) {
            Intent updateIntent = new Intent(this, WordUpdateActivity.class);
            updateIntent.putExtra(WordUpdateFragment.ARG_ITEM_ID, id);
            WordBookEntity inputData = (WordBookEntity) getIntent().getSerializableExtra(WordUpdateFragment.ARG_INPUT_DATA);
            updateIntent.putExtra(WordUpdateFragment.ARG_INPUT_DATA, inputData);
            startActivity(updateIntent);
            return;
        }

        // 画面表示状態の復元
        Util.loadViewInfo(getResources().getConfiguration().orientation,
                ((WordListFragment) getFragmentManager().findFragmentById(R.id.word_list)).getListView(),
                getContentResolver());

        // 事前に選択されていたアイテムがあれば、それを表示。
        if (id != -1) {
            transitionsWordDetail(id);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 事前に選択されていた項目があるか
        final long id = getIntent().getLongExtra(WordDetailFragment.ARG_ITEM_ID, -1);

        // 更新状態か
        boolean isUpdate = getIntent().getBooleanExtra(WordUpdateFragment.ARG_UPDATE_MODE, false);

        MenuInflater inflater = getMenuInflater();
        MenuItem item;

        if (mTwoPane && id != -1 && !isUpdate) {
            // 2画面構成で、何か単語が選択されていて、更新状態でないとき
            inflater.inflate(R.menu.menu_word_list_and_word_detail, menu);

            item = menu.findItem(R.id.menu_add);
            item.setOnMenuItemClickListener(
                    new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            Intent addIntent = new Intent(WordListActivity.this, WordAddActivity.class);
                            startActivity(addIntent);
                            return true;
                        }
                    }
            );
            item = menu.findItem(R.id.menu_delete);
            item.setOnMenuItemClickListener(
                    new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            getContentResolver().delete(WordBookContentProvider.CONTENTS_URI_WORDBOOK, "_id=?", new String[]{String.valueOf(id)});
                            Intent listIntent = new Intent(WordListActivity.this, WordListActivity.class);
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
                            getIntent().putExtra(WordUpdateFragment.ARG_UPDATE_MODE, true);
                            Bundle argument = new Bundle();
                            argument.putLong(WordUpdateFragment.ARG_ITEM_ID, id);
                            WordUpdateFragment fragment = new WordUpdateFragment();
                            fragment.setArguments(argument);
                            getFragmentManager().beginTransaction()
                                    .replace(R.id.word_detail_container, fragment)
                                    .commit();
                            return true;
                        }
                    }
            );
            item = menu.findItem(R.id.menu_training);
            item.setOnMenuItemClickListener(
                    new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            Intent trainingIntent = new Intent(WordListActivity.this, WordCardActivity.class);
                            startActivity(trainingIntent);
                            return true;
                        }
                    }
            );
            item = menu.findItem(R.id.menu_settings);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    Intent settingIntent = new Intent(WordListActivity.this, WordSettingsActivity.class);
                    settingIntent.putExtra(WordDetailFragment.ARG_ITEM_ID, getIntent().getLongExtra(WordDetailFragment.ARG_ITEM_ID, -1));
                    startActivity(settingIntent);
                    return true;
                }
            });
            item = menu.findItem(R.id.menu_help);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    showHelp();
                    return true;
                }
            });
            // SearchViewの設定
            item = menu.findItem(R.id.menu_search_on_action_bar);
            SearchView searchView = searchViewUtil.setUpSearchView(this, item);
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    final String fQuery = query;
                    ListAdapter adapter = searchViewUtil.processQueryTextSubmit(WordListActivity.this, fQuery);
                    if (adapter == null) {
                        AlertDialog.Builder adb = new AlertDialog.Builder(WordListActivity.this);
                        searchViewUtil.setAlertDialogNoResult(adb).show();
                        return true;
                    }
                    new AlertDialog.Builder(WordListActivity.this)
                            .setTitle(R.string.search_result)
                            .setNegativeButton(R.string.cancel, null)
                            .setAdapter(adapter, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    transitionsWordDetail(searchViewUtil.getIdFromListDialog(WordListActivity.this, fQuery, which));
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
        } else if (!isUpdate) {
            // 更新状態でないとき
            // (1画面構成か、2画面で単語が選択されていないとき)
            inflater.inflate(R.menu.menu, menu);

            // SearchViewの設定
            item = menu.findItem(R.id.menu_search_on_action_bar);
            SearchView searchView = searchViewUtil.setUpSearchView(this, item);
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    final String fQuery = query;
                    ListAdapter adapter = searchViewUtil.processQueryTextSubmit(WordListActivity.this, fQuery);
                    if (adapter == null) {
                        AlertDialog.Builder adb = new AlertDialog.Builder(WordListActivity.this);
                        searchViewUtil.setAlertDialogNoResult(adb).show();
                        return true;
                    }
                    new AlertDialog.Builder(WordListActivity.this)
                            .setTitle(R.string.search_result)
                            .setNegativeButton(R.string.cancel, null)
                            .setAdapter(adapter, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    transitionsWordDetail(searchViewUtil.getIdFromListDialog(WordListActivity.this, fQuery, which));
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

            item = menu.findItem(R.id.menu_add);
            item.setOnMenuItemClickListener(
                    new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            Intent addIntent = new Intent(WordListActivity.this, WordAddActivity.class);
                            startActivity(addIntent);
                            return true;
                        }
                    }
            );
            item = menu.findItem(R.id.menu_training);
            item.setOnMenuItemClickListener(
                    new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            Intent trainingIntent = new Intent(WordListActivity.this, WordCardActivity.class);
                            startActivity(trainingIntent);
                            return true;
                        }
                    }
            );
            item = menu.findItem(R.id.menu_settings);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    Intent settingIntent = new Intent(WordListActivity.this, WordSettingsActivity.class);
                    settingIntent.putExtra(WordDetailFragment.ARG_ITEM_ID, getIntent().getLongExtra(WordDetailFragment.ARG_ITEM_ID, -1));

                    startActivity(settingIntent);
                    return true;
                }
            });
            item = menu.findItem(R.id.menu_help);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    showHelp();
                    return true;
                }
            });
        } else if (mTwoPane) {
            // 2画面で更新状態のとき(if文の組み方から、ここでは必ず更新状態になっている)
            final long updateId = getIntent().getLongExtra(WordDetailFragment.ARG_ITEM_ID, -1);
            inflater.inflate(R.menu.menu_add_update, menu);
            Cursor c = getContentResolver().query(WordBookContentProvider.CONTENTS_URI_WORDBOOK,
                    WordBookContentProvider.WORD_BOOK_ALL_COLUMNS,
                    "_id = ?",
                    new String[]{String.valueOf(updateId)},
                    null);
            final WordBookEntity updateData = Util.cursorToWordbookEntity(c);
            c.close();

            item = menu.findItem(R.id.menu_ok);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    UpdateComponent comp = new UpdateComponent();
                    comp.onButtonClicked(findViewById(R.id.menu_ok), updateId, updateData, WordListActivity.this, WordListActivity.this);
                    Bundle arguments = new Bundle();
                    arguments.putLong(WordDetailFragment.ARG_ITEM_ID, updateId);
                    WordDetailFragment fragment = new WordDetailFragment();
                    fragment.setArguments(arguments);
                    getFragmentManager().beginTransaction()
                            .replace(R.id.word_detail_container, fragment)
                            .commit();
                    return true;
                }
            });
            item = menu.findItem(R.id.menu_cancel);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    UpdateComponent comp = new UpdateComponent();
                    comp.onButtonClicked(findViewById(R.id.menu_cancel), updateId, updateData, WordListActivity.this, WordListActivity.this);
                    Bundle arguments = new Bundle();
                    arguments.putLong(WordDetailFragment.ARG_ITEM_ID, updateId);
                    WordDetailFragment fragment = new WordDetailFragment();
                    fragment.setArguments(arguments);
                    getFragmentManager().beginTransaction()
                            .replace(R.id.word_detail_container, fragment)
                            .commit();
                    return true;
                }
            });
        }
        return true;
    }

    @Override
    public void onItemSelected(long id, int position) {

        // 画面表示状態を保存
        Util.saveViewInfo(getResources().getConfiguration().orientation,
                ((WordListFragment) getFragmentManager().findFragmentById(R.id.word_list)).getListView(),
                getContentResolver());

        transitionsWordDetail(id);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        UpdateComponent comp = new UpdateComponent();
        comp.saveInputDataToIntent(this);
    }

    @Override
    protected void onDestroy() {
        searchViewUtil.destroyLoaderManager(this);
        super.onDestroy();
    }

    /**
     * 単語詳細画面へ遷移する。画面表示状態の保存は行わない。
     *
     * @param id 表示対象単語のID
     */
    private void transitionsWordDetail(long id) {
        // 選択したアイテムのIDを再描画に備えてインテントに保存。
        getIntent().putExtra(WordDetailFragment.ARG_ITEM_ID, id);

        // updateフラグをリセット
        getIntent().putExtra(WordUpdateFragment.ARG_UPDATE_MODE, false);

        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putLong(WordDetailFragment.ARG_ITEM_ID, id);
            WordDetailFragment detailFragment = new WordDetailFragment();
            detailFragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .replace(R.id.word_detail_container, detailFragment)
                    .commit();
        } else {
            Intent detailIntent = new Intent(this, WordDetailActivity.class);
            detailIntent.putExtra(WordDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }

    /**
     * ヘルプを表示する。
     */
    private void showHelp() {
        // ヘルプ文章の読み込み
        BufferedReader br = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.help)));
        StringBuffer sb = new StringBuffer();
        String text;
        try {
            while ((text = br.readLine()) != null) {
                sb.append(text);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String help = sb.toString();

        // バージョン取得
        String version;
        try {
            version = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            version = "0";
        }

        help = help.replaceAll("%VERSION%", version);

        new AlertDialog.Builder(this)
                .setMessage(Html.fromHtml(help))
                .setTitle(getResources().getString(R.string.help))
                .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }
}
