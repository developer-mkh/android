package jp.gr.java_conf.mkh.voc;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

import jp.gr.java_conf.mkh.voc.listener.MyPhoneStateListener;
import jp.gr.java_conf.mkh.voc.preferences.Preferences;
import jp.gr.java_conf.mkh.voc.service.VibrationOnCallService;

/**
 * メインアクティビティ。
 *
 * @since 2020/01/03
 * @author developer.mkh@gmail.com
 */
public class MainActivity extends AppCompatActivity {

    // 設定値の保存を管理する
    private Preferences preferences;
    // 着信状況が変化した際の処理を行うリスナー
    private MyPhoneStateListener phoneStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 着信状況とバイブレーションへと起動完了通知のアクセス許可
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.VIBRATE,
                        Manifest.permission.RECEIVE_BOOT_COMPLETED},
                1);

        // メンバ変数の初期化
        preferences = new Preferences(this);
        phoneStateListener = new MyPhoneStateListener((Vibrator) getSystemService(Context.VIBRATOR_SERVICE));
        preferences.load(phoneStateListener);

        // 表示を初期化
        initView();

        // コールバックの設定
        // applyボタン
        findViewById(R.id.button1).setOnClickListener(view -> {
            if (applyListener()) {
                ((TextView) findViewById(R.id.textViewErr)).setText(R.string.message_applied);
                Intent i = new Intent(getApplicationContext(), VibrationOnCallService.class);
                stopService(i);
                startForegroundService(i);
            }
        });
    }

    /**
     * 画面表示を初期化する。
     */
    private void initView() {
        ((EditText)findViewById(R.id.editText1)).setText(String.format(Locale.getDefault(), "%d", phoneStateListener.getSleepTime()));

        ((EditText)findViewById(R.id.editText2)).setText(
                Arrays.stream(phoneStateListener.getTimings())
                        .mapToObj(Long::toString)
                        .collect(Collectors.joining(","))
        );
        ((EditText)findViewById(R.id.editText3)).setText(
                Arrays.stream(phoneStateListener.getAmplitude())
                        .map(p -> {
                            if (p != 0) {
                                p = 1;
                            }
                            return p;
                        })
                        .mapToObj(Integer::toString)
                        .collect(Collectors.joining(","))
        );
    }

    /**
     * 入力内容を{@link MyPhoneStateListener}に設定する。
     * @return 入力値不備で設定できなかった時false
     */
    private boolean applyListener() {
        // エラーメッセージ領域をクリア
        ((TextView)findViewById(R.id.textViewErr)).setText("");

        // スリープ時間を取得
        String sleepTime = ((EditText)findViewById(R.id.editText1)).getText().toString();
        if (sleepTime.length() == 0) {
            sleepTime = getString(R.string.default_sleep_time);
            ((EditText)findViewById(R.id.editText1)).setText(getString(R.string.default_sleep_time));
        }

        // バイブレーションパターンを取得
        if (((EditText)findViewById(R.id.editText2)).getText().length() == 0) {
            ((EditText)findViewById(R.id.editText2)).setText(getString(R.string.default_timings));
        }
        String[] inputTimings = ((EditText)findViewById(R.id.editText2)).getText().toString().split(",");
        long[] timings = new long[inputTimings.length];
        for (int i = 0; i < inputTimings.length; i++) {
            try {
                timings[i] = Long.parseLong(inputTimings[i]);
            } catch (NumberFormatException e) {
                ((TextView)findViewById(R.id.textViewErr)).setText(R.string.message_timings);
                return false;
            }
        }

        // バイブレーション強度を取得
        if (((EditText)findViewById(R.id.editText3)).getText().length() == 0) {
            ((EditText)findViewById(R.id.editText3)).setText(getString(R.string.default_amplitude));
        }
        String[] inputAmplitude = ((EditText)findViewById(R.id.editText3)).getText().toString().split(",");
        int[] amplitude = new int[inputAmplitude.length];
        if (timings.length != amplitude.length) {
            ((TextView)findViewById(R.id.textViewErr)).setText(R.string.message_number_not_match);
            return false;
        }
        int tmp;
        for (int i = 0; i < inputAmplitude.length; i++) {
            try {
                tmp = Integer.parseInt(inputAmplitude[i]);
                if (!(tmp == 0 || tmp == 1)) {
                    ((TextView) findViewById(R.id.textViewErr)).setText(R.string.message_number_above_one);
                    return false;
                }
                amplitude[i] = tmp * VibrationEffect.DEFAULT_AMPLITUDE;
            } catch (NumberFormatException e) {
                ((TextView)findViewById(R.id.textViewErr)).setText(R.string.message_amplitude);
                return false;
            }
        }

        phoneStateListener.setSleepTime(Long.parseLong(sleepTime));
        phoneStateListener.setTimings(timings);
        phoneStateListener.setAmplitude(amplitude);
        preferences.save(phoneStateListener);
        return true;
    }
}
