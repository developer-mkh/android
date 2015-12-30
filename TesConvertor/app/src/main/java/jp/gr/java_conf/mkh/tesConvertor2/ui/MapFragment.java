package jp.gr.java_conf.mkh.tesConvertor2.ui;

import jp.gr.java_conf.mkh.tesConvertor2.R;
import jp.gr.java_conf.mkh.tesConvertor2.model.GpsData;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.maps.MapView;

/**
 * {@link GpsData}からgoogle Mapを表示するフラグメント。
 *
 * @author mkh
 *
 */
public class MapFragment extends Fragment {

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.map_view, container, false);
        ((MapView)((LinearLayout) v).getChildAt(0)).setBuiltInZoomControls(true);
        return v;
    }


}
