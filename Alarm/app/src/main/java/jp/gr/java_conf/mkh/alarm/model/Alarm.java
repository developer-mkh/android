package jp.gr.java_conf.mkh.alarm.model;

import java.io.Serializable;
import java.util.List;

import jp.gr.java_conf.mkh.alarm.receiver.AlarmReceiver.PlayMode;

/**
 * アラーム1件を表す。
 *
 * @author mkh
 *
 */
public class Alarm implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -5928197058643713922L;

    private List<EnableCode> enableCode;
    private List<Integer> groupIdList;
    private int hour;
    private int min;
    private boolean isEnable;
    private boolean isDelete;
    private PlayMode mode;
    private int resId;
    private String musicFilePath;
    private int vol;
    private boolean forcePlay;
    private boolean vibrate;

    /**
     * コンストラクタ。<br>
     * 削除フラグはfalseに設定する。
     *
     * @param enableCode
     *            アラームを有効にする種類
     * @param groupId
     *            グループID
     * @param hour
     *            セットする時間
     * @param min
     *            セットする分
     * @param isEnable
     *            アラームが有効か
     * @param mode
     *            モード(添付ファイル音源か、ローカルファイルを再生するか)
     * @param resId
     *            添付ファイル音源を使用する場合のリソースID
     * @param musicFilePath
     *            ローカルファイルを再生する場合のファイルパス
     * @param vol
     *            ボリューム(%)
     * @param forcePlay
     *            マナーモードでも音を出す場合はtrue
     * @param vibrate
     *            バイブを作動させるときtrue
     */
    public Alarm(List<EnableCode> enableCode, List<Integer> groupId, int hour, int min, boolean isEnable,
            PlayMode mode, int resId, String musicFilePath, int vol, boolean forcePlay, boolean vibrate) {
        this.enableCode = enableCode;
        this.groupIdList = groupId;
        this.hour = hour;
        this.min = min;
        this.isEnable = isEnable;
        isDelete = false;
        this.mode = mode;
        this.resId = resId;
        this.musicFilePath = musicFilePath;
        this.vol = vol;
        this.forcePlay = forcePlay;
        this.vibrate = vibrate;
    }

    /**
     * コンストラクタ。<br>
     * 引数以外の項目の値は保証しない。
     *
     * @param hour
     *            セットする時間
     * @param min
     *            セットする分
     */
    public Alarm(int hour, int min) {
        this.hour = hour;
        this.min = min;
    }

    public List<EnableCode> getEnableCode() {
        return enableCode;
    }

    public List<Integer> getGroupIdList() {
        return groupIdList;
    }

    public void setGroupIdList(List<Integer> groupIdList) {
        this.groupIdList = groupIdList;
    }

    public int getHour() {
        return hour;
    }

    public int getMin() {
        return min;
    }

    public boolean isEnable() {
        return isEnable;
    }

    public void setIsEnable(boolean isEnable) {
        this.isEnable = isEnable;
    }

    public boolean isDelete() {
        return isDelete;
    }

    public void setIsDelete(boolean isDelete) {
        this.isDelete = isDelete;
    }

    /**
     * @return mode
     */
    public PlayMode getMode() {
        return mode;
    }

    /**
     * @return resId
     */
    public int getResId() {
        return resId;
    }

    /**
     * @return musicFilePath
     */
    public String getMusicFilePath() {
        return musicFilePath;
    }

    /**
     *
     * @return ボリューム
     */
    public int getVol() {
        return vol;
    }

    /**
     * @return forcePlay
     */
    public boolean isForcePlay() {
        return forcePlay;
    }

    /**
     * @return vibrate
     */
    public boolean isVibrate() {
        return vibrate;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getHour()).append(":").append(getMin() + "\n");
        if (enableCode != null) {
            sb.append(enableCode.toString());
        }
        if (groupIdList != null) {
            sb.append("groupId:");
            for (int i : groupIdList) {
                sb.append(i).append(", ");
            }
        }
        sb.append(", ").append(" ").append(isEnable).append(" ").append(mode).append(" ").append(resId).append(" ")
                .append(musicFilePath).append(" vol:").append(vol);

        return sb.toString();
    }

    /**
     * アラームをセットする種類。曜日、平日など。
     *
     * @author mkh
     *
     */
    public enum EnableCode {
        SUN, MON, TUE, WED, THU, FRI, SAT, EveryDay, Weekday
    };

}
