package jp.gr.java_conf.mkh.alarm.ui;

import jp.gr.java_conf.mkh.alarm.R;
import jp.gr.java_conf.mkh.alarm.model.Alarm;
import jp.gr.java_conf.mkh.alarm.receiver.AlarmReceiver;
import jp.gr.java_conf.mkh.alarm.receiver.AlarmReceiver.PlayMode;
import jp.gr.java_conf.mkh.alarm.util.Util;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * アラーム停止画面。
 *
 * @author mkh
 *
 */
public class StopFragment extends Fragment {

    private Button button1;
    private Button button2;
    private Button button3;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.alarm_stop, container, false);

        button1 = (Button) v.findViewById(R.id.button1);
        button2 = (Button) v.findViewById(R.id.button2);
        button3 = (Button) v.findViewById(R.id.button3);

        button1.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                setAlarmManagerForStop(PlayMode.STOP_PLAY_MUSIC);
            }
        });

        button2.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction tran = fm.beginTransaction();
                if (Util.isTabletMode(getActivity())) {
                    SettingFragment sf = new SettingFragment();
                    tran.replace(R.id.fragment2, sf);
                } else {
                    AlarmFragment af = new AlarmFragment();
                    tran.replace(R.id.main_layout, af);
                }
                tran.addToBackStack(null);
                tran.commit();
            }
        });

        button3.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                setAlarmManagerForStop(PlayMode.STOP_PLAY_MUSIC_AND_GROUP);
            }
        });
        return v;
    }

    /**
     * アラームを止める。
     *
     * @param playMode
     *            STOP_PLAY_MUSICか、STOP_PLAY_MUSIC_AND_GROUPを想定。
     */
    private void setAlarmManagerForStop(PlayMode playMode) {

        Alarm alarm = new Alarm(null, null, 0, 0, false, playMode, 0, "", 0, false, false);
        Intent i = new Intent(getActivity().getApplicationContext(), AlarmReceiver.class);
        i.putExtra(AlarmReceiver.KEY_ALARM_INFO, alarm);
        PendingIntent receiver = PendingIntent.getBroadcast(getActivity().getApplicationContext(), 0, i,
                PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager am = (AlarmManager) getActivity().getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        am.set(AlarmManager.RTC_WAKEUP, SystemClock.currentThreadTimeMillis(), receiver);
    }

}
