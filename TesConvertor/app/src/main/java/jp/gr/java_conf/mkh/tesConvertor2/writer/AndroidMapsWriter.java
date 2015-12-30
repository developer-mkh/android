package jp.gr.java_conf.mkh.tesConvertor2.writer;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.gr.java_conf.mkh.tesConvertor2.menu.SettingActivity;
import jp.gr.java_conf.mkh.tesConvertor2.model.GpsData;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.view.View;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

/**
 * Android Mapsに表示する。
 *
 * @author mkh
 *
 */
public class AndroidMapsWriter extends Overlay implements Writer {

    /** 軌跡データ */
    private List<GpsData> gpsDatalist;

    /**
     * 軌跡データを設定する。
     *
     * @param list
     *            軌跡データ
     */
    public void setList(List<GpsData> gpsDatalist) {
        this.gpsDatalist = gpsDatalist;
    }

    /**
     * {@inheritDoc} 本実装では何もしない。
     */
    @Override
    public void makeHeader(Map<String, Object> data) throws Exception {
        return;
    }

    /**
     * {@inheritDoc}
     *
     * 引数のlistは無視される。setListを使うこと。optionには以下のクラスを設定すること。
     * <ul>
     * <li>キー:canvas、値:描画対象の{@linkplain Canvas}
     * <li>キー:mapView、値:描画対象の{@link MapView}
     * <li>キー:shadow、値:影の描画フラグ
     * </ul>
     *
     */
    @Override
    public void makeBody(List<GpsData> list, Map<String, Object> option) throws Exception {
        Canvas canvas = (Canvas) option.get("canvas");
        MapView mapView = (MapView) option.get("mapView");
        boolean shadow = (Boolean) option.get("shadow");

        if (gpsDatalist == null || gpsDatalist.size() < 2) {
            return;
        }

        if (!shadow) {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setStyle(Paint.Style.STROKE);
            paint.setAntiAlias(true);
            paint.setStrokeWidth(3);
            paint.setColor(Color.RED);

            if (SettingActivity.isUseHardwareAccelaration(mapView.getContext())) {
                mapView.setLayerType(View.LAYER_TYPE_HARDWARE, paint);
            } else {
                mapView.setLayerType(View.LAYER_TYPE_SOFTWARE, paint);
            }

            Path path = new Path();
            Projection projection = mapView.getProjection();
            int size = gpsDatalist.size();
            Point pxStart;
            Point px;
            GeoPoint geoStart;
            GeoPoint geo;
            BigDecimal multiplier = BigDecimal.valueOf(1e6);

            int width = (int) (1.2 * canvas.getWidth());
            int height = (int) (1.2 * canvas.getHeight());
            int startX = (int) (-0.2 * width);
            int startY = (int) (-0.2 * height);
            boolean isMoved = false;
            boolean isHardwareAccelarated = canvas.isHardwareAccelerated();

            if (!isHardwareAccelarated) {
                geoStart = new GeoPoint(gpsDatalist.get(0).getLatitude().multiply(multiplier).intValue(), gpsDatalist
                        .get(0).getLongitude().multiply(multiplier).intValue());
                pxStart = projection.toPixels(geoStart, null);
                path.moveTo(pxStart.x, pxStart.y);
            }

            for (int i = 0; i < size; i++) {
                geo = new GeoPoint(gpsDatalist.get(i).getLatitude().multiply(multiplier).intValue(), gpsDatalist.get(i)
                        .getLongitude().multiply(multiplier).intValue());
                px = projection.toPixels(geo, null);

                if (isHardwareAccelarated) {
                    if ((startX <= px.x && px.x <= width) && (startY <= px.y && px.y <= height) && !isMoved) {
                        path.reset();
                        path.moveTo(px.x, px.y);
                        isMoved = true;
                    } else if ((startX <= px.x && px.x <= width) && (startY <= px.y && px.y <= height)) {
                        path.lineTo(px.x, px.y);
                    }
                } else {
                    path.lineTo(px.x, px.y);
                }
            }
            canvas.drawPath(path, paint);
        }
    }

    /**
     * {@inheritDoc} 本実装では何もしない。
     */
    @Override
    public void write(OutputStream out) throws Exception {
        return;
    }

    /**
     *
     */
    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        super.draw(canvas, mapView, shadow);

        Map<String, Object> param = new HashMap<String, Object>();
        param.put("canvas", canvas);
        param.put("mapView", mapView);
        param.put("shadow", shadow);

        try {
            makeBody(null, param);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
