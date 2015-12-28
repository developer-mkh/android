package jp.gr.java_conf.mkh.alarm.ui;

import jp.gr.java_conf.mkh.alarm.R;
import jp.gr.java_conf.mkh.alarm.content.AlarmProviderConsts;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

/**
 * グループ設定画面。
 *
 * @author mkh
 *
 */
public class GroupSettingFragment extends Fragment {

    /** タグ */
    private static final String TAG = "dialog";

    private static final String KEY_OF_SAVE_FOR_CONFIG_CHANGE = "keyOfSaveForConfigChange";

    private Spinner spinner1;
    private EditText editText1;
    private Button button1;
    private Button button2;
    private Button button3;

    private long rowId;

    public static final String KEY_SELECTION_NO = "keySelectionNo";

    /**
     * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.group_setting, container, false);

        spinner1 = (Spinner) v.findViewById(R.id.spinner1);
        editText1 = (EditText) v.findViewById(R.id.editText1);
        button1 = (Button) v.findViewById(R.id.button1);
        button2 = (Button) v.findViewById(R.id.button2);
        button3 = (Button) v.findViewById(R.id.button3);

        Cursor cursor = getActivity().getContentResolver().query(AlarmProviderConsts.CONTENT_URI_GROUP, null, null,
                null, null);
        getActivity().startManagingCursor(cursor);
        SpinnerAdapter adapter = new SimpleCursorAdapter(getActivity(), R.layout.group_setting_row, cursor,
                new String[] { AlarmProviderConsts.GROUP_NAME }, new int[] { R.id.textView1 });
        spinner1.setAdapter(adapter);

        Bundle bundle = getArguments();
        if (bundle != null) {
            spinner1.setSelection((int) bundle.getLong(KEY_SELECTION_NO));
        }

        button1.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                String str = editText1.getText().toString();
                if (str.length() == 0) {
                    GroupSettingFragmentDialog.newInstance(R.string.err_msg_empty, GroupSettingFragment.this).show(
                            getFragmentManager(), TAG);
                    return;
                }
                ContentResolver cr = getActivity().getContentResolver();
                Cursor cursor = cr.query(AlarmProviderConsts.CONTENT_URI_SELECT_GROUP, null, null,
                        new String[] { editText1.getText().toString() }, null);
                if (cursor.getCount() > 0) {
                    GroupSettingFragmentDialog.newInstance(R.string.err_msg_group_duplicate, GroupSettingFragment.this)
                            .show(getFragmentManager(), TAG);
                    return;
                }
                ContentValues cv = new ContentValues();
                cv.put(AlarmProviderConsts.GROUP_NAME, str);
                cr.insert(AlarmProviderConsts.CONTENT_URI_INSERT_NEW_GROUP, cv);
                Toast.makeText(getActivity(), R.string.msg_group_added, Toast.LENGTH_SHORT).show();
            }
        });

        button2.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                ContentResolver cr = getActivity().getContentResolver();
                Cursor cursor = cr.query(AlarmProviderConsts.CONTENT_URI_SELECT_GROUP, null, null,
                        new String[] { editText1.getText().toString() }, null);
                if (cursor.getCount() > 0) {
                    GroupSettingFragmentDialog.newInstance(R.string.err_msg_group_duplicate, GroupSettingFragment.this)
                            .show(getFragmentManager(), TAG);
                    return;
                }

                ContentValues cv = new ContentValues();
                cv.put(AlarmProviderConsts.GROUP_NAME, editText1.getText().toString());
                cr.update(AlarmProviderConsts.CONTENT_URI_UPDATE_GROUP, cv, null,
                        new String[] { String.valueOf(rowId) });
                Toast.makeText(getActivity(), R.string.msg_group_changed, Toast.LENGTH_SHORT).show();
            }
        });

        button3.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                ContentResolver cr = getActivity().getContentResolver();

                Cursor cursor = cr.query(AlarmProviderConsts.CONTENT_URI_SELECT_GROUP, null, null,
                        new String[] { editText1.getText().toString() }, null);
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    String rowId = cursor.getString(0);
                    Cursor cursor2 = cr.query(AlarmProviderConsts.CONTENT_URI_ALARM_BELONG_TO_GROUP, null, null,
                            new String[] { rowId }, null);
                    if (cursor2.getCount() > 0) {
                        GroupSettingFragmentDialog.newInstance(R.string.err_msg_there_is_belong_alarm,
                                GroupSettingFragment.this).show(getFragmentManager(), TAG);
                        return;
                    }
                } else {
                    GroupSettingFragmentDialog.newInstance(R.string.err_msg_there_is_no_group,
                            GroupSettingFragment.this).show(getFragmentManager(), TAG);
                    return;
                }

                cr.delete(AlarmProviderConsts.CONTENT_URI_DELETE_GROUP, null, new String[] { String.valueOf(rowId) });
                Toast.makeText(getActivity(), R.string.msg_group_deleted, Toast.LENGTH_SHORT).show();
            }
        });
        return v;
    }

    /**
     * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            editText1.setText(savedInstanceState.getString(KEY_OF_SAVE_FOR_CONFIG_CHANGE));
        }
    }

    /**
     *
     * @see android.support.v4.app.Fragment#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence(KEY_OF_SAVE_FOR_CONFIG_CHANGE, editText1.getText());
    }

    /**
     * ダイアログボックス
     *
     * @author mkh
     *
     */
    public static class GroupSettingFragmentDialog extends DialogFragment {

        @SuppressWarnings("unused")
        private static GroupSettingFragment gsf;

        /**
         * デフォルトコンストラクタ。
         */
        public GroupSettingFragmentDialog() {
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
        public static GroupSettingFragmentDialog newInstance(int id, GroupSettingFragment groupSettingFragment) {
            GroupSettingFragmentDialog frag = new GroupSettingFragmentDialog();
            Bundle args = new Bundle();
            args.putInt("id", id);
            frag.setArguments(args);
            gsf = groupSettingFragment;
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int id = getArguments().getInt("id");
            AlertDialog ret = null;
            switch (id) {
            case R.string.err_msg_empty:
                ret = new AlertDialog.Builder(getActivity()).setTitle(R.string.title_err)
                        .setMessage(R.string.err_msg_empty).setPositiveButton(R.string.btn_ok_name, null).create();
                break;
            case R.string.err_msg_group_duplicate:
                ret = new AlertDialog.Builder(getActivity()).setTitle(R.string.title_err)
                        .setMessage(R.string.err_msg_group_duplicate).setPositiveButton(R.string.btn_ok_name, null)
                        .create();
                break;
            case R.string.err_msg_there_is_no_group:
                ret = new AlertDialog.Builder(getActivity()).setTitle(R.string.title_err)
                        .setMessage(R.string.err_msg_there_is_no_group).setPositiveButton(R.string.btn_ok_name, null)
                        .create();
                break;
            case R.string.err_msg_there_is_belong_alarm:
                ret = new AlertDialog.Builder(getActivity()).setTitle(R.string.title_err)
                        .setMessage(R.string.err_msg_there_is_belong_alarm)
                        .setPositiveButton(R.string.btn_ok_name, null).create();
                break;

            }
            return ret;
        }
    }
}
