/**
 *
 */
package jp.gr.java_conf.mkh.alarm.ui;

import java.util.EventListener;

/**
 * グループ単位でアラームの有効、無効を切り替えたときに呼ばれるメソッドを定義したインターフェース。
 * @author mkh
 *
 */
public interface ChangeAlarmEnable extends EventListener {

    /**
     * グループ単位でアラームの有効、無効を切り替えたときに呼ばれる。
     * @param groupName グループ名
     * @param isChecked チェックしたときtrue
     */
    public void onChangeAlarmEnable(String groupName, boolean isChecked);
}
