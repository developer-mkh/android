package jp.gr.java_conf.mkh.alarm.model;



/**
 * booleanとStringを持つvalueクラス。 <br>
 * alarm_rowレイアウトで使うことを想定。
 *
 * @author mkh
 *
 */
public class ListItem {

    private boolean isEnableCheckBox;
    private boolean isEnableTextView;
    private boolean isChecked;
    private String text;

    public ListItem() {

    }

    public ListItem(boolean isChecked, String text, boolean isEnableCheckBox, boolean isEnableTextView) {
        this.isChecked = isChecked;
        this.text = text;
        this.isEnableCheckBox = isEnableCheckBox;
        this.isEnableTextView = isEnableTextView;
    }

    /**
     * @return isChecked
     */
    public boolean isChecked() {
        return isChecked;
    }

    /**
     * @param isChecked
     *            セットする isChecked
     */
    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    /**
     * @return text
     */
    public String getText() {
        return text;
    }

    /**
     * @param text
     *            セットする text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @return isEnableCheckBox
     */
    public boolean isEnableCheckBox() {
        return isEnableCheckBox;
    }

    /**
     * @param isEnableCheckBox セットする isEnableCheckBox
     */
    public void setEnableCheckBox(boolean isEnableCheckBox) {
        this.isEnableCheckBox = isEnableCheckBox;
    }

    /**
     * @return isEnableTextView
     */
    public boolean isEnableTextView() {
        return isEnableTextView;
    }

    /**
     * @param isEnableTextView セットする isEnableTextView
     */
    public void setEnableTextView(boolean isEnableTextView) {
        this.isEnableTextView = isEnableTextView;
    }


}
