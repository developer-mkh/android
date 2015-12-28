package jp.gr.java_conf.mkh.alarm.receiver;

import jp.gr.java_conf.mkh.alarm.content.AlarmProviderConsts;
import jp.gr.java_conf.mkh.alarm.ctrl.AlarmCtrl;
import jp.gr.java_conf.mkh.alarm.util.Util;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;

/**
 * アラームがクリアされる時に呼ばれる。
 *
 * @author mkh
 *
 */
public class OtherReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        ContentResolver cr = context.getContentResolver();
        int count = cr.query(AlarmProviderConsts.CONTENT_URI_ALARM, null, null, null, null).getCount();
        AlarmCtrl ctrl = new AlarmCtrl();
        for (int i = 0; i < count; i++) {
            ctrl.setAlarm(Util.makeAlarm(context, null, i), context);
        }
    }

}
