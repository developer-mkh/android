package jp.gr.java_conf.mkh.tesConvertor2.model;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * TESフォーマットのデータ1件を表す。
 *
 * @author mkh
 *
 */
public class GpsData {

    /** フラグ */
    private Flag flag;
    /** 高度 */
    private int altitude;
    /** 経度 */
    private BigDecimal longitude;
    /** 緯度 */
    private BigDecimal latitude;
    /** データを記録した年 */
    private int year;
    /** データを記録した月 */
    private byte month;
    /** データを記録した日 */
    private byte day;
    /** データを記録した時間 */
    private byte hour;
    /** データを記録した分 */
    private byte min;
    /** データを記録した秒 */
    private byte sec;
    /** 時速 */
    private double velocity;
    /** 進行方向 */
    private double ang;
    /** 地球の半径 */
    private static final double A = 6378137;

    /**
     * デフォルトコンストラクタ
     */
    public GpsData ()
    {}

    /**
     * バイト配列を受け取り、各プロパティに設定する。
     * @param data TESフォーマットの1行
     * @param lon 速度、方位角を求める時、基準となる場所の経度。計算しないときはnull
     * @param lat 速度、方位角を求める時、基準となる場所の緯度。計算しないときはnull
     * @param fromDate 速度を求める時、基準となる時刻。計算しないときはnull
     */
    public GpsData (byte[] data, BigDecimal lon, BigDecimal lat, Date fromDate) {
        if (data.length != 16) {
            throw new IllegalArgumentException("data length must be 16.");
        }

        int flag = 0;
        flag = (data[1] << 8) + data[0];
        if (flag == 1) {
            this.flag = Flag.SPLIT_MARK;
        } else if (flag == 2) {
            this.flag = Flag.INTEREST_POINT;
        } else {
            this.flag = Flag.TRACK_POINT;
        }

        int dataTime = 0;
        dataTime = data[2] & 0xFF;
        dataTime = dataTime | (data[3] << 8) & 0xFFFF;
        dataTime = dataTime | (data[4] << 16) & 0xFFFFFF;
        dataTime = dataTime | (data[5] << 24);
        sec = Integer.valueOf(dataTime & 63).byteValue();
        min = Integer.valueOf((dataTime >> 6) & 63).byteValue();
        hour = Integer.valueOf((dataTime >> 12) & 31).byteValue();
        day = Integer.valueOf((dataTime >> 17) & 31).byteValue();
        month = Integer.valueOf((dataTime >> 22) & 15).byteValue();
        year = Integer.valueOf((dataTime >> 26) & 31).byteValue() + 2000;

        int tmpLatitude = 0;
        tmpLatitude = data[6] & 0xFF;
        tmpLatitude = tmpLatitude | (data[7] << 8) & 0xFFFF;
        tmpLatitude = tmpLatitude | (data[8] << 16) & 0xFFFFFF;
        tmpLatitude = tmpLatitude | (data[9] << 24);
        latitude = new BigDecimal(tmpLatitude);
        latitude = latitude.divide(BigDecimal.valueOf(1.0e7));

        int tmpLongitude = 0;
        tmpLongitude = data[10]  & 0xFF;
        tmpLongitude = tmpLongitude | (data[11] << 8)  & 0xFFFF;
        tmpLongitude = tmpLongitude | (data[12] << 16)  & 0xFFFFFF;
        tmpLongitude = tmpLongitude | (data[13] << 24);
        longitude = new BigDecimal(tmpLongitude);
        longitude = longitude.divide(BigDecimal.valueOf(1.0e7));

        altitude = data[14];
        altitude = altitude + (data[15] << 8);

        if (lon ==null || lat == null || fromDate == null) {
            velocity = 0;
            ang = 0;
        } else {
            double toRad = Math.PI / 180.0;
            double deltaLon = lon.subtract(longitude).doubleValue() * toRad;
            double deltaLat = lat.subtract(latitude).doubleValue() * toRad;
            double deltaX = A * deltaLon * Math.cos(latitude.doubleValue() * toRad) ;
            double deltaY = A * deltaLat;

            double dist = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
            GregorianCalendar cal = (GregorianCalendar)GregorianCalendar.getInstance();
            cal.set(year, month - 1, day, hour, min, sec);
            cal.set(Calendar.MILLISECOND, 0);
            Date date = cal.getTime();
            this.velocity = dist / ((double) (date.getTime() - fromDate.getTime())) * 60.0 * 60.0;
            double tmpAng = Math.atan2(deltaY, deltaX) / toRad;
            if (deltaX >= 0 && deltaY >0) {
                this.ang = 90 - tmpAng;
            } else if (deltaX < 0 && deltaY >= 0) {
                this.ang = 360 - tmpAng;
            } else if (deltaX <= 0 && deltaY < 0) {
                this.ang = 270 - (180 + tmpAng);
            } else {
                this.ang = 180 - (90 + tmpAng);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(year).append("/").append(month).append("/").append(day).append(" ")
            .append(hour).append(":").append(min).append(".").append(sec).append(" ")
            .append("latitude=").append(latitude).append(" longitude=").append(longitude).append(" altitude=").append(altitude)
            .append(" v=").append(velocity).append(" cource=").append(ang);

        return sb.toString();
    }

    /**
     * フラグを取得する。
     *
     * @return flag フラグ
     */
    public Flag getFlag() {
        return flag;
    }

    /**
     * フラグを設定する。
     *
     * @param flag セットするフラグ
     */
    public void setFlag(Flag flag) {
        this.flag = flag;
    }

    /**
     * 高度を取得する。
     *
     * @return altitude 高度
     */
    public int getAltitude() {
        return altitude;
    }

    /**
     * 高度を設定する。
     *
     * @param altitude セットする高度
     */
    public void setAltitude(int altitude) {
        this.altitude = altitude;
    }

    /**
     * 経度を取得する。
     *
     * @return longitude 経度
     */
    public BigDecimal getLongitude() {
        return longitude;
    }

    /**
     * 経度を設定する。
     *
     * @param longitude セットする経度
     */
    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }


    /**
     * 緯度を取得する。
     *
     * @return latitude 緯度
     */
    public BigDecimal getLatitude() {
        return latitude;
    }

    /**
     * 緯度を設定する。
     *
     * @param latitude セットする緯度
     */
    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    /**
     * データを記録した年を取得する。
     *
     * @return year 年
     */
    public int getYear() {
        return year;
    }

    /**
     * データを記録した年を設定する。
     *
     * @param year セットする年
     */
    public void setYear(int year) {
        this.year = year;
    }

    /**
     * データを記録した月を取得する。
     *
     * @return month 月
     */
    public byte getMonth() {
        return month;
    }

    /**
     * データを記録した月を設定する。
     *
     * @param month セットする月
     */
    public void setMonth(byte month) {
        this.month = month;
    }

    /**
     * データを記録した日を取得する。
     *
     * @return day 日
     */
    public byte getDay() {
        return day;
    }

    /**
     * データを記録した日を設定する。
     *
     * @param day セットする日
     */
    public void setDay(byte day) {
        this.day = day;
    }

    /**
     * データを記録した時を取得する。
     *
     * @return hour 時
     */
    public byte getHour() {
        return hour;
    }

    /**
     * データを記録した時を設定する。
     *
     * @param hour セットする時
     */
    public void setHour(byte hour) {
        this.hour = hour;
    }

    /**
     * データを記録した分を取得する。
     *
     * @return min 分
     */
    public byte getMin() {
        return min;
    }

    /**
     * データを記録した分を設定する。
     *
     * @param min セットする分
     */
    public void setMin(byte min) {
        this.min = min;
    }

    /**
     * データを記録した秒を取得する。
     *
     * @return sec 秒
     */
    public byte getSec() {
        return sec;
    }

    /**
     * データを記録した秒を設定する。
     *
     * @param sec セットする秒
     */
    public void setSec(byte sec) {
        this.sec = sec;
    }

    /**
     * 進行方向を取得する。
     *
     * @return ang 進行方向
     */
    public double getAng() {
        return ang;
    }

    /**
     * 進行方向を設定する。
     *
     * @param ang セットする進行方向
     */
    public void setAng(double ang) {
        this.ang = ang;
    }

    /**
     * 時速を取得する。
     *
     * @return velocity 時速(m/h)
     */
    public double getVelocity() {
        return velocity;
    }

    /**
     * 時速を設定する。
     *
     * @param velocity セットする時速(m/h)
     */
    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }
}
