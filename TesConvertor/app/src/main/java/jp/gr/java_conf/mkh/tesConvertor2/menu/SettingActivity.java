package jp.gr.java_conf.mkh.tesConvertor2.menu;

import jp.gr.java_conf.mkh.tesConvertor2.R;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/**
 * 設定画面。
 *
 * @author mkh
 *
 */
public class SettingActivity extends PreferenceActivity {

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.setting);
    }

    /**
     * ファイル変換後自動的に書き込みを行うか。
     * @param ctx コンテキスト
     * @return ファイル変換後自動的に書き込みを行う場合{@code true}
     */
    public static boolean isWriteSameTime(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean("check1", false);
    }

    /**
     * ハードウェアアクセラレーションを使用するか。
     * @param ctx コンテキスト
     * @return ハードウェアアクセラレーションを使用する場合{@code true}
     */
    public static boolean isUseHardwareAccelaration(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean("check2", false);
        //return false;
    }

    /**
     * 出力ファイル名自動入力を使用するか。
     * @param ctx コンテキスト
     * @return 出力ファイル名自動入力を使用する場合{@code true}
     */
    public static boolean isAutoFillOutputFileName(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean("check3", false);
    }
}
