package jp.gr.java_conf.mkh.alarm.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import jp.gr.java_conf.mkh.alarm.AlarmActivity;
import jp.gr.java_conf.mkh.alarm.R;
import jp.gr.java_conf.mkh.alarm.content.AlarmProviderConsts;
import jp.gr.java_conf.mkh.alarm.ctrl.AlarmCtrl;
import jp.gr.java_conf.mkh.alarm.model.Alarm;
import jp.gr.java_conf.mkh.alarm.model.Alarm.EnableCode;
import jp.gr.java_conf.mkh.alarm.model.ListItem;
import jp.gr.java_conf.mkh.alarm.receiver.AlarmReceiver.PlayMode;
import jp.gr.java_conf.mkh.alarm.util.Util;
import jp.gr.java_conf.mkh.alarm.widget.SettingViewAdapter;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

public class SettingFragment extends Fragment {

    private Button button3;
    private TextView text1;
    private TextView text2;
    private TextView text3;
    private TextView text4;
    private TextView text5;
    private TextView text6;
    private SeekBar seekBar1;
    private CheckBox checkBox1;
    private CheckBox checkBox2;
    private Spinner spinner1;

    private ToggleButton toggleButton1;
    private Button deleteButton1;

    private int hour;
    private int min;

    /** タグ */
    private static final String TAG = "dialog";

    public static final String KEY_ALARM_DATA = "alarmData";
    private static final String KEY_OF_SAVE_FOR_CONFIG_CHANGE = "keyOfSaveForConfigChange";

    private static final int TIME_PICKER = 1;
    private static final int SELECT_CONDITION = 2;
    private static final int SELECT_SOUND_FILE_PLACE = 3;
    private static final int SELECT_BUNDLE_SOUND = 4;
    private static final int SELECT_LOCAL_MUSIC_FILE = 5;

    private final static int CHECKBOX_EVERYDAY_INDEX = 7;
    private final static int CHECKBOX_WEEKDAY_INDEX = 8;

    private int bundleSoundResId = R.raw.alarm_001;
    private String soundFileName = "";
    private int selectedBundleSoundId = 0;
    private int selectLocalFileSoundId = 0;
    private PlayMode playMode = PlayMode.PLAY_RESOURCE_MUSIC_FILE;
    private int vol = 50;

    private List<EnableCode> code;
    private boolean isEditMode;
    private int before_hour;
    private int before_min;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
     *      android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View v = inflater.inflate(R.layout.alarm_setting, container, false);

        code = new ArrayList<Alarm.EnableCode>();

        button3 = (Button) v.findViewById(R.id.button3);
        text1 = (TextView) v.findViewById(R.id.textView11);
        text2 = (TextView) v.findViewById(R.id.textView12);
        text3 = (TextView) v.findViewById(R.id.textView13);
        text4 = (TextView) v.findViewById(R.id.textView14);
        text5 = (TextView) v.findViewById(R.id.textView1);
        text6 = (TextView) v.findViewById(R.id.textView2);
        seekBar1 = (SeekBar) v.findViewById(R.id.seekBar1);
        checkBox1 = (CheckBox) v.findViewById(R.id.checkBox1);
        checkBox2 = (CheckBox) v.findViewById(R.id.checkBox2);
        spinner1 = (Spinner) v.findViewById(R.id.spinner1);

        button3.setOnClickListener(new onClickButton3());
        text1.setOnClickListener(new onClickTextTime());
        text2.setOnClickListener(new onClickTextTime());
        text3.setOnClickListener(new onClickTextCondition());
        text4.setOnClickListener(new onClickTextCondition());
        text5.setOnClickListener(new onClickTextSound());
        text6.setOnClickListener(new onClickTextSound());
        seekBar1.setOnSeekBarChangeListener(new onChangeSeekbar());

        Cursor cursor = getActivity().getContentResolver().query(AlarmProviderConsts.CONTENT_URI_GROUP, null, null,
                null, null);
        SpinnerAdapter adapter = new SimpleCursorAdapter(getActivity(), R.layout.group_setting_row, cursor,
                new String[] { AlarmProviderConsts.GROUP_NAME }, new int[] { R.id.textView1 }, 0);

        spinner1.setAdapter(adapter);

        Bundle bundle = getArguments();

