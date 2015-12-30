package jp.gr.java_conf.mkh.tesConvertor2.converter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import jp.gr.java_conf.mkh.tesConvertor2.model.GpsData;
import jp.gr.java_conf.mkh.tesConvertor2.ui.DataSizeGettable;

/**
 * TESフォーマットを変換するクラス。
 *
 * @author mkh
 *
 */
public class BasicConverter implements Convertor, DataSizeGettable {

    /** 一度に読み込むバイト数 */
    private static final int READ_SIZE = 16;

    /** 処理済みバイト数 */
    private long processedDataSize;

    /**
     * {@inheritDoc}
     */
    public long getProcessedDataSize (){
        return processedDataSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() {
        processedDataSize = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getAllDataSize() {
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    public List<GpsData> convert(InputStream is) throws FileNotFoundException, IOException {
        GpsData gpsData = null;
        GpsData lastData = null;
        Date date = null;


        List<GpsData> list = new ArrayList<GpsData>();
        GregorianCalendar cal = (GregorianCalendar) GregorianCalendar.getInstance();
        byte[] data = new byte[READ_SIZE];

        while (is.read(data) != -1) {
            if (lastData != null) {
                cal.set(lastData.getYear(), lastData.getMonth() - 1, lastData.getDay(), lastData.getHour(),
                        lastData.getMin(), lastData.getSec());
                cal.set(Calendar.MILLISECOND, 0);
                date = cal.getTime();
                gpsData = new GpsData(data, lastData.getLongitude(), lastData.getLatitude(), date);
            } else {
                gpsData = new GpsData(data, null, null, null);
            }
            lastData = gpsData;
            list.add(gpsData);
            processedDataSize = processedDataSize + READ_SIZE;
        }
        return list;

    }
}
