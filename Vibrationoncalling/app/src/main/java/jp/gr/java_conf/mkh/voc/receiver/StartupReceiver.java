package jp.gr.java_conf.mkh.voc.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import jp.gr.java_conf.mkh.voc.service.VibrationOnCallService;

/**
 * 電源オン時にスタートアップさせるためのブロードキャストレシーバー。
 * @since 2020/01/03
 * @author developer.mkh@gmail.com
 */
public class StartupReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent i = new Intent(context, VibrationOnCallService.class);
        context.startForegroundService(i);
    }
}
