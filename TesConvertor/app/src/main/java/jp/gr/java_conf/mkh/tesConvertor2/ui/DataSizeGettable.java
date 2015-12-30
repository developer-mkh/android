package jp.gr.java_conf.mkh.tesConvertor2.ui;

/**
 * 処理対象データのサイズを取得できるクラスが実装すべきインターフェース。
 * @author mkh
 *
 */
public interface DataSizeGettable {
    /**
     * サイズを初期化する。
     */
    public void init();

    /**
     * 全データサイズを取得する。
     *
     * @return 全データサイズ
     */
    public long getAllDataSize();

    /**
     * 処理済みデータサイズを取得する。
     *
     * @return 処理済みデータサイズ
     */
    public long getProcessedDataSize();
}
