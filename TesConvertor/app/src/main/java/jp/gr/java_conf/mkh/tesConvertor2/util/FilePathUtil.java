package jp.gr.java_conf.mkh.tesConvertor2.util;

import android.os.Environment;

/**
 * ファイルパスに関するユーティリティ
 *
 * @author mkh
 *
 */
public class FilePathUtil {

    /**
     * ファイルパスのディレクトリを取得する。SDカードの有無によって返却するパスを決定する。<br>
     * <ul>
     * <li>SDカードがある場合は、SDカード上のディレクトリを返却する。
     * <li>SDカードがない場合は、空文字を返却する。
     * </ul>
     *
     * @return ディレクトリのフルパス
     */
    public static String getFileBasePath(Class<?> claszz) {
        String sdCardStatus = Environment.getExternalStorageState();
        String ret = "";

        if (Environment.MEDIA_MOUNTED.equalsIgnoreCase(sdCardStatus)) {
            ret = getFileBasePathForSd(claszz);
        }

        return ret;
    }

    /**
     * SDカードがある場合のファイルパスのディレクトリを取得する。<br>
     *
     * @return ディレクトリのフルパス
     */
    public static String getFileBasePathForSd(Class<?> claszz) {
        return Environment.getExternalStorageDirectory().getPath() + "/" + claszz.getPackage().getName() + "/";
    }

}
