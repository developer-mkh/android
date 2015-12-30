package jp.gr.java_conf.mkh.tesConvertor2.menu;

import java.io.IOException;
import java.io.InputStream;

import jp.gr.java_conf.mkh.tesConvertor2.R;
import jp.gr.java_conf.mkh.tesConvertor2.TesConvertorActivity;
import jp.gr.java_conf.mkh.tesConvertor2.util.FilePathUtil;
import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

/**
 * ヘルプ画面
 *
 * @author mkh
 *
 */
public class HelpActivity extends Activity {

    private TextView textView;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help);

        textView = (TextView) this.findViewById(R.id.textView4);

        String basePath = FilePathUtil.getFileBasePath(TesConvertorActivity.class);
        String inputDir = basePath + "tes/";
        String outputDir = basePath;

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
        PackageManager pm = getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
            version = info.versionName;
        } catch (NameNotFoundException e) {
            version = "0";
        }

        String help = sb.toString();
        help = help.replaceAll("%INPUT_PATH%", inputDir).replaceAll("%OUTPUT_PATH%", outputDir)
                .replaceAll("%VERSION%", version);

        textView.setText(Html.fromHtml(help));

    }

}
