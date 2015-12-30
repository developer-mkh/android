package jp.gr.java_conf.mkh.tesConvertor2.writer;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import jp.gr.java_conf.mkh.tesConvertor2.model.GpsData;

/**
 * 変換結果を出力するクラスが実装すべきインターフェース。
 *
 * @author mkh
 *
 */
public interface Writer {
    /** 処理ステータス */
    public enum Status {
        /** 初期状態 */
        INIT,
        /** ヘッダ作成終了 */
        HEADER,
        /** ボディ作成終了 */
        BODY,
        /** 出力終了 */
        WRITE
    }

    /**
     * ヘッダーを作成する。
     *
     * @param data
     *            ヘッダーを作成するために必要なデータのマップ
     * @throws Exception
     *             例外
     */
    public void makeHeader(Map<String, Object> data) throws Exception;

    /**
     * ボディを作成する。
     *
     * @param list 出力対象のリスト
     * @param option オプションデータのマップ
     * @throws Exception 例外
     */
    public void makeBody(List<GpsData> list, Map<String, Object> option) throws Exception;

    /**
     * ファイルへの書き出しを実行する。
     *
     * @param out 書き出しアウトプットストリーム
     * @throws Exception 例外
     */
    public void write(OutputStream out) throws Exception;
}
