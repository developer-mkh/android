package jp.gr.java_conf.mkh.voc.receiver;

import android.app.Application;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.TelephonyManager;
import android.util.Log;

import jp.gr.java_conf.mkh.voc.MainActivity;
import jp.gr.java_conf.mkh.voc.preferences.Preferences;
import jp.gr.java_conf.mkh.voc.service.StartService;

/**
 * 電源オン時にスタートアップさせるためのブロードキャストレシーバー。
 * @since 2020/01/03
 * @author developer.mkh@gmail.com
 */
public class StartupReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent i = new Intent(context, StartService.class);
        context.startForegroundService(i);
    }
}
