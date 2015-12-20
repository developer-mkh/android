package jp.gr.java_conf.mkh.wordbook.component;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.ToggleButton;

import jp.gr.java_conf.mkh.wordbook.R;
import jp.gr.java_conf.mkh.wordbook.contentprovider.WordBookContentProvider;
import jp.gr.java_conf.mkh.wordbook.entity.WordBookEntity;
import jp.gr.java_conf.mkh.wordbook.util.Util;
import jp.gr.java_conf.mkh.wordbook.view.WordDetailActivity;
import jp.gr.java_conf.mkh.wordbook.view.WordDetailFragment;
import jp.gr.java_conf.mkh.wordbook.view.WordUpdateFragment;

/**
 * 更新処理で使用する処理。
 */
public class UpdateComponent {

    /**
     * ボタンが押されたときのコールバック。
     * @param v 呼び出し元のView
     * @param updateData 更新内容
     */
    public void onButtonClicked(View v, Long itemId, WordBookEntity updateData, Activity activity, Context ctx) {
        int rId = v.getId();
        switch (rId) {
            case R.id.menu_ok:
                onOkClicked(itemId, updateData, activity, ctx);
                break;
            case R.id.menu_cancel:
                onCancelClicked(itemId, activity, ctx);
                break;
        }
    }

    /**
     * 表示しているアイテムのIDがセットされた詳細画面のインテントを返す。
     * @param id 表示しているアイテムのID
     * @param ctx コンテキスト
     * @return IDがセットされた詳細画面のインテント
     */
    public Intent setItemId(Long id, Context ctx) {
        Intent ret = new Intent(ctx, WordDetailActivity.class);
        ret.putExtra(WordDetailFragment.ARG_ITEM_ID, id);
        return ret;
    }

    /**
     * 入力された値を引数のActivityのIntentに保存する。
     * アップデートフラグが立っていて、更新画面のかな入力欄が取得できる場合に保存する。
     * WordBookEntity型で、WordUpdateFragment.ARG_INPUT_DATAをキーとする。
     *
     * @param activity 保存先のアクティビティ
     */
    public void saveInputDataToIntent(Activity activity) {
        WordBookEntity entity;
        boolean updateMode = activity.getIntent().getBooleanExtra(WordUpdateFragment.ARG_UPDATE_MODE, false);
        if (updateMode && (activity.findViewById(R.id.edit_kana_update) != null)) {
            entity = new WordBookEntity();
            entity.setKana(((EditText) activity.findViewById(R.id.edit_kana_update)).getText().toString());
            entity.setWord(((EditText) activity.findViewById(R.id.edit_word_update)).getText().toString());
            entity.setCategory(((EditText) activity.findViewById(R.id.edit_category_update)).getText().toString());
            entity.setMeaning(((EditText) activity.findViewById(R.id.edit_meaning_update)).getText().toString());
            boolean isChecked = ((ToggleButton) activity.findViewById(R.id.training_target_toggle_button_update)).isChecked();
            entity.setTrainingTarget(isChecked ? WordBookContentProvider.TRUE : WordBookContentProvider.FALSE);
            entity.setOther(((EditText) activity.findViewById(R.id.edit_other_update)).getText().toString());
            activity.getIntent().putExtra(WordUpdateFragment.ARG_INPUT_DATA, entity);
        }
    }

    /**
     * OKボタンが押下された場合の処理。
     *
     * @param id 対象アイテムのID
     * @param updateData 更新情報
     * @param activity アクティビティ
     * @param ctx コンテキスト
     * @return 更新件数
     *
     */
    private int onOkClicked(Long id, WordBookEntity updateData, Activity activity, Context ctx) {
        updateData.setKana(((EditText) activity.findViewById(R.id.edit_kana_update)).getText().toString());
        updateData.setWord(((EditText) activity.findViewById(R.id.edit_word_update)).getText().toString());
        updateData.setCategory(((EditText) activity.findViewById(R.id.edit_category_update)).getText().toString());
        updateData.setMeaning(((EditText) activity.findViewById(R.id.edit_meaning_update)).getText().toString());
        updateData.setOther(((EditText) activity.findViewById(R.id.edit_other_update)).getText().toString());
        updateData.setUpdatedDate(Util.getDate());
        Boolean isTrainingTargetChecked = ((ToggleButton) (activity.findViewById(R.id.training_target_toggle_button_update))).isChecked();
        updateData.setTrainingTarget(isTrainingTargetChecked ? WordBookContentProvider.TRUE : WordBookContentProvider.FALSE);

        // アップデートフラグをリセット
        activity.getIntent().putExtra(WordUpdateFragment.ARG_UPDATE_MODE, false);

        return ctx.getContentResolver().update(WordBookContentProvider.CONTENTS_URI_WORDBOOK,
                updateData.getContentValues(),
                "_id=?",
                new String[]{id.toString()});
    }

    /**
     * キャンセルボタンが押されたときの処理。
     * @param id 対象アイテムのID
     * @param activity アクティビティ
     * @param ctx コンテキスト
     */
    private void onCancelClicked(Long id, Activity activity, Context ctx){
        activity.getIntent().putExtra(WordUpdateFragment.ARG_UPDATE_MODE, false);
    }
}
