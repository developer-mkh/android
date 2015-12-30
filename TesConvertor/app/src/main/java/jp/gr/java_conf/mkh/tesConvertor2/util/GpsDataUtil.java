package jp.gr.java_conf.mkh.tesConvertor2.util;

import java.math.BigDecimal;
import java.util.List;

import jp.gr.java_conf.mkh.tesConvertor2.model.GpsData;

/**
 * {@code GpsData}に関するユーティリティクラス。
 *
 * @author mkh
 *
 */
public final class GpsDataUtil {

    /**
     * 経度、緯度の最大値を取得する。
     *
     * @param list 取得対象のリスト
     * @return 緯度、経度の最大値が設定された{@code GpsData}クラス。その他のプロパティは不定。
     */
    public static GpsData getMax(List<GpsData> list) {
        BigDecimal maxLon = new BigDecimal(0);
        BigDecimal maxLat = new BigDecimal(0);

        for(GpsData data : list) {
            maxLon = maxLon.max(data.getLongitude());
            maxLat = maxLat.max(data.getLatitude());
        }

        GpsData ret = new GpsData();
        ret.setLongitude(maxLon);
        ret.setLatitude(maxLat);
        return ret;
    }

    /**
     * 経度、緯度の最小値を取得する。
     *
     * @param list 取得対象のリスト
     * @return 緯度、経度の最小値が設定された{@code GpsData}クラス。その他のプロパティは不定。
     */
    public static GpsData getMin(List<GpsData> list) {
        BigDecimal minLon = new BigDecimal(180);
        BigDecimal minLat = new BigDecimal(90);

        for(GpsData data : list) {
            minLon = minLon.min(data.getLongitude());
            minLat = minLat.min(data.getLatitude());
        }

        GpsData ret = new GpsData();
        ret.setLongitude(minLon);
        ret.setLatitude(minLat);
        return ret;
    }
}
