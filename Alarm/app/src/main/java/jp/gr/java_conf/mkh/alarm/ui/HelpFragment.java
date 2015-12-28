package jp.gr.java_conf.mkh.alarm.ui;

import java.io.IOException;
import java.io.InputStream;

import jp.gr.java_conf.mkh.alarm.AlarmActivity;
import jp.gr.java_conf.mkh.alarm.R;
import jp.gr.java_conf.mkh.alarm.util.Util;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class HelpFragment extends Fragment {

    private TextView textView;

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.help, container, false);

        textView = (TextView) v.findViewById(R.id.textView1);

        String inputDir = Util.getFileBasePath(AlarmActivity.class);

        InputStream is = this.getResources().openRawResource(R.raw.help);
        StringBuilder sb = new StringBuilder();
        try {
            byte[] buffer = new byte[is.available()];
            while (is.read(buffer) != -1) {
                sb = sb.append(new String(buffer));
            }
        } catch (IOException e) {
            // リソースファイルの読み込みに失敗するとは思えない。
            e.printStackTrace();
        } finally {

            try {
                is.close();
            } catch (IOException e) {
                // リソースファイルの読み込みに失敗するとは思えない。
                e.printStackTrace();
            }
        }

        String version;
        PackageManager pm = getActivity().getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(getActivity().getPackageName(), PackageManager.GET_META_DATA);
            version = info.versionName;
        } catch (NameNotFoundException e) {
            version = "0";
        }

        String help = sb.toString();
        help = help.replaceAll("%INPUT_PATH%", inputDir).replaceAll("%VERSION%", version);

        textView.setText(Html.fromHtml(help));

        return v;
    }

}
