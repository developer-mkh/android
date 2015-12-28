package jp.gr.java_conf.mkh.alarm.widget;

import java.io.File;
import java.util.List;

import jp.gr.java_conf.mkh.alarm.R;
import jp.gr.java_conf.mkh.alarm.content.AlarmProviderConsts;
import jp.gr.java_conf.mkh.alarm.model.Alarm.EnableCode;
import jp.gr.java_conf.mkh.alarm.util.Util;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

public class AlarmDBAdapter extends android.support.v4.widget.CursorAdapter {

    public AlarmDBAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        StringBuilder sb = new StringBuilder();

        sb.append(cursor.getString(4)).append(":").append(Util.padZero(cursor.getInt(5))).append(" ");

        switch (Util.dbPlayModeToPlayMode(cursor.getInt(7))) {
        case PLAY_LOCAL_MUSIC_FILE:

            sb.append(new File(cursor.getString(9)).getName()).append("\n");
            break;
        case PLAY_RESOURCE_MUSIC_FILE:
            sb.append(Util.soundResIdToSoundName(context, cursor.getInt(8))).append("\n");
            break;
        }

        StringBuilder sb2 = new StringBuilder();
        if (Util.DbBooleanToBoolean(cursor.getInt(12))) {
            sb2.append(view.getResources().getString(R.string.msg_vibration_enable)).append(",");
        }
        if (Util.DbBooleanToBoolean(cursor.getInt(11))) {
            sb2.append(view.getResources().getString(R.string.msg_force_sound)).append(",");
        }
        if (sb2.length() != 0) {
            sb2.delete(sb2.length() - 1, sb2.length());
            sb2.append("\n");
            sb.append(sb2.toString());
        }

        final CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox8);

        if (cursor.getInt(6) == AlarmProviderConsts.TRUE) {
            checkBox.setChecked(true);
        } else {
            checkBox.setChecked(false);
        }

        List<EnableCode> list = Util.makeConditionList(context, cursor);
        sb.append(Util.enableCodeToStringForView(list, view.getContext())).append("\n");

        List<String> groupList = Util.makeGroupListForView(context, cursor);
        for (int i = 0; i < groupList.size(); i++) {
            sb.append(groupList.get(i)).append(",");
        }

        String str = sb.length() > 0 ? sb.toString().substring(0, sb.length() - 1) : sb.toString();


        ((TextView) view.findViewById(R.id.textView1)).setText(str);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewgroup) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.alarm_row, null);
        CheckBox checkBox = (CheckBox) v.findViewById(R.id.checkBox8);
        checkBox.setClickable(false);
        checkBox.setFocusable(false);
        checkBox.setFocusableInTouchMode(false);

        return v;
    }

}
