package jp.gr.java_conf.mkh.tesConvertor2.ui;

import jp.gr.java_conf.mkh.tesConvertor2.R;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

/**
 * {@code ProgressDialog}を更新する非同期クラス。
 *
 * @author mkh
 *
 */
public abstract class AbstructUpdateProgressDialog extends AsyncTask<Object, Integer, Void> {

    /** UIスレッドを持つアクティビティ */
    protected Activity activity;
    /** 処理済みデータサイズの取得機能を持つクラス */
    protected DataSizeGettable processed;
    /** 全データサイズの取得機能を持つクラス */
    protected DataSizeGettable allData;
    /** 表示する{@code ProgressDialog}のインスタンス */
    protected ProgressDialog dialog;

    /**
     * コンストラクタ。
     *
     * @param activity UIスレッドを持つアクティビティ
     * @param processed 全データサイズの取得機能を持つクラス
     * @param allData 全データサイズの取得機能を持つクラス
     */
    public AbstructUpdateProgressDialog(Activity activity, DataSizeGettable processed, DataSizeGettable allData) {
        this.activity = activity;
        this.processed = processed;
        this.allData = allData;
    }

    /**
     * {@inheritDoc}
     *
     * {@code ProgressDialog}の準備をする。
     */
    @Override
    protected void onPreExecute() {

        super.onPreExecute();
        dialog = new ProgressDialog(activity);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setProgress(0);
        dialog.setMax(100);
        dialog.setMessage(activity.getText(R.string.dialog_msg_inConvert));

        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }

    /**
     * {@inheritDoc}
     *
     * 非同期スレッドで実行する処理を実装する。
     */
    @Override
    abstract protected Void doInBackground(Object... params);

    /**
     * {@inheritDoc}
     *
     * {@code ProgressDialog}を更新する処理を実装する。
     */
    @Override
    protected void onProgressUpdate(Integer... values) {
        dialog.setProgress(values[0]);
    }

    /**
     * {@inheritDoc}
     *
     * ダイアログを閉じる処理を実装している。
     */
    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        dialog.dismiss();
    }

}
