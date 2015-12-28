package jp.gr.java_conf.mkh.alarm.ui;

import jp.gr.java_conf.mkh.alarm.R;
import jp.gr.java_conf.mkh.alarm.content.AlarmProviderConsts;
import jp.gr.java_conf.mkh.alarm.ctrl.AlarmCtrl;
import jp.gr.java_conf.mkh.alarm.model.Alarm;
import jp.gr.java_conf.mkh.alarm.util.Util;
import jp.gr.java_conf.mkh.alarm.widget.AlarmDBAdapter;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

public class AlarmFragment extends Fragment implements LoaderCallbacks<Cursor> {

    private ListView listView1;
    private TextView textView1;

    private AlarmDBAdapter alarmDBAdapter;

    /** タグ */
    private static final String TAG = "dialog";

    /** SharedPreferencesのキー */
    private static final String KEY_IS_SHOW_INFO_DIALOG = "isShowInfoDialog";

    public static final String DB_FILE = "alart_file.db";

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

        View v = inflater.inflate(R.layout.main_fragment, container, false);

        listView1 = (ListView) v.findViewById(R.id.listView1);
        listView1.setOnItemClickListener(new onItemClicked());
        ContentResolver cr = getActivity().getContentResolver();
        Cursor c = cr.query(AlarmProviderConsts.CONTENT_URI_ALARM, null, null, null, null);
        alarmDBAdapter = new AlarmDBAdapter(v.getContext(), c);
        listView1.setAdapter(alarmDBAdapter);

        textView1 = (TextView) v.findViewById(R.id.textView1);

        registerForContextMenu(textView1);
        registerForContextMenu(listView1);

        getLoaderManager().initLoader(0, null, this);
        return v;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v == textView1) {
            getActivity().getMenuInflater().inflate(R.menu.alarmlist_ctx_menu, menu);
        } else if (v == listView1) {
            getActivity().getMenuInflater().inflate(R.menu.alarmlist_ctx_menu_on_list, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction tran;

        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

        if (info == null) {
            switch (item.getItemId()) {
            case R.id.item1:
                tran = fm.beginTransaction();
                SettingFragment sf = new SettingFragment();
                tran.replace(R.id.main_layout, sf);
                tran.addToBackStack(null);
                tran.commit();
                break;
            }
        } else if (info.targetView.getId() == R.id.linearLayout1) {
            Alarm alarm = Util.makeAlarm(getActivity(), null, info.position);

            switch (item.getItemId()) {
            case R.id.item1:
                tran = fm.beginTransaction();
                SettingFragment sf = new SettingFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable(SettingFragment.KEY_ALARM_DATA, alarm);
                sf.setArguments(bundle);
                tran.replace(R.id.main_layout, sf);
                tran.addToBackStack(null);
                tran.commit();
                break;
            case R.id.item2:
                alarm.setIsEnable(false);
                new AlarmCtrl().setAlarm(alarm, getActivity());
                ContentResolver cr = getActivity().getContentResolver();
                cr.delete(
                        AlarmProviderConsts.CONTENT_URI_DELETE,
                        null,
                        new String[] { "1900", "1", "1", String.valueOf(alarm.getHour()),
                                String.valueOf(alarm.getMin()) });
                break;
            default:
                break;
            }
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        ContentResolver cr = getActivity().getContentResolver();
        Cursor cursor = cr.query(AlarmProviderConsts.CONTENT_URI_ALARM, null, null, null, null);
        if (cursor.getCount() == 0 && sp.getBoolean(KEY_IS_SHOW_INFO_DIALOG, Boolean.TRUE)) {
            AlarmFragmentDialog.newInstance(R.string.info_msg_thereis_not_alarm, this).show(getFragmentManager(), TAG);
        }
    }

    /**
     * リストが選択された時の処理。
     *
     * @author mkh
     *
     */
    private class onItemClicked implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

            CheckBox checkBox = (CheckBox) arg1.findViewById(R.id.checkBox8);
            boolean isChecked = !checkBox.isChecked();
            listView1.setItemChecked(arg2, isChecked);

            Util.changeAlarmEnableDisable(getActivity().getApplicationContext(), isChecked, arg2);
        }
    }

    /**
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onCreateLoader(int,
     *      android.os.Bundle)
     */
    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        Uri baseUri = AlarmProviderConsts.CONTENT_URI_ALARM;
        return new CursorLoader(getActivity().getApplicationContext(), baseUri, null, null, null, null);
    }

    /**
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoadFinished(android.support.v4.content.Loader,
     *      java.lang.Object)
     */
    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
        alarmDBAdapter.swapCursor(arg1);
    }

    /**
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoaderReset(android.support.v4.content.Loader)
     */
    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        alarmDBAdapter.swapCursor(null);
    }

    /**
     * ダイアログボックス
     *
     * @author mkh
     *
     */
    public static class AlarmFragmentDialog extends DialogFragment {

        @SuppressWarnings("unused")
        private static AlarmFragment af;

        /**
         * デフォルトコンストラクタ。
         */
        public AlarmFragmentDialog() {
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
        public static AlarmFragmentDialog newInstance(int id, AlarmFragment alarmFragment) {
            AlarmFragmentDialog frag = new AlarmFragmentDialog();
            Bundle args = new Bundle();
            args.putInt("id", id);
            frag.setArguments(args);
            af = alarmFragment;
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int id = getArguments().getInt("id");
            AlertDialog ret = null;
            switch (id) {
            case R.string.info_msg_thereis_not_alarm:
                ret = new AlertDialog.Builder(getActivity()).setTitle(R.string.title_info)
                        .setMessage(R.string.info_msg_thereis_not_alarm).setNeutralButton(R.string.btn_close, null)
                        .setNegativeButton(R.string.btn_close_never, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                                sp.edit().putBoolean(KEY_IS_SHOW_INFO_DIALOG, Boolean.FALSE).commit();
                            }
                        }).create();
                break;
            }
            return ret;
        }
    }
}