package jp.gr.java_conf.mkh.alarm;

import java.io.File;

import jp.gr.java_conf.mkh.alarm.content.AlarmProviderConsts;
import jp.gr.java_conf.mkh.alarm.ui.AlarmFragment;
import jp.gr.java_conf.mkh.alarm.ui.ChangeAlarmEnable;
import jp.gr.java_conf.mkh.alarm.ui.DeleteFragment;
import jp.gr.java_conf.mkh.alarm.ui.GroupFragment;
import jp.gr.java_conf.mkh.alarm.ui.HelpFragment;
import jp.gr.java_conf.mkh.alarm.ui.HolidayFragment;
import jp.gr.java_conf.mkh.alarm.ui.SettingFragment;
import jp.gr.java_conf.mkh.alarm.ui.StopFragment;
import jp.gr.java_conf.mkh.alarm.util.Util;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * アラームのメインアクティビティ。
 *
 * @author mkh
 *
 */
public class AlarmActivity extends AppCompatActivity implements ChangeAlarmEnable {

    public static final String DB_FILE = "alart_file.db";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Util.isTabletMode(getApplicationContext())) {
            setContentView(R.layout.main_tablet);
        } else {
            setContentView(R.layout.main);
        }

        String path = Util.getFileBasePath(AlarmActivity.class);
        if (path.length() != 0) {
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
        }

        if (!Intent.ACTION_MAIN.equals(getIntent().getAction())) {
            replaceStopFragment();
            return;
        }

        if (savedInstanceState == null) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction tran = fm.beginTransaction();

            if (Util.isTabletMode(getApplicationContext())) {
                GroupFragment gf = new GroupFragment();
                SettingFragment sf = new SettingFragment();
                tran.replace(R.id.fragment1, gf);
                tran.replace(R.id.fragment2, sf);
            } else {
                Fragment af = new AlarmFragment();
                tran.replace(R.id.main_layout, af, "af");
            }
            tran.commit();
        }
    }

    @Override
    public void onChangeAlarmEnable(String groupName, boolean isChecked) {
        TextView textView = (TextView) findViewById(R.id.textView12);
        if (Util.isTabletMode(this) && textView != null) {
            ContentResolver cr = getContentResolver();
            Cursor groupCursor = cr.query(AlarmProviderConsts.CONTENT_URI_SELECT_GROUP, null, null,
                    new String[] { groupName }, null);
            groupCursor.moveToFirst();

            Cursor alarmCursor = cr.query(AlarmProviderConsts.CONTENT_URI_ALARM_BELONG_TO_GROUP, null, null,
                    new String[] { groupCursor.getString(0) }, null);
            String[] condition = textView.getText().toString().split(":");
            while (alarmCursor.moveToNext()) {
                if (condition[0].equals(alarmCursor.getString(4))
                        && condition[1].equals(Util.padZero(alarmCursor.getInt(5)))) {
                    ToggleButton tb = (ToggleButton) findViewById(R.id.toggleButton1);
                    tb.setChecked(isChecked);
                    break;
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (Util.isTabletMode(getApplicationContext())) {
            getMenuInflater().inflate(R.menu.group_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.main_menu, menu);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean ret = true;
        super.onOptionsItemSelected(item);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction tran;

        ContentResolver cr;
        switch (item.getItemId()) {
        case R.id.item1:
            tran = fm.beginTransaction();
            SettingFragment sf = new SettingFragment();
            if (Util.isTabletMode(getApplicationContext())) {
                tran.replace(R.id.fragment2, sf);
            } else {
                tran.replace(R.id.main_layout, sf);
            }
            tran.addToBackStack(null);
            tran.commit();
            break;
        case R.id.item2:
            cr = getContentResolver();
            cr.update(AlarmProviderConsts.CONTENT_URI_RESET_DELETE, null, null, null);
            tran = fm.beginTransaction();
            DeleteFragment df = new DeleteFragment();
            if (Util.isTabletMode(getApplicationContext())) {
                tran.replace(R.id.fragment1, df);
            } else {
                tran.replace(R.id.main_layout, df);
            }
            tran.addToBackStack(null);
            tran.commit();
            break;
        case R.id.item3:
            tran = fm.beginTransaction();
            HolidayFragment hf = new HolidayFragment();
            if (Util.isTabletMode(getApplicationContext())) {
                tran.replace(R.id.fragment2, hf);
            } else {
                tran.replace(R.id.main_layout, hf);
            }
            tran.addToBackStack(null);
            tran.commit();
            break;
        case R.id.item4:
            tran = fm.beginTransaction();
            HelpFragment helpf = new HelpFragment();
            if (Util.isTabletMode(getApplicationContext())) {
                tran.replace(R.id.fragment2, helpf);
            } else {
                tran.replace(R.id.main_layout, helpf);
            }
            tran.addToBackStack(null);
            tran.commit();
            break;
        case R.id.item5:
            replaceStopFragment();
            break;
        case R.id.item6:
            tran = fm.beginTransaction();
            GroupFragment gf = new GroupFragment();
            if (Util.isTabletMode(getApplicationContext())) {
                tran.replace(R.id.fragment1, gf);
            } else {
                tran.replace(R.id.main_layout, gf);
            }
            tran.addToBackStack(null);
            tran.commit();
            break;

        default:
            ret = false;
            break;
        }
        return ret;
    }

    /**
     * @see android.app.Activity#dispatchKeyEvent(android.view.KeyEvent)
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * アラーム停止画面を表示する。
     */
    private void replaceStopFragment() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction tran = fm.beginTransaction();
        StopFragment sf = new StopFragment();
        if (Util.isTabletMode(getApplicationContext())) {
            tran.replace(R.id.fragment1, new GroupFragment());
            tran.replace(R.id.fragment2, sf);
        } else {
            tran.replace(R.id.main_layout, sf);
        }
        tran.commit();
    }
}