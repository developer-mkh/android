package jp.gr.java_conf.mkh.voc.listener;

import android.annotation.SuppressLint;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * 着信状況が変化した際の処理を行うリスナー。
 * @since 2020/01/03
 * @author developer.mkh@gmail.com
 */
public class MyPhoneStateListener extends PhoneStateListener {

    // バイブレーションサービス
    private Vibrator vibrator;
    // スリープする時間(ミリ秒)
    private long sleepTime = 3000;
    // バイブレーションパターン
    private long[] timings = {1000, 1000};
    // バイブレーション強度
    private int[] amplitude = {VibrationEffect.DEFAULT_AMPLITUDE, 0};

    /**
     * スリープ時間を返す。
     * @return 多分、端末デフォルトのバイブレーションをやり過ごすためのスリープ時間(ミリ秒)
     */
    public long getSleepTime() {
        return sleepTime;
    }

    /**
     * スリープ時間を設定する。
     * @param sleepTime 多分、端末デフォルトのバイブレーションをやり過ごすためのスリープ時間(ミリ秒)
     */
    public void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
    }

    /**
     * バイブレーションパターンを返す。
     * @return バイブレーションパターン
     * @see VibrationEffect
     */
    public long[] getTimings() {
        return timings;
    }

    /**
     * バイブレーションパターンを設定する。
     * @param timings バイブレーションパターン
     * @see VibrationEffect
     */
    public void setTimings(long[] timings) {
        this.timings = timings;
    }

    /**
     * バイブレーション強度を返す。
     * @return バイブレーション強度
     * @see VibrationEffect
     */
    public int[] getAmplitude() {
        return amplitude;
    }

    /**
     * バイブレーション強度を設定する。
     * @param amplitude バイブレーション強度
     * @see VibrationEffect
     */
    public void setAmplitude(int[] amplitude) {
        this.amplitude = amplitude;
    }

    /**
     * コンストラクタ。
     * @param vibrator バイブレーションサービス
     */
    public MyPhoneStateListener(Vibrator vibrator) {
        this.vibrator = vibrator;
    }

    @SuppressLint("MissingPermission")
    public void onCallStateChanged(int state, String callNumber) {

        // スリープする(多分、端末デフォルトのバイブレーションをやり過ごすため)。
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            Log.d("jp.gr.java_conf.mkh.voc", "InterruptedException");
        }

        String text = "";
        switch (state){
            case TelephonyManager.CALL_STATE_RINGING:
                text = "RINGING";
                vibrator.vibrate(VibrationEffect.createWaveform(timings,
                        amplitude,
                        0)
                );
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                text = "IDLE";

                vibrator.cancel();
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                text = "OFFHOOK";

                vibrator.cancel();
                break;
        }
        Log.d("jp.gr.java_conf.mkh.voc", text);
    }
}
