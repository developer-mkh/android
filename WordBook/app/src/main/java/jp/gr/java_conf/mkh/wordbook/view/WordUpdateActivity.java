package jp.gr.java_conf.mkh.wordbook.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;

import jp.gr.java_conf.mkh.wordbook.R;
import jp.gr.java_conf.mkh.wordbook.component.UpdateComponent;
import jp.gr.java_conf.mkh.wordbook.contentprovider.WordBookContentProvider;
import jp.gr.java_conf.mkh.wordbook.entity.WordBookEntity;
import jp.gr.java_conf.mkh.wordbook.util.Util;


public class WordUpdateActivity extends Activity {

    /**
     * 選択されたアイテムのID
     */
    private Long id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        id = getIntent().getLongExtra(WordUpdateFragment.ARG_ITEM_ID, -1);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // 横表示になった場合、今表示しているアイテムを再表示したいので、
            // 表示していたアイテムのIDを保存して、WordListアクティビティをスタート。
            Intent intent = new Intent(this, WordListActivity.class);
            intent.putExtra(WordUpdateFragment.ARG_ITEM_ID, id);
            intent.putExtra(WordUpdateFragment.ARG_UPDATE_MODE, true);
            WordBookEntity entity = (WordBookEntity) getIntent().getSerializableExtra(WordUpdateFragment.ARG_INPUT_DATA);
            if (entity != null) {
                intent.putExtra(WordUpdateFragment.ARG_INPUT_DATA, entity);
            }
            startActivity(intent);
        } else {
            setContentView(R.layout.activity_word_update);
        }

        // アクションバーに「戻る」を表示。
        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            Bundle argument = new Bundle();
            WordBookEntity inputData = (WordBookEntity) getIntent().getSerializableExtra(WordUpdateFragment.ARG_INPUT_DATA);
            if (inputData != null) {
                argument.putSerializable(WordUpdateFragment.ARG_INPUT_DATA, inputData);
            }
            argument.putLong(WordUpdateFragment.ARG_ITEM_ID, id);
            // 念のためリセット
            getIntent().putExtra(WordUpdateFragment.ARG_UPDATE_MODE, false);

            WordUpdateFragment fragment = new WordUpdateFragment();
            fragment.setArguments(argument);
            getFragmentManager().beginTransaction()
                    .replace(R.id.word_update_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_add_update, menu);

        Cursor c = getContentResolver().query(WordBookContentProvider.CONTENTS_URI_WORDBOOK,
                WordBookContentProvider.WORD_BOOK_ALL_COLUMNS,
                "_id = ?",
                new String[]{String.valueOf(id)},
                null);
        final WordBookEntity updateData = Util.cursorToWordbookEntity(c);
        c.close();
        MenuItem item = menu.findItem(R.id.menu_ok);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                UpdateComponent comp = new UpdateComponent();
                comp.onButtonClicked(findViewById(R.id.menu_ok), id, updateData, WordUpdateActivity.this, WordUpdateActivity.this);
                NavUtils.navigateUpTo(WordUpdateActivity.this, comp.setItemId(id, WordUpdateActivity.this));
                return true;
            }
        });
        item = menu.findItem(R.id.menu_cancel);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                UpdateComponent comp = new UpdateComponent();
                comp.onButtonClicked(findViewById(R.id.menu_cancel), id, updateData, WordUpdateActivity.this, WordUpdateActivity.this);
                NavUtils.navigateUpTo(WordUpdateActivity.this, comp.setItemId(id, WordUpdateActivity.this));
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            UpdateComponent comp = new UpdateComponent();
            NavUtils.navigateUpTo(this, comp.setItemId(this.id, this));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        UpdateComponent comp = new UpdateComponent();
        // リセットしたので再度trueにセット
        getIntent().putExtra(WordUpdateFragment.ARG_UPDATE_MODE, true);
        comp.saveInputDataToIntent(this);
    }
}
