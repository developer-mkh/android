package jp.gr.java_conf.mkh.wordbook.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;

import jp.gr.java_conf.mkh.wordbook.R;
import jp.gr.java_conf.mkh.wordbook.contentprovider.WordBookContentProvider;
import jp.gr.java_conf.mkh.wordbook.entity.WordBookEntity;
import jp.gr.java_conf.mkh.wordbook.util.Util;

/**
 * 単語登録アクティビティ。
 */
public class WordAddActivity extends Activity implements
        WordListFragment.Callbacks, View.OnClickListener {

    /**
     * 練習対象の状態
     */
    private boolean isTrainingTarget = false;

    /** 履歴ボタンユーティリティ */
    private HistoryButtonUtil historyButtonUtil = new HistoryButtonUtil(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_add);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        getLoaderManager().initLoader(HistoryButtonUtil.LOADER_MANAGER_ID, null, historyButtonUtil);

        Intent intent = getIntent();
        if (intent != null && intent.getAction() == Intent.ACTION_SEND) {
            processIntent(intent);
        }

        // イベントハンドラの登録
        ((CompoundButton) findViewById(R.id.training_target_toggle_button)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isTrainingTarget = isChecked;
            }
        });
        findViewById(R.id.button_history).setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_add_update, menu);

        MenuItem item = menu.findItem(R.id.menu_ok);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                onOkClicked();
                return true;
            }
        });
        item = menu.findItem(R.id.menu_cancel);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                onCancelClicked();
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            NavUtils.navigateUpTo(this, new Intent(this, WordListActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(long id, int position) {
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        switch (viewId) {
            case R.id.button_history:
                ListAdapter cursorAdapter = historyButtonUtil.getAdapter(this);
                if (cursorAdapter == null) {
                    historyButtonUtil.setAlertDialog(new AlertDialog.Builder(this)).show();
                    return;
                }
                AlertDialog show = historyButtonUtil.setAlertDialog(new AlertDialog.Builder(this),cursorAdapter, this, (EditText)findViewById(R.id.edit_category) ).show();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        historyButtonUtil.destroyLoaderManager(this);
        super.onDestroy();
    }

    /**
     * キャンセルボタンが押されたときの処理。
     */
    private void onCancelClicked() {
        NavUtils.navigateUpTo(this, new Intent(this, WordListActivity.class));
    }

    /**
     * OKボタンが押されたときの処理。
     */
    private void onOkClicked() {
        WordBookEntity entity = new WordBookEntity();
        entity.setKana(((TextView) findViewById(R.id.edit_kana)).getText().toString());
        entity.setWord(((TextView) findViewById(R.id.edit_word)).getText().toString());
        entity.setCategory(((TextView) findViewById(R.id.edit_category)).getText().toString());
        entity.setMeaning(((TextView) findViewById(R.id.edit_meaning)).getText().toString());
        entity.setOther(((TextView) findViewById(R.id.edit_other)).getText().toString());
        entity.setInsertedDate(Util.getDate());
        entity.setUpdatedDate(Util.getDate());
        entity.setTrainingTarget(isTrainingTarget ? WordBookContentProvider.TRUE : WordBookContentProvider.FALSE);

        getContentResolver().insert(WordBookContentProvider.CONTENTS_URI_WORDBOOK, entity.getContentValues());

        NavUtils.navigateUpTo(this, new Intent(this, WordListActivity.class));
    }

    /**
     * 暗黙的インテントを受け取った時の処理。
     * @param intent 受け取ったインテント
     */
    private void processIntent(final Intent intent) {
        // 表示項目と、それに対応する入力欄(のID)の配列
        CharSequence[] items =  {
                getResources().getString(R.string.word),
                getResources().getString(R.string.meaning),
                getResources().getString(R.string.other)
        };
        final View [] widgets = {
                findViewById(R.id.edit_word),
                findViewById(R.id.edit_meaning),
                findViewById(R.id.edit_other)
        };
        final boolean[] checkStates = {false, false, false};
        new AlertDialog.Builder(this)
                .setTitle(R.string.choice_input_element)
                .setMultiChoiceItems(items, checkStates, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        checkStates[which] = isChecked;
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (int i = 0; i < widgets.length; i++) {
                            if (checkStates[i]) {
                                ((EditText) widgets[i]).setText(intent.getStringExtra(Intent.EXTRA_TEXT));
                            }
                        }
                    }
                })
                .show();
    }
}
