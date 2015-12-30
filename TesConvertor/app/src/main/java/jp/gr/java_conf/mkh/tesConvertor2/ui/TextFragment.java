package jp.gr.java_conf.mkh.tesConvertor2.ui;

import java.util.List;

import jp.gr.java_conf.mkh.tesConvertor2.R;
import jp.gr.java_conf.mkh.tesConvertor2.model.GpsData;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * {@link GpsData}からテキストデータを表示するフラグメント。
 * @author mkh
 *
 */
public class TextFragment extends Fragment {

    /** データ表示領域 */
    private TextView textView;

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

        View v = inflater.inflate(R.layout.text_view, container, false);
        textView = (TextView) v.findViewById(R.id.textView3);
        return v;
    }

    /**
     * データを表示する。
     * <br>
     * それまでの表示内容はクリアする。
     * @param data 表示するデータのリスト
     */
    public void setText(List<GpsData> data) {
        textView.setText("");
        for (GpsData d : data) {
            textView.append(d.toString() + "\n");
        }

    }
}
