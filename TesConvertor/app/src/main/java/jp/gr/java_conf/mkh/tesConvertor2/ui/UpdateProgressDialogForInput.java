package jp.gr.java_conf.mkh.tesConvertor2.ui;

import jp.gr.java_conf.mkh.tesConvertor2.R;
import android.app.Activity;

/**
 * ファイル入力時の進行状況を表示する非同期クラスの実装。
 *
 * @author mkh
 *
 */
public class UpdateProgressDialogForInput extends AbstructUpdateProgressDialog {

    /**
     * コンストラクタ。
     *
     * @param activity UIスレッドを持つアクティビティ
     * @param processed 全データサイズの取得機能を持つクラス
     * @param allData 全データサイズの取得機能を持つクラス
     */
    public UpdateProgressDialogForInput(Activity activity, DataSizeGettable process, DataSizeGettable allData) {
        super(activity, process, allData);
    }

    /**
     * {@inheritDoc}
     *
     * 親クラスの処理と、ダイアログのメッセージを設定する。
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog.setMessage(activity.getText(R.string.dialog_msg_inConvert));
    }

    /**
     * {@inheritDoc}
     *
     * 本実装では引数は使用していない。
     */
    @Override
    protected Void doInBackground(Object... params) {

        double numerator = 0.0;
        double denominator = allData.getAllDataSize();

        while (numerator < denominator) {
            numerator = processed.getProcessedDataSize();
            Integer progressValue = Integer.valueOf((int) (numerator / denominator * 100));
            publishProgress(progressValue);
        }
        return null;
    }
}
