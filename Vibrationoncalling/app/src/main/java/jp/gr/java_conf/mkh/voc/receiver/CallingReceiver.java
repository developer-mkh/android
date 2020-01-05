package jp.gr.java_conf.mkh.voc.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * ブロードキャストレシーバー。
 * 着信状況の変化があった際にコールされる。
 *
 * @since 2020/01/03
 * @author developer.mkh@gmail.com
 */
public class CallingReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("jp.gr.java_conf.mkh.voc", "onReceive");
    }
}
