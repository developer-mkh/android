package jp.gr.java_conf.mkh.voc.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jp.gr.java_conf.mkh.voc.R;
import jp.gr.java_conf.mkh.voc.listener.MyPhoneStateListener;

/**
 * 設定値を管理する。
 * CallingReceiverに設定されているプロパティの永続化とその復元。
 *
 * @since 2020/01/03
 * @author developer.mkh@gmail.com
 */
public class Preferences {

    // 共有環境設定ファイル
    private SharedPreferences sharedPreferences;
    // コンテキスト
    private Context context;
    // 保存時のキー
    private static final String SLEEP_TIME_KEY = "sleepTimeKey";
    private static final String TIMINGS_KEY = "timingsKey";
    private static final String AMPLITUDE_KEY = "amplitudeKey";

    /**
     * コンストラクタ
     * @param context コンテキスト
     */
    public Preferences(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("jp.gr.java_conf.mkh.voc_Preferences", Context.MODE_PRIVATE);
    }

    /**
     * プロパティを保存する。
     * @param listener 保存対象の{@link MyPhoneStateListener}
     */
    public void save(MyPhoneStateListener listener) {
        String sleepTime = Long.toString(listener.getSleepTime());
        String timings = Arrays.stream(listener.getTimings())
                .mapToObj(Long::toString)
                .collect(Collectors.joining(","));
        String amplitude = Arrays.stream(listener.getAmplitude())
                .mapToObj(Integer::toString)
                .collect(Collectors.joining(","));

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SLEEP_TIME_KEY, sleepTime);
        editor.putString(TIMINGS_KEY, timings);
        editor.putString(AMPLITUDE_KEY, amplitude);
        editor.apply();
    }

    /**
     * プロパティを復元する。
     * @param listener 保存されたプロパティを設定する{@link MyPhoneStateListener}
     * @return 保存されたプロパティが設定された{@link MyPhoneStateListener}。
     */
    public MyPhoneStateListener load(MyPhoneStateListener listener) {
        String sleepTime = sharedPreferences.getString(SLEEP_TIME_KEY, context.getString(R.string.default_sleep_time));
        String timings = sharedPreferences.getString(TIMINGS_KEY, context.getString(R.string.default_timings));
        String amplitude = sharedPreferences.getString(AMPLITUDE_KEY, context.getString(R.string.default_amplitude));

        listener.setSleepTime(Long.parseLong(sleepTime));
        listener.setTimings(
                Stream.
                        of(timings.split(",")).
                        mapToLong(Long::parseLong).
                        toArray()
        );
        listener.setAmplitude(
                Stream.
                        of(amplitude.split(","))
                        .mapToInt(Integer::parseInt)
                        .toArray()
        );
        return listener;
    }
}
