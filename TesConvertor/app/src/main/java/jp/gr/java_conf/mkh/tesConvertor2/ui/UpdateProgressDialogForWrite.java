package jp.gr.java_conf.mkh.tesConvertor2.ui;

import jp.gr.java_conf.mkh.tesConvertor2.R;
import android.app.Activity;

/**
 * ファイル出力時の進行状況を表示する非同期クラスの実装。
 *
 * @author mkh
 *
 */
public class UpdateProgressDialogForWrite extends AbstructUpdateProgressDialog {

    /**
     * コンストラクタ。
     *
     * @param activity UIスレッドを持つアクティビティ
     * @param processed 全データサイズの取得機能を持つクラス
     * @param allData 全データサイズの取得機能を持つクラス
     */
    public UpdateProgressDialogForWrite(Activity activity, DataSizeGettable processed, DataSizeGettable allData) {
        super(activity, processed, allData);
    }

    /**
     * {@inheritDoc}
     *
     * 親クラスの処理と、ダイアログのメッセージを設定する。
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog.setMessage(activity.getText(R.string.dialog_msg_inSave));
    }

    /**
     * {@inheritDoc}
     *
     * 本実装では引数は使用していない。
     */
    @Override
    protected Void doInBackground(Object... params) {

        int processedDataSize = (int) processed.getProcessedDataSize();

        while (processedDataSize <= 100) {
            if (processedDataSize < 100) {
                processedDataSize = (int) processed.getProcessedDataSize();
                publishProgress(Integer.valueOf(processedDataSize));
            } else {
                publishProgress(Integer.valueOf(100));
                return null;
            }
        }
        return null;
    }

}
