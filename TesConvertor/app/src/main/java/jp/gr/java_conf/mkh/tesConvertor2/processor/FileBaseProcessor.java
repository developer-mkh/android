package jp.gr.java_conf.mkh.tesConvertor2.processor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.gr.java_conf.mkh.tesConvertor2.converter.Convertor;
import jp.gr.java_conf.mkh.tesConvertor2.model.GpsData;
import jp.gr.java_conf.mkh.tesConvertor2.ui.DataSizeGettable;
import jp.gr.java_conf.mkh.tesConvertor2.util.GpsDataUtil;
import jp.gr.java_conf.mkh.tesConvertor2.writer.Writer;

/**
 * ファイルを対象とする変換実施クラス。
 * 変換対象ファイルを読み込み、ファイルに書き出す。
 *
 * @author mkh
 *
 */
public class FileBaseProcessor implements Processor, DataSizeGettable {

    /** 入力ファイルのフルパス */
    private String inputFilePath;
    /** 出力ファイルのフルパス */
    private String outputFilePath;

    /**
     * 入力ファイルのフルパスを取得する。
     *
     * @return 入力ファイルのフルパス
     */
    public String getInputFilePath() {
        return inputFilePath;
    }

    /**
     * 入力ファイルのフルパスを設定する。
     *
     * @param inputFilePath 入力ファイルのフルパス
     */
    public void setInputFilePath(String inputFilePath) {
        this.inputFilePath = inputFilePath;
    }

    /**
     * 出力ファイルのフルパスを取得する。
     *
     * @return 出力ファイルのフルパス
     */
    public String getOutputFilePath() {
        return outputFilePath;
    }

    /**
     * 出力ファイルのフルパスを設定する。
     *
     * @param outputFilePath 出力ファイルのフルパス
     */
    public void setOutputFilePath(String outputFilePath) {
        this.outputFilePath = outputFilePath;
    }

    /**
     * 設定した出力ファイルが存在するか確認する。
     *
     * @return 存在する場合{@code true}
     */
    public boolean isExists() {
        return new File(outputFilePath).exists();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getAllDataSize() {
        return new File(inputFilePath).length();
    }

    /**
     * {@inheritDoc}
     *
     * 本実装では、常にー1を返す。
     */
    @Override
    public long getProcessedDataSize() {
        return -1;
    }

    /**
     * {@inheritDoc}
     *
     * @throws FileNotFoundException 入力ファイルが見つからない場合
     * @throws IOException 入出力エラーが発生した場合
     */
    public List<GpsData> convert(Convertor convertor) throws FileNotFoundException, IOException {

        List<GpsData> list = null;

        FileInputStream fis = new FileInputStream(new File(inputFilePath));
        BufferedInputStream bis = new BufferedInputStream(fis);
        try {
            list = convertor.convert(bis);
        } finally {
            bis.close();
            fis.close();
        }

        return list;
    }

    /**
     * [{@inheritDoc}
     */
    public void write(List<GpsData> list, Writer writer) throws Throwable {

        Map<String, Object> param = new HashMap<String, Object>();
        param.put("min", GpsDataUtil.getMin(list));
        param.put("max", GpsDataUtil.getMax(list));
        writer.makeHeader(null);
        writer.makeBody(list, param);
        FileOutputStream fos = new FileOutputStream(new File(outputFilePath));
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        try {
            writer.write(bos);
        } finally {
            bos.close();
            fos.close();
        }
    }
}
