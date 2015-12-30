package jp.gr.java_conf.mkh.tesConvertor2.writer;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import jp.gr.java_conf.mkh.tesConvertor2.model.GpsData;
import jp.gr.java_conf.mkh.tesConvertor2.ui.DataSizeGettable;


import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * GPX形式に出力する{@code Writer}の実装。
 * @author mkh
 *
 */
public class GpxWriter implements Writer, DataSizeGettable {

    /** XMLドキュメント */
    private Document document;
    /** ソース構造を結果構造に変換する */
    private Transformer transformer;
    /** ソース構造 */
    private DOMSource source;
    /** XMLのルート */
    private Element rootElement;

    /** 処理ステータス */
    private Status status;

    /** 処理済みデータ数 */
    private double numOfProcessed;
    /** 全処理対象データ数 */
    private double numOfAllData;

    /**
     * コンストラクタ。
     */
    public GpxWriter() {
        status = Status.INIT;
        numOfProcessed = 0;
        numOfAllData = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() {
        status = Status.INIT;
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
    @Override
    public long getProcessedDataSize() {
        switch (status) {
        case INIT:
            return 0;
        case HEADER:
            return 20;
        case BODY:
            return (long) (numOfProcessed / numOfAllData * 100 * 0.4) + 20;
        case WRITE:
            return 100;
        }
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    public void makeHeader(Map<String, Object> data) throws ParserConfigurationException,
            TransformerConfigurationException, TransformerFactoryConfigurationError {

        if (status == Status.INIT) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.newDocument();

            rootElement = document.createElement("gpx");
            rootElement.setAttribute("version", "1.1");
            rootElement.setAttribute("creator", "TesTpGpx");
            rootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            rootElement.setAttribute("xmlns", "http://www.topografix.com/GPX/1/1");
            rootElement
                    .setAttribute(
                            "xsi:schemaLocation",
                            "http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd http://www.topografix.com/GPX/gpx_overlay/0/3 http://www.topografix.com/GPX/gpx_overlay/0/3/gpx_overlay.xsd http://www.topografix.com/GPX/gpx_modified/0/1 http://www.topografix.com/GPX/gpx_modified/0/1/gpx_modified.xsd");
            document.appendChild(rootElement);

            transformer = TransformerFactory.newInstance().newTransformer();
            source = new DOMSource(document);

            Properties outFormat = new Properties();
            outFormat.setProperty(OutputKeys.INDENT, "yes");
            outFormat.setProperty(OutputKeys.METHOD, "xml");
            outFormat.setProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            outFormat.setProperty(OutputKeys.VERSION, "1.0");
            outFormat.setProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperties(outFormat);

            status = Status.HEADER;

        }
    }

    /**
     * {@inheritDoc}
     */
    public void makeBody(List<GpsData> list, Map<String, Object> minMax) throws TransformerException {

        numOfAllData = list.size();

        GpsData max = (GpsData) minMax.get("max");
        GpsData min = (GpsData) minMax.get("min");

        Element metadata = makeMetaData(max, min);

        Element trk = document.createElement("trk");

        Element name = document.createElement("name");
        name.appendChild(document.createTextNode("Track 001"));

        Element desc = document.createElement("desc");
        desc.appendChild(document.createTextNode("Track#1"));

        Element trkseg = document.createElement("trkseg");

        List<Element> trkpt = makeTrkpt(list);
        for (Element ele : trkpt) {
            trkseg.appendChild(ele);
        }

        trk.appendChild(name);
        trk.appendChild(desc);
        trk.appendChild(trkseg);
        rootElement.appendChild(metadata);
        rootElement.appendChild(trk);

        status = Status.BODY;
    }

    /**
     * {@inheritDoc}
     */
    public void write(OutputStream out) throws TransformerException {
        StreamResult result = new StreamResult(out);
        transformer.transform(source, result);

        status = Status.WRITE;
    }

    /**
     * metaタグの部分を作成する。
     *
     * @param max 緯度、経度の最大値を持つ{@code GpsData}
     * @param min 緯度、経度の最小値値を持つ{@code GpsData}
     * @return metaタグ
     */
    private Element makeMetaData(GpsData max, GpsData min) {
        Element bounds = document.createElement("bounds");
        bounds.setAttribute("maxlat", max.getLatitude().toString());
        bounds.setAttribute("maxlon", max.getLongitude().toString());
        bounds.setAttribute("minlat", min.getLatitude().toString());
        bounds.setAttribute("minlon", min.getLongitude().toString());

        Element metadata = document.createElement("metadata");
        metadata.appendChild(bounds);

        return metadata;
    }

    /**
     * Trkptタグを作成する。
     *
     * @param list 作成元となる{@code GpsData}のリスト
     * @return 作成したTrkptタグのリスト
     */
    private List<Element> makeTrkpt(List<GpsData> list) {
        List<Element> ret = new ArrayList<Element>();
        Element trkpt;
        Element ele;
        Element time;
        Element desc;

        StringBuilder sb = new StringBuilder();
        numOfProcessed = 0;
        for (GpsData data : list) {
            sb.setLength(0);

            trkpt = document.createElement("trkpt");
            trkpt.setAttribute("lat", data.getLatitude().toString());
            trkpt.setAttribute("lon", data.getLongitude().toString());

            ele = document.createElement("ele");
            ele.appendChild(document.createTextNode(String.valueOf(data.getAltitude())));

            time = document.createElement("time");
            String month = zeroPadding(data.getMonth());
            String day = zeroPadding(data.getDay());
            String hour = zeroPadding(data.getHour());
            String min = zeroPadding(data.getMin());
            String sec = zeroPadding(data.getSec());
            String timeData = sb.append(data.getYear()).append("-").append(month).append("-").append(day).append("T")
                    .append(hour).append(":").append(min).append(":").append(sec).append("Z").toString();
            time.appendChild(document.createTextNode(timeData));

            sb.setLength(0);
            desc = document.createElement("desc");
            String descData = sb.append("Lat.=").append(data.getLatitude()).append(", Long.=")
                    .append(data.getLongitude()).append(", Alt.=").append(data.getAltitude()).append("m, Speed=")
                    .append(data.getVelocity()).append("Km/h, Course=").append(data.getAltitude()).append("deg.")
                    .toString();
            desc.appendChild(document.createTextNode(descData));

            trkpt.appendChild(ele);
            trkpt.appendChild(time);
            trkpt.appendChild(desc);

            ret.add(trkpt);
            numOfProcessed++;
        }
        return ret;
    }

    /**
     * 1ケタの整数に対して左0パディングを行い、2ケタにする。
     *
     * 例<br>
     * <ul>
     *   <li>入力:1のとき出力:01
     *   <li>入力:10のとき出力:10
     * </ul>
     * 3ケタ以上の整数が入力された場合は何もしない。
     *
     * @param param 処理対象整数
     * @return パディングした文字列
     */
    private String zeroPadding(int param) {
        StringBuilder sb = new StringBuilder();
        if (param < 10) {
            sb.append("0").append(param);

        } else {
            sb.append(param);
        }

        return sb.toString();
    }
}
