package jp.gr.java_conf.mkh.wordbook.preference;

import android.content.Context;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;

import jp.gr.java_conf.mkh.wordbook.R;
import jp.gr.java_conf.mkh.wordbook.view.WordSettingsActivity;

/**
 * 設定画面用NumberPicker。
 */
public class NumberPickerPreference extends DialogPreference{

    /** View */
    private View view;

    /**
     * コンストラクタ
     * @param context コンテキスト
     * @param attributeSet スタイル属性
     */
    public NumberPickerPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setDialogLayoutResource(R.layout.number_picker_preference);
        setPersistent(true);
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);
        this.view = view;

        NumberPicker picker = (NumberPicker) view.findViewById(R.id.pref_numberPicker);
        picker.setMaxValue(60);
        picker.setMinValue(1);
        picker.setValue(getPersistedInt(WordSettingsActivity.DEFAULT_INTERVAL));
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            NumberPicker picker = (NumberPicker) view.findViewById(R.id.pref_numberPicker);
            persistInt(picker.getValue());
        }
    }


}
