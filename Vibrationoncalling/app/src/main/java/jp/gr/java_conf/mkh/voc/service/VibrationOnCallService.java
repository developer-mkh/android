package jp.gr.java_conf.mkh.voc.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import jp.gr.java_conf.mkh.voc.MainActivity;
import jp.gr.java_conf.mkh.voc.R;
import jp.gr.java_conf.mkh.voc.listener.MyPhoneStateListener;
import jp.gr.java_conf.mkh.voc.preferences.Preferences;

/**
 * メインサービス。
 * @since 2020/01/03
 * @author developer.mkh@gmail.com
 */
public class VibrationOnCallService extends IntentService {

    // 着信状況が変化した際の処理を行うリスナー
    private MyPhoneStateListener phoneStateListener;

    /**
     * コンストラクタ
     */
    public VibrationOnCallService() {
        super("VibrationOnCallService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // 通知をタップしたときに起動させるための準備
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.setFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
        );
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // 通知を作成
        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(getString(R.string.notificatio_channel_id), "Vibration on calling service", NotificationManager.IMPORTANCE_LOW);
        notificationManager.createNotificationChannel(channel);
        Notification notification = new Notification.Builder(getApplicationContext(), getString(R.string.notificatio_channel_id))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        phoneStateListener = new MyPhoneStateListener((Vibrator) getSystemService(Context.VIBRATOR_SERVICE));
        Preferences preferences = new Preferences(this);
        preferences.load(phoneStateListener);
        // 着信状況が変化した際の処理を行うリスナーの登録
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    @Override
    public void onDestroy(){

        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        phoneStateListener = null;

        super.onDestroy();
    }
}
