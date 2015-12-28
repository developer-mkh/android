package jp.gr.java_conf.mkh.alarm.ui;

import java.util.List;

import jp.gr.java_conf.mkh.alarm.R;
import jp.gr.java_conf.mkh.alarm.content.AlarmProviderConsts;
import jp.gr.java_conf.mkh.alarm.model.Alarm;
import jp.gr.java_conf.mkh.alarm.model.Alarm.EnableCode;
import jp.gr.java_conf.mkh.alarm.util.Util;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.LinearLayout;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;

/**
 * グループ設定画面。
 *
 * @author mkh
 *
 */
public class GroupFragment extends Fragment {

    /** タグ */
    private static final String TAG = "dialog";

    private ExpandableListView exListView;
    private TextView textView1;

    private String[] selectedAlarmCondition;

    private static final int SELECT_GROUP_DIALOG = 1;

    private ChangeAlarmEnable onAlarmEnableChangeListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.group, container, false);

        exListView = (ExpandableListView) v.findViewById(R.id.expandableListView1);
        textView1 = (TextView) v.findViewById(R.id.textView1);

        ContentResolver cr = getActivity().getContentResolver();

        if (savedInstanceState == null) {
            Cursor tmpCursor = cr.query(AlarmProviderConsts.CONTENT_URI_GROUP, null, null, null, null);
            ContentValues vals = new ContentValues();
            vals.put(AlarmProviderConsts.ENABLE, Boolean.FALSE);
            while (tmpCursor.moveToNext()) {
                cr.update(AlarmProviderConsts.CONTENT_URI_UPDATE_GROUP_ENABLE, vals, null,
                        new String[] { tmpCursor.getString(0) });
            }
        }

        Cursor cursor = cr.query(AlarmProviderConsts.CONTENT_URI_GROUP, null, null, null, null);
        ExpandableListAdapter adapter = new GroupDBAdapter(getActivity().getApplicationContext(), cursor,
                R.layout.group_row, R.layout.group_row, new String[] { AlarmProviderConsts.GROUP_NAME },
                new int[] { R.id.textView1 }, R.layout.group_child_row, R.layout.group_child_row, new String[] {
                        AlarmProviderConsts.HOUR, AlarmProviderConsts.MIN },
                new int[] { R.id.textView1, R.id.textView1 });
        exListView.setAdapter(adapter);

        registerForContextMenu(exListView);
        registerForContextMenu(textView1);

        if (Util.isTabletMode(getActivity())) {
            exListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition,
                        long id) {

                    String[] condition = Util.getConditoinForOneRecFromStr(((TextView) ((LinearLayout) v).getChildAt(0))
                            .getText().toString());
                    ContentResolver cr = getActivity().getContentResolver();
                    Cursor cursor = cr.query(AlarmProviderConsts.CONTENT_URI_GROUP_BELONG_TO_ALARM, null, null,
                            condition, null);
                    cursor.moveToFirst();
                    String groupName = cursor.getString(1);

                    Alarm alarm = Util.makeAlarmFromGroupName(childPosition, getActivity(), groupName);

                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    FragmentTransaction tran;
                    tran = fm.beginTransaction();
                    SettingFragment sf = new SettingFragment();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(SettingFragment.KEY_ALARM_DATA, alarm);
                    sf.setArguments(bundle);
                    tran.replace(R.id.fragment2, sf);
                    tran.addToBackStack(null);
                    tran.commit();

                    return true;
                }
            });
        }

        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Util.isTabletMode(getActivity())) {
            onAlarmEnableChangeListener = (ChangeAlarmEnable) activity;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v == exListView) {
            ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuInfo;

            long type = ExpandableListView.getPackedPositionType(info.packedPosition);
            switch ((int) type) {
            case ExpandableListView.PACKED_POSITION_TYPE_GROUP:
                getActivity().getMenuInflater().inflate(R.menu.group_ctx_menu, menu);
                break;
            case ExpandableListView.PACKED_POSITION_TYPE_CHILD:
                if (!Util.isTabletMode(getActivity())) {
                    getActivity().getMenuInflater().inflate(R.menu.groupchild_ctx_menu, menu);
                }
                break;
            default:
                break;
            }
        } else if (v == textView1) {
            getActivity().getMenuInflater().inflate(R.menu.group_edit_ctx_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction tran;
        tran = fm.beginTransaction();

        if (item.getMenuInfo() == null) {
            switch (item.getItemId()) {
            case R.id.item1:
                GroupSettingFragment gsf = new GroupSettingFragment();
                if (Util.isTabletMode(getActivity())) {
                    tran.replace(R.id.fragment2, gsf);
                } else {
                    tran.replace(R.id.main_layout, gsf);
                }
                break;
            case R.id.item2:
                SettingFragment sf = new SettingFragment();
                if (Util.isTabletMode(getActivity())) {
                    tran.replace(R.id.fragment2, sf);
                } else {
                    tran.replace(R.id.main_layout, sf);
                }
                break;
            }
            tran.addToBackStack(null);
            tran.commit();
            return true;
        }

        ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();

        long type = ExpandableListView.getPackedPositionType(info.packedPosition);
        long group = ExpandableListView.getPackedPositionGroup(info.packedPosition);

        switch ((int) type) {
        case ExpandableListView.PACKED_POSITION_TYPE_GROUP:
            tran = fm.beginTransaction();
            GroupSettingFragment gsf = new GroupSettingFragment();
            Bundle bundle = new Bundle();
            bundle.putLong(GroupSettingFragment.KEY_SELECTION_NO, group);
            gsf.setArguments(bundle);
            if (Util.isTabletMode(getActivity())) {
                tran.replace(R.id.fragment2, gsf);
            } else {
                tran.replace(R.id.main_layout, gsf);
            }
            tran.addToBackStack(null);
            tran.commit();
            break;
        case ExpandableListView.PACKED_POSITION_TYPE_CHILD:
            if (!Util.isTabletMode(getActivity())) {
                selectedAlarmCondition = Util
                        .getConditoinForOneRecFromStr((String) ((TextView) ((LinearLayout) info.targetView)
                                .getChildAt(0)).getText());
                GroupFragmentDialog.newInstance(SELECT_GROUP_DIALOG, GroupFragment.this).show(fm, TAG);
            }
            break;
        default:
            break;
        }

        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.group_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction tran;

        switch (item.getItemId()) {
        case R.id.item10:
            tran = fm.beginTransaction();
            GroupSettingFragment gsf = new GroupSettingFragment();
            if (Util.isTabletMode(getActivity())) {
                tran.replace(R.id.fragment2, gsf);
            } else {
                tran.replace(R.id.main_layout, gsf);
            }
            tran.addToBackStack(null);
            tran.commit();
            break;

        default:
            break;
        }
        return true;
    }

    /**
     * グループ表示に使用するDBアダプタ
     *
     * @author mkh
     *
     */
    public class GroupDBAdapter extends SimpleCursorTreeAdapter {

        public GroupDBAdapter(Context context, Cursor cursor, int collapsedGroupLayout, int expandedGroupLayout,
                String[] groupFrom, int[] groupTo, int childLayout, int lastChildLayout, String[] childFrom,
                int[] childTo) {
            super(context, cursor, collapsedGroupLayout, expandedGroupLayout, groupFrom, groupTo, childLayout,
                    lastChildLayout, childFrom, childTo);
        }

        @Override
        protected Cursor getChildrenCursor(Cursor cursor) {
            Activity activity = getActivity();

            if (activity == null) {
                return null;
            }
            ContentResolver cr = activity.getContentResolver();
            return cr.query(AlarmProviderConsts.CONTENT_URI_ALARM_BELONG_TO_GROUP, null, null,
                    new String[] { cursor.getString(0) }, null);
        }

        @Override
        protected void bindGroupView(View view, Context context, Cursor cursor, boolean isExpanded) {
            CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox3);
            checkBox.setChecked(Util.DbBooleanToBoolean(cursor.getInt(2)));
            checkBox.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    onCheckBoxClick((CheckBox) view);
                }
            });

            super.bindGroupView(view, context, cursor, isExpanded);
        }

        @Override
        protected void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {
            StringBuilder sb = new StringBuilder();
            sb.append(cursor.getString(4)).append(":").append(Util.padZero(cursor.getInt(5))).append(" ");
            if (Util.DbBooleanToBoolean(cursor.getInt(6))) {
                sb.append(getResources().getString(R.string.msg_alarm_enble));
            } else {
                sb.append(getResources().getString(R.string.msg_alarm_disanble));
            }
            sb.append("\n");

            List<EnableCode> list = Util.makeConditionList(context, cursor);
            sb.append(Util.enableCodeToStringForView(list, context));
            ((TextView) view.findViewById(R.id.textView1)).setText(sb.toString());
        }
    }

    /**
     * チェックボックスがクリックされた時の処理。
     *
     * @param cb
     *            チェックボックス
     */
    private void onCheckBoxClick(CheckBox cb) {
        String groupName = (String) ((TextView) ((LinearLayout) cb.getParent()).getChildAt(1)).getText();
        ContentResolver cr = getActivity().getContentResolver();
        Cursor groupCursor = cr.query(AlarmProviderConsts.CONTENT_URI_SELECT_GROUP, null, null,
                new String[] { groupName }, null);
        groupCursor.moveToFirst();
        Cursor alarmCursor = cr.query(AlarmProviderConsts.CONTENT_URI_ALARM_BELONG_TO_GROUP, null, null,
                new String[] { groupCursor.getString(0) }, null);
        while (alarmCursor.moveToNext()) {
            Util.changeAlarmEnableDisable(getActivity().getApplicationContext(), alarmCursor, cb.isChecked());
        }
        ContentValues cv = new ContentValues();

        cv.put(AlarmProviderConsts.ENABLE, cb.isChecked());
        cr.update(AlarmProviderConsts.CONTENT_URI_UPDATE_GROUP_ENABLE, cv, null,
                new String[] { groupCursor.getString(0) });

        if (onAlarmEnableChangeListener != null) {
            onAlarmEnableChangeListener.onChangeAlarmEnable(groupName, cb.isChecked());
        }
    }

    /**
     * ダイアログボックス
     *
     * @author mkh
     *
     */
    public static class GroupFragmentDialog extends DialogFragment {

        private static GroupFragment gf;

        /**
         * デフォルトコンストラクタ。
         */
        public GroupFragmentDialog() {
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
        public static GroupFragmentDialog newInstance(int id, GroupFragment groupFragment) {
            GroupFragmentDialog frag = new GroupFragmentDialog();
            Bundle args = new Bundle();
            args.putInt("id", id);
            frag.setArguments(args);
            gf = groupFragment;
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int id = getArguments().getInt("id");
            AlertDialog ret = null;
            switch (id) {
            case SELECT_GROUP_DIALOG:
                ret = new AlertDialog.Builder(getActivity()).setTitle(R.string.title_select_group)
                        .setItems(makeGroupList(), new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                onClickGroupName(which, gf.selectedAlarmCondition);

                            }
                        }).create();
                break;
            }
            return ret;
        }

        /**
         * グループ名のリストを作成する。
         *
         * @return グループ名のリスト
         */
        private String[] makeGroupList() {
            ContentResolver cr = getActivity().getContentResolver();
            Cursor cursor = cr.query(AlarmProviderConsts.CONTENT_URI_GROUP, null, null, null, null);
            String[] ret = new String[cursor.getCount()];
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                ret[i] = cursor.getString(1);
                cursor.moveToNext();
            }

            return ret;
        }

        /**
         * コンテキストメニューでグループを選択したときの処理。 元々所属していたグループから削除し、選択したグループに追加する。
         *
         * @param i
         *            リストで選択された時の位置
         * @param condition
         *            アラームを特定するためのキー
         */
        private void onClickGroupName(int i, String[] condition) {
            String groupName = makeGroupList()[i];
            ContentResolver cr = getActivity().getContentResolver();
            Cursor cursor = cr.query(AlarmProviderConsts.CONTENT_URI_SELECT_GROUP, null, null,
                    new String[] { groupName }, null);
            cursor.moveToFirst();
            int groupId = cursor.getInt(0);
            cr.delete(AlarmProviderConsts.CONTENT_URI_DELETE_ALARM_FROM_GROUP, null, condition);
            ContentValues values = new ContentValues();
            values.put(AlarmProviderConsts.GROUP, groupId);
            values.put(AlarmProviderConsts.YEAR, condition[0]);
            values.put(AlarmProviderConsts.MONTH, condition[1]);
            values.put(AlarmProviderConsts.DAY, condition[2]);
            values.put(AlarmProviderConsts.HOUR, condition[3]);
            values.put(AlarmProviderConsts.MIN, condition[4]);

            cr.insert(AlarmProviderConsts.CONTENT_URI_INSERT_GROUP_LIST, values);

        }
    }
}
