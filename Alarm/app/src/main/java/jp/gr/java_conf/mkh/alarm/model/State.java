package jp.gr.java_conf.mkh.alarm.model;

import java.util.List;

import android.media.MediaPlayer;

/**
 * アラームを鳴らす前、鳴らすときの状態を保持する。
 * @author mkh
 *
 */
public class State {

    private MediaPlayer mp;
    private int vol;
    private int ringerMode;
    private List<Integer> groupIdList;

    public State() {

    }

    /**
     * MediaPlayerを取得する。
     *
     * @return mp
     */
    public MediaPlayer getMp() {
        return mp;
    }

    /**
     * MediaPlayerを取得する。
     * @param mp セットする MediaPlayer
     */
    public void setMp(MediaPlayer mp) {
        this.mp = mp;
    }

    /**
     * ボリュームを取得する。
     *
     * @return vol ボリューム
     */
    public int getVol() {
        return vol;
    }

    /**
     * ボリュームを設定する。
     * @param vol セットするボリューム
     */
    public void setVol(int vol) {
        this.vol = vol;
    }

    /**
     * マナーモードの状態を取得する。
     *
     * @return ringerMode マナーモードの状態
     */
    public int getRingerMode() {
        return ringerMode;
    }

    /**
     * マナーモードの状態を設定する。
     *
     * @param ringerMode セットするマナーモードの状態
     */
    public void setRingerMode(int ringerMode) {
        this.ringerMode = ringerMode;
    }

    /**
     * グループIDのリストを設定する。
     * @return groupIdList グループIDのリスト
     */
    public List<Integer> getGroupIdList() {
        return groupIdList;
    }

    /**
     * グループIDのリストを設定する。
     *
     * @param groupIdList セットするグループIDのリスト
     */
    public void setGroupIdList(List<Integer> groupIdList) {
        this.groupIdList = groupIdList;
    }

}
