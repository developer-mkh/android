package jp.gr.java_conf.mkh.tesConvertor2.processor;

import java.math.BigDecimal;
import java.util.List;

import jp.gr.java_conf.mkh.tesConvertor2.converter.Convertor;
import jp.gr.java_conf.mkh.tesConvertor2.model.GpsData;
import jp.gr.java_conf.mkh.tesConvertor2.util.GpsDataUtil;
import jp.gr.java_conf.mkh.tesConvertor2.writer.AndroidMapsWriter;
import jp.gr.java_conf.mkh.tesConvertor2.writer.Writer;
import android.app.Fragment;
import android.widget.LinearLayout;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

/**
 * GoogleMapに出力する変換実施クラス。
 * <br/>
 * TESフォーマットからの変換は他の{@link Processor}実装クラスに委譲することとし、本クラスでは実装しない。
 * @author mkh
 *
 */
public class AndroidMapsProcessor implements Processor {

    /** フラグメント */
    private Fragment mapFragment;

    /**
     * フラグメントを設定する。
     *
     * @param mapFragment フラグメント
     */
    public void setMapFragment(Fragment mapFragment) {
        this.mapFragment = mapFragment;
    }

    /**
     * 常にnullを返す。
     */
    @Override
    public List<GpsData> convert(Convertor convertor) throws Throwable {
        return null;
    }

    /**
     *
     */
    @Override
    public void write(List<GpsData> list, Writer writer) throws Throwable {
        AndroidMapsWriter androidMapsWriter = (AndroidMapsWriter) writer;
        androidMapsWriter.setList(list);

        MapView mapView = (MapView) ((LinearLayout) mapFragment.getView()).getChildAt(0);
        List<Overlay> overLaylist = mapView.getOverlays();
        overLaylist.clear();
        overLaylist.add(androidMapsWriter);

        MapController ctrl = mapView.getController();
        BigDecimal multyplier = BigDecimal.valueOf(1e6);

        GpsData max = GpsDataUtil.getMax(list);
        GpsData min = GpsDataUtil.getMin(list);
        int latSpan = (int) Math.abs(max.getLatitude().subtract(min.getLatitude()).multiply(multyplier).doubleValue());
        int lonSpan = (int) Math.abs(max.getLongitude().subtract(min.getLongitude()).multiply(multyplier).doubleValue());
        int latMin = min.getLatitude().multiply(multyplier).intValue();
        int lonMin = min.getLongitude().multiply(multyplier).intValue();

        ctrl.animateTo(new GeoPoint(latMin + latSpan / 2, lonMin + lonSpan / 2));
        ctrl.zoomToSpan(latSpan, lonSpan);

        mapView.invalidate();
    }

}