        if (Util.isTabletMode(getActivity())) {

            toggleButton1 = (ToggleButton) v.findViewById(R.id.toggleButton1);
            toggleButton1.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View view) {
                    if (!isExistAlarm()) {
                        toggleButton1.setEnabled(false);
                        toggleButton1.setChecked(!toggleButton1.isChecked());
                        return;
                    }

                    Alarm alarm = makeAlarmFromView();
                    alarm.setIsEnable((toggleButton1.isChecked()));
                    Util.changeAlarmEnableDisable(getActivity(), alarm);
                }
            });

            deleteButton1 = (Button) v.findViewById(R.id.button1);
            deleteButton1.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View view) {
                    if (!isExistAlarm()) {
                        return;
                    }
                    String[] selectionArgs = Util.getConditoinForOneRecFromStr(text2.getText().toString());
                    Alarm alarm = new Alarm(Integer.parseInt(selectionArgs[3]), Integer.parseInt(selectionArgs[4]));
                    alarm.setIsEnable(false);
                    new AlarmCtrl().setAlarm(alarm, getActivity().getApplicationContext());
                    ContentResolver cr = getActivity().getContentResolver();
                    cr.delete(AlarmProviderConsts.CONTENT_URI_DELETE, null, selectionArgs);

                }
            });
        }

        if (bundle == null) {

            isEditMode = false;

            Calendar cal = Calendar.getInstance();
            hour = cal.get(Calendar.HOUR_OF_DAY);
            min = cal.get(Calendar.MINUTE);
            updateDisplay();

            seekBar1.setMax(100);
            seekBar1.setProgress(50);

            text4.setText("-");
            text6.setText(getResources().getStringArray(R.array.bundle_sound)[selectedBundleSoundId]);
        } else {
            isEditMode = true;
            Alarm alarm = (Alarm) bundle.get(KEY_ALARM_DATA);
            restoreViewFromAlarm(alarm);
        }

        return v;
    }

    /**
     * 指定されたアラームが存在するかどうか。
     *
     * @return 存在する場合true
     */
    private boolean isExistAlarm() {
        String[] selectionArgs = Util.getConditoinForOneRecFromStr(text2.getText().toString());
        ContentResolver cr = getActivity().getContentResolver();
        Cursor cursor = cr.query(AlarmProviderConsts.CONTENT_URI_ALARM_ROWID_ONLY, null, null, new String[] {
                selectionArgs[3], selectionArgs[4] }, null);
        if (cursor.getCount() == 0) {
            return false;
        }
        return true;

    }

    /**
     * アラームインスタンスから画面を再構成する。
     *
     * @param alarm
     *            アラームインスタンス
     */
    private void restoreViewFromAlarm(Alarm alarm) {
        hour = alarm.getHour();
        min = alarm.getMin();
        updateDisplay();
        before_hour = hour;
        before_min = min;

        code = alarm.getEnableCode();
        text4.setText(Util.enableCodeToStringForView(code, getActivity().getApplicationContext()));

        playMode = alarm.getMode();
        switch (playMode) {
        case PLAY_RESOURCE_MUSIC_FILE:
            playMode = PlayMode.PLAY_RESOURCE_MUSIC_FILE;
            bundleSoundResId = alarm.getResId();
            selectedBundleSoundId = getBundleSound(bundleSoundResId);
            text6.setText(getResources().getStringArray(R.array.bundle_sound)[getBundleSound(bundleSoundResId)]);
            break;
        case PLAY_LOCAL_MUSIC_FILE:
            playMode = PlayMode.PLAY_LOCAL_MUSIC_FILE;
            soundFileName = alarm.getMusicFilePath();
            File file = new File(alarm.getMusicFilePath());
            text6.setText(file.getName());
            String[] files = getFileNames(Util.getFileBasePath(AlarmActivity.class));
            selectLocalFileSoundId = Arrays.binarySearch(files, text6.getText());
            selectLocalFileSoundId = selectLocalFileSoundId < 0 ? 0 : selectLocalFileSoundId;
            break;
        }

        Cursor groupList = getActivity().getContentResolver().query(AlarmProviderConsts.CONTENT_URI_GROUP, null, null,
                null, null);
        groupList.moveToFirst();
        int i;
        for (i = 0; i < groupList.getCount(); i++) {
            if (groupList.getInt(0) == alarm.getGroupIdList().get(0)) {
                break;
            }
            groupList.moveToNext();
        }
        spinner1.setSelection(i);

        seekBar1.setProgress(alarm.getVol());

        checkBox1.setChecked(alarm.isVibrate());
        checkBox2.setChecked(alarm.isForcePlay());

        if (Util.isTabletMode(getActivity())) {
            toggleButton1.setChecked(alarm.isEnable());
        }
    }

    /**
     * 時刻を表示するテキストがクリックされた時の処理
     *
     * @author mkh
     *
     */
    private class onClickTextTime implements OnClickListener {

        /**
         * @see android.view.View.OnClickListener#onClick(android.view.View)
         */
        @Override
        public void onClick(View v) {
            SettingFragmentDialog.newInstance(TIME_PICKER, SettingFragment.this).show(getFragmentManager(), TAG);
        }
    }

    /**
     * アラーム起動条件を表示するテキストがクリックされた時の処理
     *
     * @author mkh
     *
     */
    private class onClickTextCondition implements OnClickListener {

        /**
         * @see android.view.View.OnClickListener#onClick(android.view.View)
         */
        @Override
        public void onClick(View v) {
            SettingFragmentDialog.newInstance(SELECT_CONDITION, SettingFragment.this).show(getFragmentManager(), TAG);
        }
    }

    /**
     * サウンド表示部分が押された時の処理。
     *
     * @author mkh
     *
     */
    private class onClickTextSound implements OnClickListener {

        @Override
        public void onClick(View view) {
            SettingFragmentDialog.newInstance(SELECT_SOUND_FILE_PLACE, SettingFragment.this).show(getFragmentManager(),
                    TAG);
        }

    }

    /**
     * ボタン3(アラーム追加)が押された時の処理。
     *
     * @author mkh
     *
     */
    private class onClickButton3 implements OnClickListener {

        @Override
        public void onClick(View v) {

            int count = 0;
            AlarmCtrl alarmCtrl = new AlarmCtrl();

            if (code.size() == 0) {
                SettingFragmentDialog.newInstance(R.string.err_msg_not_select, SettingFragment.this).show(
                        getFragmentManager(), TAG);
                return;
            } else {
                ContentResolver cr = getActivity().getContentResolver();
                Cursor cursor = cr.query(AlarmProviderConsts.CONTENT_URI_ALARM_ROWID_ONLY, null, null, new String[] {
                        String.valueOf(hour), String.valueOf(min) }, null);

                count = cursor.getCount();
                if ((count != 0 && !isEditMode)
                        || (((before_hour != hour || before_min != min)) && isEditMode && count != 0)) {
                    SettingFragmentDialog.newInstance(R.string.err_msg_duplicate, SettingFragment.this).show(
                            getFragmentManager(), TAG);
                    return;
                }
                if (count > 0) {
                    Alarm deleteAlarm = new Alarm(before_hour, before_min);
                    deleteAlarm.setIsEnable(false);
                    alarmCtrl.setAlarm(deleteAlarm, getActivity().getApplicationContext());
                    String[] condition = new String[] { "1900", "1", "1", String.valueOf(before_hour),
                            String.valueOf(before_min) };
                    cr.delete(AlarmProviderConsts.CONTENT_URI_DELETE, null, condition);
                    cr.delete(AlarmProviderConsts.CONTENT_URI_DELETE_ALARM_FROM_GROUP, null, condition);
                }
            }

            Alarm alarm = makeAlarmFromView();

            ContentResolver cr = getActivity().getContentResolver();
            ContentValues cv = new ContentValues();
            cv.put(AlarmProviderConsts.HOUR, alarm.getHour());
            cv.put(AlarmProviderConsts.MIN, alarm.getMin());
            for (EnableCode item : code) {
                cv.putNull(item.toString());
            }
            Cursor cursor = (Cursor) spinner1.getSelectedItem();
            List<Integer> groupList = new ArrayList<Integer>();
            groupList.add(cursor.getInt(0));
            for (Integer i : groupList) {
                cv.putNull(i.toString());
            }
            cv.put(AlarmProviderConsts.PLAY_MODE, Util.PlayModeToDbPlayMode(alarm.getMode()));
            cv.put(AlarmProviderConsts.RESOURCE_ID, alarm.getResId());
            cv.put(AlarmProviderConsts.FILE_PATH, alarm.getMusicFilePath());
            cv.put(AlarmProviderConsts.VOL, alarm.getVol());
            cv.put(AlarmProviderConsts.FORCE_PLAY, Util.BooleanToDbBoolean(alarm.isForcePlay()));
            cv.put(AlarmProviderConsts.VIBRATE, Util.BooleanToDbBoolean(alarm.isVibrate()));
            cr.insert(AlarmProviderConsts.CONTENT_URI_INSERT_NEW_ALARM, cv);
            alarmCtrl.setAlarm(alarm, getActivity().getApplicationContext());

            if (!Util.isTabletMode(getActivity())) {
                FragmentManager manager = getFragmentManager();
                manager.popBackStack();
            }
        }
    }

    /**
     * 画面の表示内容からアラームインスタンスを作成する。
     *
     * @return アラームインスタンス
     */
    private Alarm makeAlarmFromView() {
        Cursor cursor = (Cursor) spinner1.getSelectedItem();
        List<Integer> groupList = new ArrayList<Integer>();
        groupList.add(cursor.getInt(0));
        return new Alarm(code, groupList, hour, min, true, playMode, bundleSoundResId, soundFileName, vol,
                checkBox2.isChecked(), checkBox1.isChecked());

    }

    /**
     * 時刻を設定したときの処理。
     *
     * @author mkh
     *
     */
    private class TimeSet implements OnTimeSetListener {

        /**
         * @see android.app.TimePickerDialog.OnTimeSetListener#onTimeSet(android.widget.TimePicker,
         *      int, int)
         */
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            hour = hourOfDay;
            min = minute;
            updateDisplay();
            if (Util.isTabletMode(getActivity())) {
                if (!isExistAlarm()) {
                    toggleButton1.setEnabled(false);
                } else {
                    toggleButton1.setEnabled(true);
                }
            }

        }
    }

    /**
     * 音量を設定したときの処理。
     *
     * @author mkh
     *
     */
    private class onChangeSeekbar implements OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekbar, int i, boolean flag) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekbar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekbar) {
            vol = seekbar.getProgress();
        }

    }

    /**
     * 表示の更新
     */
    private void updateDisplay() {
        text2.setText(hour + ":" + Util.padZero(min));
    }

    /**
     * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            Alarm alarm = (Alarm) savedInstanceState.getSerializable(KEY_OF_SAVE_FOR_CONFIG_CHANGE);
            restoreViewFromAlarm(alarm);
        }
    }

    /**
     *
     * @see android.support.v4.app.Fragment#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        List<Integer> groupList = new ArrayList<Integer>();
        if (spinner1 != null) {
            Cursor cursor = (Cursor) spinner1.getSelectedItem();
            groupList.add(cursor.getInt(0));
        }
        Alarm alarm = new Alarm(code, groupList, hour, min, true, playMode, bundleSoundResId, soundFileName, vol,
                checkBox2.isChecked(), checkBox1.isChecked());
        outState.putSerializable(KEY_OF_SAVE_FOR_CONFIG_CHANGE, alarm);

    }

    /**
     * 付属サウンドファイルに対応するインデックスを返す。
     *
     * @param resId
     *            付属サウンドファイルのリソースID
     */
    private int getBundleSound(int resId) {
        int ret = 0;
        switch (resId) {
        case R.raw.alarm_001:
            ret = 0;
            break;
        case R.raw.alarm_002:
            ret = 1;
            break;
        case R.raw.alarm_003:
            ret = 2;
            break;
        case R.raw.alarm_004:
            ret = 3;
            break;
        case R.raw.alarm_005:
            ret = 4;
            break;
        case R.raw.alarm_006:
            ret = 5;
            break;
        case R.raw.alarm_007:
            ret = 6;
            break;
        case R.raw.alarm_008:
            ret = 7;
            break;
        }
        return ret;

    }

    /**
     * ディレクトリに存在するファイル名の一覧を取得する。
     *
     * @param filePath
     *            ファイル名の一覧を取得するディレクトリ
     * @return ファイル名の一覧
     */
    private String[] getFileNames(String filePath) {
        if (filePath == null || filePath.length() == 0) {
            return new String[0];
        }
        File file = new File(filePath);
        File[] existFiles = file.listFiles();

        if (existFiles == null || existFiles.length == 0) {
            return new String[0];
        }

        List<String> fileNames = new ArrayList<String>(existFiles.length);
        for (File item : existFiles) {
            if (item.isFile()) {
                fileNames.add(item.getName());
            }
        }
        Collections.sort(fileNames);
        String[] ret = fileNames.size() == 0 ? new String[] {} : objectArrayToStringArray(fileNames.toArray());
        return ret;
    }

    /**
     * {@code Object}クラスの配列を{@code String}クラスの配列に変換する。
     *
     * @param objArray
     * @return
     */
    private String[] objectArrayToStringArray(Object[] objArray) {
        int length = objArray.length;

        String[] ret = new String[length];
        for (int i = 0; i < length; i++) {
            ret[i] = (String) objArray[i];
        }

        return ret;
    }

    /**
     * ダイアログボックス
     *
     * @author mkh
     *
     */
    public static class SettingFragmentDialog extends DialogFragment {

        private static SettingFragment sf;
        private static List<EnableCode> code;

        /**
         * デフォルトコンストラクタ。
         */
        public SettingFragmentDialog() {
        }

        /**
         * インスタンスを返却する。
         *
         * @param id
         *            表示するダイアログボックスを指定するID
         * @param staticFragment
         *            settingFragmentインスタンス
         * @return インスタンス
         */
        public static SettingFragmentDialog newInstance(int id, SettingFragment staticFragment) {
            SettingFragmentDialog frag = new SettingFragmentDialog();
            Bundle args = new Bundle();
            args.putInt("id", id);
            frag.setArguments(args);
            sf = staticFragment;
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int id = getArguments().getInt("id");
            AlertDialog ret = null;
            switch (id) {
            case R.string.err_msg_not_select:
                ret = new AlertDialog.Builder(getActivity()).setTitle(R.string.title_err)
                        .setMessage(R.string.err_msg_not_select).setPositiveButton(R.string.btn_ok_name, null).create();
                break;
            case R.string.err_msg_duplicate:
                ret = new AlertDialog.Builder(getActivity()).setTitle(R.string.title_err)
                        .setMessage(R.string.err_msg_duplicate).setPositiveButton(R.string.btn_ok_name, null).create();
                break;
            case TIME_PICKER:
                Calendar cal = Calendar.getInstance();
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                int min = cal.get(Calendar.MINUTE);
                ret = new TimePickerDialog(getActivity(), sf.new TimeSet(), hour, min, true);
                break;
            case SELECT_CONDITION:
                ListItem[] choiceItem = {
                        new ListItem(false, getResources().getString(R.string.sun), true, true),
                        new ListItem(false, getResources().getString(R.string.mon), true, true),
                        new ListItem(false, getResources().getString(R.string.tue), true, true),
                        new ListItem(false, getResources().getString(R.string.wed), true, true),
                        new ListItem(false, getResources().getString(R.string.thu), true, true),
                        new ListItem(false, getResources().getString(R.string.fri), true, true),
                        new ListItem(false, getResources().getString(R.string.sat), true, true),
                        //new ListItem(false, getResources().getString(R.string.everyday), true, true),
                        //new ListItem(false, getResources().getString(R.string.weekday), true, true)
                };
                code = new ArrayList<Alarm.EnableCode>();
                for (EnableCode item : sf.code) {
                    code.add(item);
                    int index = (int) Util.enableCodeToDbCode(item);
                    choiceItem[index].setChecked(true);
                    if (item == EnableCode.EveryDay || item == EnableCode.Weekday) {
                        for (int i = 0; i < choiceItem.length; i++) {
                            if (i != index) {
                                choiceItem[i].setChecked(false);
                                choiceItem[i].setEnableCheckBox(false);
                                choiceItem[i].setEnableTextView(false);
                            }
                        }
                    }
                }

                LayoutInflater inflater = getActivity().getLayoutInflater();
                ListView listView = (ListView) inflater.inflate(R.layout.alarm_condition_setting_list, null, false);
                SettingViewAdapter adapter = new SettingViewAdapter(getActivity(), choiceItem);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new onClickSelectDialog());

                ret = new AlertDialog.Builder(getActivity()).setView(listView)
                        .setTitle(R.string.dialog_select_condition)
                        .setPositiveButton(R.string.btn_ok_name, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialoginterface, int i) {
                                sf.code = code;
                                if (code.isEmpty()) {
                                    sf.text4.setText("-");
                                } else {
                                    sf.text4.setText(Util.enableCodeToStringForView(code, getActivity()
                                            .getApplicationContext()));
                                }
                            }
                        }).setNegativeButton(R.string.btn_cancel_name, null).create();
                break;
            case SELECT_SOUND_FILE_PLACE:
                String basePath = Util.getFileBasePath(AlarmActivity.class);
                if (basePath == null || sf.getFileNames(basePath).length == 0) {
                    // 添付音源しかないとき
                    ret = new AlertDialog.Builder(getActivity()).setTitle(R.string.title_select_sound)
                            .setItems(R.array.bundle_sound, null)
                            .setSingleChoiceItems(R.array.bundle_sound, sf.selectedBundleSoundId, new onClickItem())
                            .setPositiveButton(R.string.btn_ok_name, new onClickBundleSoundPositive())
                            .setNeutralButton(R.string.btn_play_name, new onClickBundleSoundNeutral()).create();
                } else {
                    // サウンドファイルがあるとき
                    ret = new AlertDialog.Builder(getActivity()).setTitle(R.string.title_select_sound_place)
                            .setItems(R.array.select_sound_msg, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialoginterface, int i) {
                                    switch (i) {
                                    case 0:
                                        SettingFragmentDialog.newInstance(SELECT_BUNDLE_SOUND, sf).show(
                                                getFragmentManager(), TAG);
                                        break;
                                    case 1:
                                        SettingFragmentDialog.newInstance(SELECT_LOCAL_MUSIC_FILE, sf).show(
                                                getFragmentManager(), TAG);
                                        break;
                                    }
                                }
                            }).create();
                }
                break;
            case SELECT_BUNDLE_SOUND:
                ret = new AlertDialog.Builder(getActivity()).setTitle(R.string.title_select_sound)
                        .setItems(R.array.bundle_sound, null)
                        .setSingleChoiceItems(R.array.bundle_sound, sf.selectedBundleSoundId, new onClickItem())
                        .setPositiveButton(R.string.btn_ok_name, new onClickBundleSoundPositive())
                        .setNeutralButton(R.string.btn_play_name, new onClickBundleSoundNeutral()).create();
                break;
            case SELECT_LOCAL_MUSIC_FILE:
                final String path = Util.getFileBasePath(AlarmActivity.class);
                final String[] files = sf.getFileNames(path);
                sf.soundFileName = path + files[sf.selectLocalFileSoundId];
                ret = new AlertDialog.Builder(getActivity()).setTitle(R.string.title_select_sound)
                        .setSingleChoiceItems(files, sf.selectLocalFileSoundId, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialoginterface, int i) {
                                sf.selectLocalFileSoundId = i;
                                sf.soundFileName = path + files[sf.selectLocalFileSoundId];
                            }
                        }).setPositiveButton(R.string.btn_ok_name, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialoginterface, int i) {
                                sf.playMode = PlayMode.PLAY_LOCAL_MUSIC_FILE;
                                sf.text6.setText(files[sf.selectedBundleSoundId]);

                            }
                        }).setNeutralButton(R.string.btn_play_name, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialoginterface, int i) {
                                MediaPlayer mp = new MediaPlayer();
                                try {
                                    mp.setDataSource(sf.soundFileName);
                                    mp.prepare();
                                } catch (IllegalArgumentException e) {
                                    e.printStackTrace();
                                } catch (IllegalStateException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                mp.setLooping(false);
                                mp.start();
                                SettingFragmentDialog.newInstance(SELECT_LOCAL_MUSIC_FILE, sf).show(
                                        getFragmentManager(), TAG);
                            }
                        }).create();
                break;
            }
            return ret;
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            sf.code = code;
        }

        /**
         * 付属サウンドファイルの選択画面でアイテムをクリックしたときの処理。
         *
         * @return
         */
        private class onClickItem implements android.content.DialogInterface.OnClickListener {

            @Override
            public void onClick(DialogInterface dialoginterface, int i) {
                switch (i) {
                case 0:
                    sf.bundleSoundResId = R.raw.alarm_001;
                    sf.selectedBundleSoundId = 0;
                    break;
                case 1:
                    sf.bundleSoundResId = R.raw.alarm_002;
                    sf.selectedBundleSoundId = 1;
                    break;
                case 2:
                    sf.bundleSoundResId = R.raw.alarm_003;
                    sf.selectedBundleSoundId = 2;
                    break;
                case 3:
                    sf.bundleSoundResId = R.raw.alarm_004;
                    sf.selectedBundleSoundId = 3;
                    break;
                case 4:
                    sf.bundleSoundResId = R.raw.alarm_005;
                    sf.selectedBundleSoundId = 3;
                    break;
                case 5:
                    sf.bundleSoundResId = R.raw.alarm_006;
                    sf.selectedBundleSoundId = 3;
                    break;
                case 6:
                    sf.bundleSoundResId = R.raw.alarm_007;
                    sf.selectedBundleSoundId = 3;
                    break;
                case 7:
                    sf.bundleSoundResId = R.raw.alarm_008;
                    sf.selectedBundleSoundId = 3;
                    break;
                }

            }

        }

        /**
         * 付属サウンドファイルの選択画面でOKをクリックしたときの処理。
         *
         * @author mkh
         *
         */
        private class onClickBundleSoundPositive implements android.content.DialogInterface.OnClickListener {

            @Override
            public void onClick(DialogInterface dialoginterface, int i) {
                sf.playMode = PlayMode.PLAY_RESOURCE_MUSIC_FILE;
                sf.text6.setText(getResources().getStringArray(R.array.bundle_sound)[sf
                        .getBundleSound(sf.bundleSoundResId)]);
            }
        }

        /**
         * 付属サウンドファイルの選択画面で再生をクリックしたときの処理。
         *
         * @author mkh
         *
         */
        private class onClickBundleSoundNeutral implements android.content.DialogInterface.OnClickListener {

            @Override
            public void onClick(DialogInterface dialoginterface, int i) {
                MediaPlayer mp = MediaPlayer.create(getActivity().getApplicationContext(), sf.bundleSoundResId);
                mp.setLooping(false);
                mp.start();
                SettingFragmentDialog.newInstance(SELECT_BUNDLE_SOUND, sf).show(getFragmentManager(), TAG);
            }

        }

        /**
         * 起動条件ダイアログがクリックされた時の処理
         *
         * @author mkh
         *
         */
        private class onClickSelectDialog implements OnItemClickListener {

            /**
             * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView,
             *      android.view.View, int, long)
             */
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                ListItem item = (ListItem) arg0.getItemAtPosition(arg2);

                if (arg3 == CHECKBOX_EVERYDAY_INDEX || arg3 == CHECKBOX_WEEKDAY_INDEX) {
                    updateCheckBoxAndTextView(item, arg0, arg3);
                    code.clear();
                } else {
                    item.setChecked(!item.isChecked());
                    ((CheckBox) arg1.findViewById(R.id.checkBox8)).setChecked(item.isChecked());
                }

                if (item.isChecked()) {
                    code.add(Util.dbCodeToEnableCode((int) arg3));
                } else {
                    code.remove(Util.dbCodeToEnableCode((int) arg3));
                }

                return;
            }

            /**
             * チェックボックスと表示テキストのアップデート。
             *
             * @param item
             *            対象のリスト
             * @param parent
             *            itemの親
             * @param id
             *            設定可能とするitemのid
             */
            private void updateCheckBoxAndTextView(ListItem item, AdapterView<?> parent, long id) {
                if (item.isChecked()) {
                    for (int i = 0; i < parent.getCount(); i++) {
                        item = (ListItem) parent.getItemAtPosition(i);
                        item.setChecked(false);
                        item.setEnableCheckBox(true);
                        item.setEnableTextView(true);
                        ((CheckBox) ((LinearLayout) parent.getChildAt(i)).getChildAt(0)).setChecked(false);
                        ((LinearLayout) parent.getChildAt(i)).getChildAt(0).setEnabled(true);
                        ((LinearLayout) parent.getChildAt(i)).getChildAt(1).setEnabled(true);
                    }
                } else {
                    for (int i = 0; i < parent.getCount(); i++) {
                        item = (ListItem) parent.getItemAtPosition(i);
                        if (i != id) {
                            item.setChecked(false);
                            item.setEnableCheckBox(false);
                            item.setEnableTextView(false);
                            ((CheckBox) ((LinearLayout) parent.getChildAt(i)).getChildAt(0)).setChecked(false);
                            ((LinearLayout) parent.getChildAt(i)).getChildAt(0).setEnabled(false);
                            ((LinearLayout) parent.getChildAt(i)).getChildAt(1).setEnabled(false);
                        } else {
                            item.setChecked(true);
                            item.setEnableCheckBox(true);
                            item.setEnableTextView(true);
                            ((CheckBox) ((LinearLayout) parent.getChildAt(i)).getChildAt(0)).setChecked(true);
                        }
                    }
                }
            }
        }
    }
}
