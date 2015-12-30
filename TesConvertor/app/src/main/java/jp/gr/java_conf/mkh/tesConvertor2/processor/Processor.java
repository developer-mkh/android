package jp.gr.java_conf.mkh.tesConvertor2.processor;

import java.util.List;

import jp.gr.java_conf.mkh.tesConvertor2.converter.Convertor;
import jp.gr.java_conf.mkh.tesConvertor2.model.GpsData;
import jp.gr.java_conf.mkh.tesConvertor2.writer.Writer;

/**
 * TESフォーマットからの変換を行うクラスが実装すべきインターフェース。
 *
 * @author mkh
 *
 */
public interface Processor {
    /**
     * 変換を実行する。
     *
     * @param convertor 変換機能を提供するクラス。
     * @return 変換結果のリスト
     * @throws Throwable 例外
     */
    public List<GpsData> convert(Convertor convertor) throws Throwable;

    /**
     * 変換結果の書き込みを実行する。
     *
     * @param list 書き込み対象
     * @param writer 書き込み機能を提供するクラス
     * @throws Throwable 例外
     */
    public void write(List<GpsData> list, Writer writer) throws Throwable;
}
