package jp.gr.java_conf.mkh.alarm.widget;

import jp.gr.java_conf.mkh.alarm.R;
import jp.gr.java_conf.mkh.alarm.model.ListItem;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class SettingViewAdapter extends ArrayAdapter<ListItem> {

    private LayoutInflater inflater;

    /**
     * コンストラクタ。
     *
     * @param context
     *            コンテキスト
     * @param listItem
     *            表示に使うListItemの配列
     */
    public SettingViewAdapter(Context context, ListItem[] listItem) {
        super(context, R.layout.alarm_row, R.id.textView1, listItem);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * @see android.widget.ArrayAdapter#getView(int, android.view.View,
     *      android.view.ViewGroup)
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.alarm_row, parent, false);
        }

        CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkBox8);
        TextView text = (TextView) convertView.findViewById(R.id.textView1);
        ListItem item = getItem(position);

        checkBox.setOnCheckedChangeListener(null);

        if (item.isEnableCheckBox()) {
            checkBox.setChecked(item.isChecked());
            checkBox.setEnabled(true);
            checkBox.setFocusable(false);
            checkBox.setFocusableInTouchMode(false);
        } else {
            checkBox.setChecked(false);
            checkBox.setEnabled(false);
            checkBox.setFocusable(true);
            checkBox.setFocusableInTouchMode(true);
        }
        text.setText(item.getText());
        if (item.isEnableTextView()) {
            text.setEnabled(true);
            text.setFocusable(false);
            text.setFocusableInTouchMode(false);
        } else {
            text.setEnabled(false);
            text.setFocusable(true);
            text.setFocusableInTouchMode(true);
        }
        return convertView;
    }

}
