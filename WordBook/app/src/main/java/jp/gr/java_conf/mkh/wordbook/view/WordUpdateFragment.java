package jp.gr.java_conf.mkh.wordbook.view;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ToggleButton;

import jp.gr.java_conf.mkh.wordbook.R;
import jp.gr.java_conf.mkh.wordbook.contentprovider.WordBookContentProvider;
import jp.gr.java_conf.mkh.wordbook.entity.WordBookEntity;
import jp.gr.java_conf.mkh.wordbook.util.Util;


/**
 * 登録内容更新フラグメント。
 */
public class WordUpdateFragment extends Fragment implements View.OnClickListener {

    /**
     * 選択されたアイテムのIDを保存する際のキー。
     */
    public static final String ARG_ITEM_ID = "wordId";

    /**
     * 更新作業中を示すフラグを保存する際のキー。
     */
    public static final String ARG_UPDATE_MODE = "updateMode";

    /**
     * 入力データを保存する際のキー。
     */
    public static final String ARG_INPUT_DATA = "inputData";

    /**
     * 表示する単語情報。
     */
    private WordBookEntity showData;

    /** 履歴ボタンユーティリティ */
    private HistoryButtonUtil historyButtonUtil = null;

    /**
     * デフォルトコンストラクタ。
     */
    public WordUpdateFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().invalidateOptionsMenu();
        historyButtonUtil = new HistoryButtonUtil(getActivity());
        getLoaderManager().initLoader(HistoryButtonUtil.LOADER_MANAGER_ID, null, historyButtonUtil);

        Long id = -1L;
        if (getArguments().containsKey(ARG_ITEM_ID)) {
            id = getArguments().getLong(ARG_ITEM_ID);
        }

        boolean isUpdateMode = getActivity().getIntent().getBooleanExtra(WordUpdateFragment.ARG_UPDATE_MODE, false);
        if ((getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) && isUpdateMode) {
            // 縦表示でアップデートフラグがtrueの場合は、縦方向への画面回転が発生した時。処理はActivityに任せる。
            // i.e.画面回転して縦表示のActivityはWordListActivity。この場合は、アップデートフラグが立っている。
            // 元々縦表示の場合は、メニューのコールバックでWordUpdateActivityを直接呼び出しているので、フラグが立っていない。
            getActivity().getIntent().putExtra(WordUpdateFragment.ARG_ITEM_ID, id);
        } else if (id != -1) {
            Cursor c = getActivity().getContentResolver().query(
                    WordBookContentProvider.CONTENTS_URI_WORDBOOK,
                    WordBookContentProvider.WORD_BOOK_ALL_COLUMNS,
                    "_id=?",
                    new String[]{id.toString()},
                    null);
            showData = Util.cursorToWordbookEntity(c);
        } else {
            showData = null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_word_update, container, false);

        // コールバックの設定
        rootView.findViewById(R.id.button_history_update).setOnClickListener(this);

        // 選択された単語の詳細を表示.
        WordBookEntity inputData = (WordBookEntity) getArguments().getSerializable(ARG_INPUT_DATA);
        if (inputData == null) {
            inputData = showData;
        }

        if (inputData != null) {
            ((EditText) rootView.findViewById(R.id.edit_kana_update)).setText(inputData.getKana());
            ((EditText) rootView.findViewById(R.id.edit_word_update)).setText(inputData.getWord());
            ((EditText) rootView.findViewById(R.id.edit_category_update)).setText(inputData.getCategory());
            ((EditText) rootView.findViewById(R.id.edit_meaning_update)).setText(inputData.getMeaning());
            ((ToggleButton) rootView.findViewById(R.id.training_target_toggle_button_update)).setChecked(true);
            if (inputData.getTrainingTarget() == WordBookContentProvider.FALSE) {
                ((ToggleButton) rootView.findViewById(R.id.training_target_toggle_button_update)).setChecked(false);
            }
            ((EditText) rootView.findViewById(R.id.edit_other_update)).setText(inputData.getOther());
        }
        return rootView;
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        switch (viewId) {
            case R.id.button_history_update:
                ListAdapter cursorAdapter = historyButtonUtil.getAdapter(getActivity());
                if (cursorAdapter == null) {
                    historyButtonUtil.setAlertDialog(new AlertDialog.Builder(getActivity())).show();
                    return;
                }
                AlertDialog show = historyButtonUtil.setAlertDialog(new AlertDialog.Builder(getActivity()),cursorAdapter, getActivity(), (EditText)(getActivity().findViewById(R.id.edit_category_update))).show();
                break;
        }
    }

    @Override
    public void onDestroy() {
        historyButtonUtil.destroyLoaderManager(getActivity());
        super.onDestroy();
    }
}
