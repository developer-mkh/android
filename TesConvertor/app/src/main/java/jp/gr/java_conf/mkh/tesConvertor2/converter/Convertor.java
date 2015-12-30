package jp.gr.java_conf.mkh.tesConvertor2.converter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import jp.gr.java_conf.mkh.tesConvertor2.model.GpsData;

/**
 * TESフォーマットを変換するクラスが実装すべきインターフェース。
 * @author mkh
 *
 */
public interface Convertor {

    /**
     * TESフォーマットを変換する。
     *
     * @param is TESフォーマットファイルを読み込むインプットストリーム
     * @return 変換結果のリスト
     * @throws FileNotFoundException ファイルが見つからなかった場合
     * @throws IOException 入出力エラーが発生した場合
     */
    public List<GpsData> convert (InputStream is) throws FileNotFoundException, IOException;
}
