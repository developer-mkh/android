/**
 *
 */
package jp.gr.java_conf.mkh.alarm.ui;

import java.util.Calendar;

import jp.gr.java_conf.mkh.alarm.R;
import jp.gr.java_conf.mkh.alarm.content.AlarmProviderConsts;
import jp.gr.java_conf.mkh.alarm.util.Util;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

/**
 * 休日設定。
 *
 * @author mkh
 *
 */
public class HolidayFragment extends Fragment {

    private TextView textView;
    private DatePicker datePicker;
    private Button button;

    private static final String KEY_OF_YEAR = "keyOfYear";
    private static final String KEY_OF_MONTH = "keyOfMonth";

    private float lastTouchX;

    private int year;
    private int month;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.holiday_setting, container, false);

        textView = (TextView) v.findViewById(R.id.textView1);
        datePicker = (DatePicker) v.findViewById(R.id.datePicker1);
        button = (Button) v.findViewById(R.id.button1);

        Calendar cal = Calendar.getInstance();

        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH);
        viewCalender(year, month);

        v.setOnTouchListener(new OnTouchScreen());
        button.setOnClickListener(new onClickButton());

        return v;
    }

    /**
     * ボタン1クリック時の動作
     *
     * @author mkh
     *
     */
    private class onClickButton implements OnClickListener {

        @Override
        public void onClick(View view) {

            int year = datePicker.getYear();
            int month = datePicker.getMonth();
            int day = datePicker.getDayOfMonth();

            ContentResolver cr = getActivity().getContentResolver();
            if (Util.isHoliday(cr, year, month, day)) {
                cr.delete(AlarmProviderConsts.CONTENT_URI_DELETE_HOLIDAY, null, new String[] { String.valueOf(year),
                        String.valueOf(month), String.valueOf(day) });
            } else {
                ContentValues cv = new ContentValues();
                cv.put(AlarmProviderConsts.YEAR, year);
                cv.put(AlarmProviderConsts.MONTH, month);
                cv.put(AlarmProviderConsts.DAY, day);
                cr.insert(AlarmProviderConsts.CONTENT_URI_INSERT_HOLIDAY, cv);
            }
            viewCalender(year, month);
        }

    }

    /**
     * カレンダーを表示する。
     *
     * @param year
     *            年
     * @param month
     *            月
     */
    private void viewCalender(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1);

        int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        cal.add(Calendar.DAY_OF_MONTH, lastDay - 1);
        int lastDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

        StringBuilder sb = new StringBuilder();

        sb.append(year).append("  ").append(month + 1).append("\n\n");
        textView.setText(sb.toString());
        sb.setLength(0);

        textView.append(" ");
        textView.append(coloredString(getText(R.string.s), "RED"));
        textView.append(" ");
        sb.append(" ").append(getText(R.string.m)).append(" ");
        sb.append(" ").append(getText(R.string.t)).append(" ");
        sb.append(" ").append(getText(R.string.w)).append(" ");
        sb.append(" ").append(getText(R.string.th)).append(" ");
        sb.append(" ").append(getText(R.string.f)).append(" ");
        textView.append(sb.toString());
        textView.append(" ");
        textView.append(coloredString(getText(R.string.st), "RED"));
        textView.append(" \n");

        int lastIndex = firstDayOfWeek + lastDay + (7 - lastDayOfWeek);

        for (int i = 0; i < lastIndex; i++) {
            if (i == 0) {
                continue;
            } else if (0 < i && i < firstDayOfWeek) {
                textView.append("   ");
            } else if (i < firstDayOfWeek + lastDay) {
                int day = i - firstDayOfWeek + 1;
                textView.append(day < 10 ? " " : "");
                if (i % 7 == 0) {
                    textView.append((coloredString(String.valueOf(day), "RED")));
                    textView.append(" \n");
                } else if ((i - 1) % 7 == 0) {
                    textView.append((coloredString(String.valueOf(day), "RED")));
                    textView.append(" ");
                } else {
                    if (Util.isHoliday(getActivity().getContentResolver(), year, month, day)) {
                        textView.append(coloredString(String.valueOf(day), "RED"));
                    } else {
                        textView.append(String.valueOf(day));
                    }
                    textView.append(" ");
                }
            } else {
                textView.append("   ");
            }
        }
    }

    /**
     * 色つき文字を返す。
     *
     * @param str
     *            文字列
     * @param color
     *            設定する色
     * @return 設定する色になった文字列
     */
    private Spanned coloredString(CharSequence str, String color) {
        String colorRedStart = "<font color=\"" + color + "\">";
        String colorRedEnd = "</font>";

        return Html.fromHtml(colorRedStart + str + colorRedEnd);
    }

    /**
     * 画面をタッチした時の処理。
     *
     * @author mkh
     *
     */
    private class OnTouchScreen implements OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                int top = textView.getTop();
                int bottom = textView.getBottom();
                float touchY = event.getY();
                if (top < touchY && touchY < bottom) {
                    lastTouchX = event.getX();
                } else {
                    lastTouchX = -1;
                }

                break;
            case MotionEvent.ACTION_UP:
                float currentX = event.getX();
                if (-1 < lastTouchX && lastTouchX < currentX) {
                    if (month == 0) {
                        year--;
                        month = 12;
                    }
                    viewCalender(year, --month);
                } else if (-1 < lastTouchX && currentX < lastTouchX) {
                    if (month == 11) {
                        year++;
                        month = -1;
                    }
                    viewCalender(year, ++month);
                }
                break;

            default:
                break;
            }
            return true;
        }
    }

    /**
     * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            viewCalender(savedInstanceState.getInt(KEY_OF_YEAR), savedInstanceState.getInt(KEY_OF_MONTH));
        }
    }

    /**
     *
     * @see android.support.v4.app.Fragment#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (datePicker != null) {
            outState.putInt(KEY_OF_YEAR, datePicker.getYear());
            outState.putInt(KEY_OF_MONTH, datePicker.getMonth());
        }
    }

}
