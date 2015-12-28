package jp.gr.java_conf.mkh.alarm.ctrl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import jp.gr.java_conf.mkh.alarm.content.AlarmProviderConsts;
import jp.gr.java_conf.mkh.alarm.model.Alarm;
import jp.gr.java_conf.mkh.alarm.model.Alarm.EnableCode;
import jp.gr.java_conf.mkh.alarm.receiver.AlarmReceiver;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

/**
 * アラームの制御クラス。
 *
 * @author mkh
 *
 */
public class AlarmCtrl {

    /**
     * アラームを設定する。
     *
     * @param alarm
     *            アラーム情報。この中のアラーム起動設定に従って設定/解除を行う。
     * @param context
     *            コンテキスト
     */
    public void setAlarm(Alarm alarm, Context context) {

        // rowidの取得
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(AlarmProviderConsts.CONTENT_URI_ALARM_ROWID_ONLY, null, null,
                new String[] { String.valueOf(alarm.getHour()), String.valueOf(alarm.getMin()) }, null);
        if (cursor.getCount() == 0) {
            return;
        }
        cursor.moveToFirst();
        String rowid = cursor.getString(0);

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setData(Uri.parse("http://" + rowid));
        intent.putExtra(AlarmReceiver.KEY_ALARM_INFO, alarm);

        PendingIntent receiver = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // キャンセルのときは、実行曜日情報がないことがあるので、ここでキャンセルする。
        if (!alarm.isEnable()) {
            am.cancel(receiver);
            return;
        }

        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, alarm.getHour());
        cal.set(Calendar.MINUTE, alarm.getMin());
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        // 次に起動するのは何日後か。
        int dayOfWeekNow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        Date settingDate = cal.getTime();
        int diff = Integer.MAX_VALUE;
        List<EnableCode> code = alarm.getEnableCode();
        // 毎日起動の場合
        if (code.contains(EnableCode.EveryDay)) {
            // 設定時間が現在より前の場合は24時間後にする。
            if (date.after(cal.getTime())) {
                diff = 24;
            } else {
                diff = 0;
            }
        } else if (code.contains(EnableCode.Weekday)) {
            // 平日起動の場合
            switch (dayOfWeekNow) {
            case Calendar.SUNDAY:
                diff = 24;
                break;
            case Calendar.SATURDAY:
                diff = 24 * 2;
                break;
            default:
                List<EnableCode> list = new ArrayList<Alarm.EnableCode>();
                list.add(EnableCode.MON);
                list.add(EnableCode.TUE);
                list.add(EnableCode.WED);
                list.add(EnableCode.THU);
                list.add(EnableCode.FRI);
                diff = Integer.MAX_VALUE;
                for (EnableCode target : list) {
                    int tmp = calcHourByDayOfWeek(target, cal.getTime());
                    if (tmp < diff) {
                        diff = tmp;
                    }
                }
                break;
            }
        } else {
            for (EnableCode target : code) {
                int tmp = calcHourByDayOfWeek(target, settingDate);
                if (tmp < diff) {
                    diff = tmp;
                }
            }
        }

        cal.add(Calendar.HOUR_OF_DAY, diff);

        if (alarm.isEnable()) {
            am.set(AlarmManager.RTC_WAKEUP, cal.getTime().getTime(), receiver);
        }
    }

    /**
     * 設定曜日と設定日時から、次の起動日まで何日(日数×24)あるか計算する。
     *
     * @param target
     *            設定曜日
     * @param settingDate
     *            設定日時(日は今日)
     * @return 次の起動日までの日数の時間表示(日数×24)
     */
    private int calcHourByDayOfWeek(EnableCode target, Date settingDate) {
        int ret = 0;
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date now = cal.getTime();

        switch (target) {
        case SUN:
            // 日曜起動
            ret = calcHour(Calendar.SUNDAY, settingDate, now);
            break;
        case MON:
            // 月曜起動
            ret = calcHour(Calendar.MONDAY, settingDate, now);
            break;
        case TUE:
            // 火曜起動
            ret = calcHour(Calendar.TUESDAY, settingDate, now);
            break;
        case WED:
            // 水曜起動
            ret = calcHour(Calendar.WEDNESDAY, settingDate, now);
            break;
        case THU:
            // 木曜起動
            ret = calcHour(Calendar.THURSDAY, settingDate, now);
            break;
        case FRI:
            // 金曜起動
            ret = calcHour(Calendar.FRIDAY, settingDate, now);
            break;
        case SAT:
            // 土曜起動
            ret = calcHour(Calendar.SATURDAY, settingDate, now);
            break;
        }
        return ret;
    }

    /**
     * 引数で与えられた設定曜日までの時間を求める。
     *
     * @param tagetDayOfWeek
     *            設定曜日
     * @param settingDate
     *            設定時刻(日付は現在日付)
     * @param now
     *            現在時刻
     * @return 引数で与えられた日時までの時間
     */
    private int calcHour(int tagetDayOfWeek, Date settingDate, Date now) {

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        int dayOfWeekNow = cal.get(Calendar.DAY_OF_WEEK);

        int ret;

        if ((dayOfWeekNow == tagetDayOfWeek) && (settingDate.compareTo(now) > 0)) {
            // 現在が設定曜日と同じで設定時刻よりも前
            ret = 0;
        } else if ((dayOfWeekNow == tagetDayOfWeek) && (settingDate.compareTo(now) <= 0)) {
            // 現在が設定曜日と同じで設定時刻の後
            ret = 24 * 7;
        } else if (dayOfWeekNow < tagetDayOfWeek) {
            // 現在が設定曜日よりも前
            ret = 24 * (tagetDayOfWeek - dayOfWeekNow);
        } else {
            // 現在が設定曜日よりも後
            ret = 24 * (7 + tagetDayOfWeek - dayOfWeekNow);
        }
        return ret;
    }
}
