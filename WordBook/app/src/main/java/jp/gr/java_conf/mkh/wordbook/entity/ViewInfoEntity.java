package jp.gr.java_conf.mkh.wordbook.entity;

import android.content.ContentValues;

/**
 * ViewInfoテーブルの1レコードを表すエンティティクラス
 */
public class ViewInfoEntity {

    /** 画面の向き */
    private int orientation;
    /** 選択されたアイテムの位置 */
    private int selectedItemPosition;
    /** 先頭に表示しているアイテムの位置 */
    private int topPosition;
    /** 表示しているアイテムの数 */
    private int showItemNum;

    /**
     * 画面の向きを返す。
     * @return 画面の向き(Configurationクラス参照)
     */
    public int getOrientation() {
        return orientation;
    }

    /**
     * 画面の向きを設定する。
     * @param orientation 画面の向き(Configurationクラス参照)
     */
    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    /**
     * 選択しているアイテムの位置を返す。
     * @return 選択しているアイテムの位置
     */
    public int getSelectedItemPosition() {
        return selectedItemPosition;
    }

    /**
     * 選択しているアイテムの位置を設定する。
     * @param selectedItemPosition 選択しているアイテムの位置
     */
    public void setSelectedItemPosition(int selectedItemPosition) {
        this.selectedItemPosition = selectedItemPosition;
    }

    /**
     * 先頭に表示しているアイテムの位置を返す。
     * @return 先頭に表示しているアイテムの位置
     */
    public int getTopPosition() {
        return topPosition;
    }

    /**
     * 先頭に表示しているアイテムの位置を設定する。
     * @param topPosition 先頭に表示しているアイテムの位置
     */
    public void setTopPosition(int topPosition) {
        this.topPosition = topPosition;
    }

    /**
     * 画面に表示しているアイテムの数を返す。
     * @return 画面に表示しているアイテム数
     */
    public int getShowItemNum() {
        return showItemNum;
    }

    /**
     * 画面に表示しているアイテムの数を設定する。
     * @param showItemNum 画面に表示しているアイテム数
     */
    public void setShowItemNum(int showItemNum) {
        this.showItemNum = showItemNum;
    }

    /**
     * 保持している値をContentValuesに変換する。
     * @return 保持している値
     */
    public ContentValues getContentValues() {
        ContentValues value = new ContentValues();
        value.put("orientation", orientation);
        value.put("selected_item_position", selectedItemPosition);
        value.put("top_position", topPosition);
        value.put("show_item_num", showItemNum);

        return  value;
    }

}
