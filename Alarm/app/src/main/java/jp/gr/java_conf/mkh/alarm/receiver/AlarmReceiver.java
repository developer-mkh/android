package jp.gr.java_conf.mkh.alarm.receiver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jp.gr.java_conf.mkh.alarm.AlarmActivity;
import jp.gr.java_conf.mkh.alarm.R;
import jp.gr.java_conf.mkh.alarm.content.AlarmProviderConsts;
import jp.gr.java_conf.mkh.alarm.ctrl.AlarmCtrl;
import jp.gr.java_conf.mkh.alarm.model.Alarm;
import jp.gr.java_conf.mkh.alarm.model.Alarm.EnableCode;
import jp.gr.java_conf.mkh.alarm.model.State;
import jp.gr.java_conf.mkh.alarm.util.Util;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Vibrator;

/**
 * AlarmManagerから呼び出される。
 *
 * @author mkh
 *
 */
public class AlarmReceiver extends BroadcastReceiver {

    public enum PlayMode {
        PLAY_LOCAL_MUSIC_FILE, PLAY_RESOURCE_MUSIC_FILE, STOP_PLAY_MUSIC, STOP_PLAY_MUSIC_AND_GROUP
    }

    /** キー:アラーム情報 */
    public static final String KEY_ALARM_INFO = "alarmInfo";

    private static List<State> stateList = new ArrayList<State>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        Alarm alarm = (Alarm) intent.getSerializableExtra(KEY_ALARM_INFO);

        if (alarm.getMode() == PlayMode.STOP_PLAY_MUSIC) {
            if (stateList.isEmpty()) {
                return;
            }
            stopAlarm(context);
            stateList.remove(0);

            return;
        } else if (alarm.getMode() == PlayMode.STOP_PLAY_MUSIC_AND_GROUP) {
            if (stateList.isEmpty()) {
                return;
            }
            stopAlarm(context);

            List<Integer> groupIdList = stateList.get(0).getGroupIdList();
            stateList.remove(0);

            ContentValues cv = new ContentValues();
            cv.put(AlarmProviderConsts.ENABLE, Boolean.FALSE);
            ContentResolver cr = context.getContentResolver();
            for (Integer id : groupIdList) {
                Cursor cursor = cr.query(AlarmProviderConsts.CONTENT_URI_ALARM_BELONG_TO_GROUP, null, null,
                        new String[] { String.valueOf(id) }, null);
                while (cursor.moveToNext()) {
                    Util.changeAlarmEnableDisable(context, cursor, false);
                }
            }

            return;
        }

        startAlarm(context, alarm);

        new AlarmCtrl().setAlarm((Alarm) intent.getSerializableExtra(KEY_ALARM_INFO), context);
        Intent alarmIntent = new Intent();
        alarmIntent.setClass(context, AlarmActivity.class);
        alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(alarmIntent);
    }

    /**
     * アラームを止める。
     *
     * @param context
     *            コンテキスト
     */
    private void stopAlarm(Context context) {
        if (stateList.isEmpty()) {
            return;
        }

        Vibrator vib = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vib.cancel();

        State state = stateList.get(0);

        MediaPlayer mp = state.getMp();
        if (mp.isPlaying()) {
            mp.stop();
            try {
                mp.prepare();
                mp.release();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        manager.setStreamVolume(AudioManager.STREAM_MUSIC, state.getVol(), 0);
        manager.setRingerMode(state.getRingerMode());
    }

    /**
     * アラームを鳴らす。
     *
     * @param ctx
     *            コンテキスト
     * @param alarm
     *            鳴らすアラームの情報を持ったアラームクラスのインスタンス
     */
    private void startAlarm(Context ctx, Alarm alarm) {

        boolean isEnableWeekday = false;
        if (alarm.getEnableCode() != null) {
            for (EnableCode code : alarm.getEnableCode()) {
                if (code == EnableCode.Weekday) {
                    isEnableWeekday = true;
                    break;
                }
            }
        }

        boolean isPlay = true;
        if (isEnableWeekday && Util.isHoliday(ctx.getContentResolver(), new Date())) {
            isPlay = false;
        }

        if (isPlay) {

            AudioManager manager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);

            State state = new State();
            state.setRingerMode(manager.getRingerMode());
            state.setVol(manager.getStreamVolume(AudioManager.STREAM_MUSIC));
            state.setGroupIdList(alarm.getGroupIdList());
            if (alarm.isForcePlay()) {
                manager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            }

            setVolume(alarm, ctx);
            if (alarm.isVibrate()) {
                setVibrate(alarm, ctx);
            }

            MediaPlayer mp = null;
            switch (alarm.getMode()) {
            case PLAY_LOCAL_MUSIC_FILE:
                mp = new MediaPlayer();
                try {
                    mp.setDataSource(alarm.getMusicFilePath());
                    mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mp.prepare();
                } catch (IllegalArgumentException e) {
                    playBundleSound(ctx, R.raw.alarm_001);
                    break;
                } catch (IllegalStateException e) {
                    playBundleSound(ctx, R.raw.alarm_001);
                    break;
                } catch (IOException e) {
                    playBundleSound(ctx, R.raw.alarm_001);
                    break;
                }
                mp.setLooping(true);
                mp.start();
                break;
            case PLAY_RESOURCE_MUSIC_FILE:
                mp = playBundleSound(ctx, alarm.getResId());
                break;
            }
            state.setMp(mp);

            stateList.add(state);
        }
    }

    /**
     * 付属のサウンドを演奏する。
     *
     * @param ctx
     *            コンテキスト
     * @param resId
     *            付属サウンドのリソースID
     * @return 作成したMediaPlayer
     */
    private MediaPlayer playBundleSound(Context ctx, int resId) {
        MediaPlayer mp = null;

        mp = MediaPlayer.create(ctx, resId);
        mp.setLooping(true);
        mp.start();
        return mp;

    }

    /**
     * 音量を設定する。<br>
     * 現在の音量をvolListに追加する。
     *
     * @param alarm
     *            アラーム情報
     * @param ctx
     *            コンテキスト
     */
    private void setVolume(Alarm alarm, Context ctx) {
        AudioManager manager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);

        int ringMode = manager.getRingerMode();
        int vol;

        if (ringMode != AudioManager.RINGER_MODE_NORMAL && !alarm.isForcePlay()) {
            vol = 0;
        } else {
            int maxVol = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            vol = maxVol / 2 + ((maxVol / 2) * alarm.getVol() / 100);
        }

        manager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0);
    }

    /**
     * バイブレーションを作動させる。
     *
     * @param alarm
     *            アラーム情報
     * @param ctx
     *            コンテキスト
     */
    private void setVibrate(Alarm alarm, Context ctx) {
        Vibrator manager = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
        manager.vibrate(new long[] { 1000, 500 }, 0);
    }
}
