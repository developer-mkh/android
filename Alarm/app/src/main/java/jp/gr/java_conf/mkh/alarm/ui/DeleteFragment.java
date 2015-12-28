package jp.gr.java_conf.mkh.alarm.ui;

import jp.gr.java_conf.mkh.alarm.R;
import jp.gr.java_conf.mkh.alarm.content.AlarmProviderConsts;
import jp.gr.java_conf.mkh.alarm.ctrl.AlarmCtrl;
import jp.gr.java_conf.mkh.alarm.model.Alarm;
import jp.gr.java_conf.mkh.alarm.util.Util;
import jp.gr.java_conf.mkh.alarm.widget.AlarmDBAdapter;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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
import android.widget.TextView;

/**
 * 削除処理。
 *
 * @author mkh
 *
 */
public class DeleteFragment extends Fragment implements LoaderCallbacks<Cursor> {

    /** キー:アラーム情報 */
    public static final String KEY_ALARM_LIST = "alarmList";

    private Button button6;
    private ListView listView2;

    private AlarmDBAdapter alarmDBAdapter;

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
        View v = inflater.inflate(R.layout.delete, container, false);

        ContentResolver cr = getActivity().getContentResolver();
        Cursor c = cr.query(AlarmProviderConsts.CONTENT_URI_ALARM_WITH_DELETE_FLAG, null, null, null, null);
        alarmDBAdapter = new AlarmDBAdapter(v.getContext(), c);
        listView2 = (ListView) v.findViewById(R.id.listView2);
        listView2.setAdapter(alarmDBAdapter);

        button6 = (Button) v.findViewById(R.id.button6);

        button6.setOnClickListener(new onClickButton6());
        listView2.setOnItemClickListener(new onItemClicked());

        getLoaderManager().initLoader(0, null, this);

        return v;
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
            checkBox.setChecked(isChecked);

            ContentResolver cr = getActivity().getContentResolver();
            ContentValues cv = new ContentValues();
            cv.put(AlarmProviderConsts.DELETE, isChecked);
            cr.update(AlarmProviderConsts.CONTENT_URI_UPDATE_DELETE, cv,
                    (String) ((TextView) arg1.findViewById(R.id.textView1)).getText(), null);
        }
    }

    /**
     * ボタン6が押下された時の処理
     *
     * @author mkh
     *
     */
    private class onClickButton6 implements OnClickListener {

        @Override
        public void onClick(View v) {

            ContentResolver cr = getActivity().getContentResolver();

            int count = listView2.getChildCount();
            for (int i = 0; i < count; i++) {
                LinearLayout layout = (LinearLayout) listView2.getChildAt(i);
                boolean isChecked = ((CheckBox) layout.getChildAt(0)).isChecked();
                if (isChecked) {
                    String[] selectionAtgs = Util.getConditoinForOneRecFromStr((String) ((TextView) layout
                            .getChildAt(1)).getText());
                    Alarm alarm = new Alarm(Integer.parseInt(selectionAtgs[3]), Integer.parseInt(selectionAtgs[4]));
                    new AlarmCtrl().setAlarm(alarm, getActivity().getApplicationContext());
                    cr.delete(AlarmProviderConsts.CONTENT_URI_DELETE, null, selectionAtgs);
                }
            }
        }
    }

    /**
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onCreateLoader(int,
     *      android.os.Bundle)
     */
    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        Uri baseUri = AlarmProviderConsts.CONTENT_URI_ALARM_WITH_DELETE_FLAG;
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

}
