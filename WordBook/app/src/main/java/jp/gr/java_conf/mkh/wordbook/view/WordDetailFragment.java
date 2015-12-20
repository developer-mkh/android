package jp.gr.java_conf.mkh.wordbook.view;

import android.database.Cursor;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import java.nio.DoubleBuffer;
import java.text.ParseException;

import jp.gr.java_conf.mkh.wordbook.R;
import jp.gr.java_conf.mkh.wordbook.contentprovider.WordBookContentProvider;
import jp.gr.java_conf.mkh.wordbook.entity.WordBookEntity;
import jp.gr.java_conf.mkh.wordbook.util.Util;

/**
 * 詳細表示のフラグメント。
 */
public class WordDetailFragment extends Fragment {
    /**
     * 選択されたアイテムのIDを保存する際のキー。
     */
    public static final String ARG_ITEM_ID = "wordId";

    /**
     * 表示するデータ。.
     */
    private WordBookEntity showData;

    /**
     * デフォルトコンストラクタ。
     */
    public WordDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().invalidateOptionsMenu();

        if (getArguments().containsKey(ARG_ITEM_ID) && getArguments().getLong(ARG_ITEM_ID) != -1) {
            Cursor c = getActivity().getContentResolver().query(
                    WordBookContentProvider.CONTENTS_URI_WORDBOOK,
                    WordBookContentProvider.WORD_BOOK_ALL_COLUMNS,
                    "_id=?",
                    new String[]{String.valueOf(getArguments().getLong(ARG_ITEM_ID))},
                    null);
            showData = Util.cursorToWordbookEntity(c);
        } else {
            showData = null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_word_detail, container, false);

        // 選択された単語の詳細を表示.
        if (showData != null) {
            ((TextView) rootView.findViewById(R.id.kana_content)).setText(showData.getKana());
            ((TextView) rootView.findViewById(R.id.word_content)).setText(showData.getWord());
            ((TextView) rootView.findViewById(R.id.category_content)).setText(showData.getCategory());
            ((TextView) rootView.findViewById(R.id.meaning_content)).setText(showData.getMeaning());
            String trainingTarget = getResources().getString(R.string.training_target_on);
            if (showData.getTrainingTarget() == WordBookContentProvider.FALSE) {
                trainingTarget = getResources().getString(R.string.training_target_off);
            }
            ((TextView) rootView.findViewById(R.id.training_target_content)).setText(trainingTarget);
            ((TextView) rootView.findViewById(R.id.other_content)).setText(showData.getOther());
            try {
                ((TextView) rootView.findViewById(R.id.inserted_date_content)).setText(Util.formatDate(showData.getInsertedDate(), "yyyy/MM/dd", "yyyyMMdd"));
                ((TextView) rootView.findViewById(R.id.updated_date_content)).setText(Util.formatDate(showData.getUpdatedDate(), "yyyy/MM/dd", "yyyyMMdd"));
            } catch (ParseException e) {
                // DBからとってきた値なので、発生しえない。
            }
        }
        return rootView;
    }
}
