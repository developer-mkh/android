package jp.gr.java_conf.mkh.tesConvertor2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import jp.gr.java_conf.mkh.tesConvertor2.R;
import jp.gr.java_conf.mkh.tesConvertor2.converter.BasicConverter;
import jp.gr.java_conf.mkh.tesConvertor2.menu.HelpActivity;
import jp.gr.java_conf.mkh.tesConvertor2.menu.SettingActivity;
import jp.gr.java_conf.mkh.tesConvertor2.model.GpsData;
import jp.gr.java_conf.mkh.tesConvertor2.processor.AndroidMapsProcessor;
import jp.gr.java_conf.mkh.tesConvertor2.processor.FileBaseProcessor;
import jp.gr.java_conf.mkh.tesConvertor2.ui.DataSizeGettable;
import jp.gr.java_conf.mkh.tesConvertor2.ui.TextFragment;
import jp.gr.java_conf.mkh.tesConvertor2.ui.UpdateProgressDialogForInput;
import jp.gr.java_conf.mkh.tesConvertor2.ui.UpdateProgressDialogForWrite;
import jp.gr.java_conf.mkh.tesConvertor2.util.FilePathUtil;
import jp.gr.java_conf.mkh.tesConvertor2.writer.AndroidMapsWriter;
import jp.gr.java_conf.mkh.tesConvertor2.writer.GpxWriter;
import jp.gr.java_conf.mkh.tesConvertor2.writer.Writer;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.maps.MapActivity;

/**
 * TESフォーマットを変換する。
 *
 * @author mkh
 *
 */
public class TesConvertorActivity extends MapActivity {

    /** 変換ボタン */
    private Button btnConvert;
    /** 入力ファイルの候補を表示するボタン */
    private Button btnInputFile;
    /** 出力ファイルの候補を表示するボタン */
    private Button btnOutputFile;
    /** 保存ボタン */
    private Button btnSave;
    /** 入力ファイルの記述欄 */
    private EditText editTextInput;
    /** 出力ファイルの記述欄 */
    private EditText editTextOutput;
    /** 表示内容切り換えボタン */
    private ToggleButton btnChangeView;
    /** テキストを表示するフラグメント */
    private Fragment textFragment;
    /** google Mapを表示するフラグメント */
    private Fragment mapFragment;

    /** 変換機能:ファイルベース */
    private FileBaseProcessor fileBaseProcessor;
    /** 変換機能:Android Maps */
    private AndroidMapsProcessor mapProcessor;
    /** 変換クラス */
    private BasicConverter tesToGpxConverter;
    /** 出力機能:GPXフォーマット */
    private GpxWriter gpxWriter;
    /** 出力機能:Android Maps */
    private AndroidMapsWriter mapWriter;;

    /** 入出力ファイルベースパス */
    private static String FILE_BASE_PATH;
    /** 入力ファイル格納先 */
    private static String INPUT_DIR;;
    /** 出力フォーマットファイル格納先 */
    private static String OUTPUT_DIR;;

    /** TESフォーマット変換結果 */
    private List<GpsData> gpsDataList;

    /** タグ */
    private static final String TAG = "dialog";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        FILE_BASE_PATH = FilePathUtil.getFileBasePath(this.getClass());

        if (FILE_BASE_PATH.length() != 0) {
            INPUT_DIR = FILE_BASE_PATH + "tes/";
            File file = new File(INPUT_DIR);
            if (!file.exists()) {
                file.mkdirs();
            }
            OUTPUT_DIR = FILE_BASE_PATH;
            file = new File(OUTPUT_DIR);
            if (!file.exists()) {
                file.mkdirs();
            }
        } else {
            INPUT_DIR = "";
            OUTPUT_DIR = "";
            DialogFragment fragment = TesConvertorDialogFragment.newInstance(R.string.err_msg_sdcard_not_found);
            fragment.show(getFragmentManager(), TAG);
        }

        btnConvert = (Button) this.findViewById(R.id.button1);
        btnInputFile = (Button) this.findViewById(R.id.button2);
        btnOutputFile = (Button) this.findViewById(R.id.button3);
        btnSave = (Button) this.findViewById(R.id.button4);
        editTextInput = (EditText) this.findViewById(R.id.editText1);
        editTextOutput = (EditText) this.findViewById(R.id.editText2);

        btnChangeView = (ToggleButton) this.findViewById(R.id.toggleButton1);
        textFragment = this.getFragmentManager().findFragmentById(R.id.text_view);

        btnConvert.setOnClickListener(new onButton1Click());
        btnInputFile.setOnClickListener(new onButton2Click());
        btnOutputFile.setOnClickListener(new onButton3Click());
        btnSave.setOnClickListener(new onButton4Click());
        btnChangeView.setOnCheckedChangeListener(new onCheckedChangeListener());
        editTextInput.addTextChangedListener(new onEditText1ChangedListener());

        fileBaseProcessor = new FileBaseProcessor();
        mapProcessor = new AndroidMapsProcessor();
        tesToGpxConverter = new BasicConverter();
        gpxWriter = new GpxWriter();
        mapWriter = new AndroidMapsWriter();
        setMapFragment(savedInstanceState);
        btnChangeView.setChecked(true);


        Resources res = getResources();
        TextView inputDir = ((TextView) this.findViewById(R.id.textView3));
        TextView outputDir = ((TextView) this.findViewById(R.id.textView4));
        inputDir.setText(res.getString(R.string.input_file_dir) + INPUT_DIR);
        outputDir.setText(res.getString(R.string.output_file_dir) + OUTPUT_DIR);
        inputDir.setTypeface(Typeface.DEFAULT_BOLD);
        outputDir.setTypeface(Typeface.DEFAULT_BOLD);
    }

    /**
     *
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    /**
     *
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
        case R.id.item1:
            Intent intent1 = new Intent(this, SettingActivity.class);
            startActivity(intent1);
            break;
        case R.id.item2:
            Intent intent2 = new Intent(this, HelpActivity.class);
            startActivity(intent2);
            break;
        }
        return true;
    }

    /**
     *
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     *
     */
    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    /**
     * 表示切り換えボタンのコールバックを定義するクラス。
     *
     * @author mkh
     *
     */
    private class onCheckedChangeListener implements OnCheckedChangeListener {

        /**
         *
         */
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            setMapFragment(mapFragment);
            if (isChecked) {
                ft.hide(textFragment);
                ft.show(mapFragment);
            } else {
                ft.hide(mapFragment);
                ft.show(textFragment);
            }
            ft.commit();
        }

    }

    /**
     * 変換ボタンのコールバックを定義するクラス。
     *
     * @author mkh
     *
     */
    private class onButton1Click implements OnClickListener {

        /**
         *
         */
        @Override
        public void onClick(View v) {

            String fileName = editTextInput.getText().toString().trim();
            if (fileName == null || fileName.length() == 0) {
                DialogFragment fragment = TesConvertorDialogFragment.newInstance(R.string.err_msg_inputFileNotDefined);
                fragment.show(getFragmentManager(), TAG);
                return;
            }
            final String path = INPUT_DIR + fileName;
            fileBaseProcessor.setInputFilePath(path);

            fileBaseProcessor.init();
            tesToGpxConverter.init();

            UpdateProgressDialogForInput dialog = new UpdateProgressDialogForInput(TesConvertorActivity.this,
                    tesToGpxConverter, fileBaseProcessor);
            dialog.execute("");

            final Handler h = new Handler();

            new Thread() {

                @Override
                public void run() {
                    gpsDataList = convert(path);

                    new Thread() {
                        public void run() {
                            h.post(new Runnable() {

                                @Override
                                public void run() {
                                    if (gpsDataList != null) {
                                        ((TextFragment) textFragment).setText(gpsDataList);
                                        try {
                                            setMapFragment(mapFragment);
                                            mapProcessor.write(gpsDataList, mapWriter);
                                        } catch (Throwable e) {
                                            // 設計上ここで例外が出ることは考えにくい。
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                        }
                    }.start();
                    if (SettingActivity.isWriteSameTime(getBaseContext())) {
                        fileBaseProcessor.init();
                        gpxWriter.init();
                        new Thread() {
                            public void run() {
                                h.post(new Runnable() {

                                    @Override
                                    public void run() {
                                        saveGpsDataList(gpsDataList);
                                    }
                                });
                            }
                        }.start();
                    }
                }
            }.start();
        }

    }

    /**
     * 入力ファイルの候補を表示するボタンのコールバックを定義するクラス。
     *
     * @author mkh
     *
     */
    private class onButton2Click implements OnClickListener {

        /**
         *
         */
        @Override
        public void onClick(View v) {
            TesConvertorDialogFragment fragment = TesConvertorDialogFragment
                    .newInstance(R.string.dialog_title_input_file);
            fragment.setEditText(editTextInput);
            fragment.show(getFragmentManager(), TAG);
        }
    }

    /**
     * 入力ファイル欄が変更されたときのコールバックを定義するクラス。
     * @author mkh
     *
     */
    private class onEditText1ChangedListener implements TextWatcher {

        @Override
        public void afterTextChanged(Editable s) {
            if(SettingActivity.isAutoFillOutputFileName(getBaseContext())) {
                String fileName = s.toString();
                if(fileName.endsWith(".tes") || fileName.endsWith(".TES")) {
                    fileName = fileName.substring(0, fileName.length()-".tes".length());
                }
                ((EditText)findViewById(R.id.editText2)).setText(fileName+".gpx");
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

    }

    /**
     * 出力ファイルの候補を表示するボタンのコールバックを定義するクラス。
     *
     * @author mkh
     *
     */
    private class onButton3Click implements OnClickListener {

        /**
         *
         */
        @Override
        public void onClick(View v) {
            TesConvertorDialogFragment fragment = TesConvertorDialogFragment
                    .newInstance(R.string.dialog_title_output_file);
            fragment.setEditText(editTextOutput);
            fragment.show(getFragmentManager(), TAG);
        }
    }

    /**
     * 出力ボタンのコールバックを定義するクラス。
     *
     * @author mkh
     *
     */
    private class onButton4Click implements OnClickListener {

        /**
         *
         */
        @Override
        public void onClick(View v) {

            if (gpsDataList == null || gpsDataList.size() == 0) {
                DialogFragment fragment = TesConvertorDialogFragment.newInstance(R.string.err_msg_notConverted);
                fragment.show(getFragmentManager(), TAG);
                return;
            }

            fileBaseProcessor.init();
            gpxWriter.init();

            saveGpsDataList(gpsDataList);
        }
    }

    /**
     * TESフォーマットを変換する。
     *
     * @param filePath
     *            変換対象ファイルのフルパス
     * @return {@code GpsData}形式のリスト
     */
    private List<GpsData> convert(String filePath) {
        List<GpsData> ret;
        try {
            ret = fileBaseProcessor.convert(tesToGpxConverter);
        } catch (FileNotFoundException e) {
            DialogFragment fragment = TesConvertorDialogFragment.newInstance(R.string.err_msg_inputFileNotFound);
            fragment.show(getFragmentManager(), TAG);
            return null;
        } catch (IOException e) {
            DialogFragment fragment = TesConvertorDialogFragment.newInstance(R.string.err_msg_IOException);
            fragment.show(getFragmentManager(), TAG);
            return null;
        }
        return ret;
    }

    /**
     * 変換結果を出力する。
     *
     * @param list
     *            出力対象
     */
    private void saveGpsDataList(List<GpsData> list) {

        String fileName = editTextOutput.getText().toString().trim();
        if (fileName == null || fileName.length() == 0) {
            DialogFragment fragment = TesConvertorDialogFragment.newInstance(R.string.err_msg_outputFileNotDefined);
            fragment.show(getFragmentManager(), TAG);
            return;
        }

        String outPath = OUTPUT_DIR + fileName;

        fileBaseProcessor.setOutputFilePath(outPath);

        try {
            if (fileBaseProcessor.isExists()) {
                TesConvertorDialogFragment fragment = TesConvertorDialogFragment
                        .newInstance(R.string.err_msg_fileAlreadyExist);
                fragment.setTesToGpx(fileBaseProcessor);
                fragment.setList(list);
                fragment.setEditText(editTextOutput);
                fragment.setWriter(gpxWriter);
                fragment.setActivity(TesConvertorActivity.this);
                fragment.show(getFragmentManager(), TAG);
            } else {
                UpdateProgressDialogForWrite dialog = new UpdateProgressDialogForWrite(TesConvertorActivity.this,
                        gpxWriter, fileBaseProcessor);
                dialog.execute("");

                fileBaseProcessor.write(list, gpxWriter);
            }

        } catch (ParserConfigurationException e) {
            TesConvertorDialogFragment fragment = TesConvertorDialogFragment
                    .newInstance(R.string.err_msg_ParserConfigurationException);
            fragment.show(getFragmentManager(), TAG);
            return;
        } catch (TransformerException e) {
            TesConvertorDialogFragment fragment = TesConvertorDialogFragment
                    .newInstance(R.string.err_msg_TransformerException);
            fragment.show(getFragmentManager(), TAG);
            return;
        } catch (IOException e) {
            TesConvertorDialogFragment fragment = TesConvertorDialogFragment.newInstance(R.string.err_msg_IOException);
            fragment.show(getFragmentManager(), TAG);
            return;
        } catch (Throwable e) {
            TesConvertorDialogFragment fragment = TesConvertorDialogFragment.newInstance(R.string.err_msg_err);
            fragment.setExceptionName(e.getClass().getName());
            fragment.show(getFragmentManager(), TAG);
            return;
        }
    }

    /**
     * MapFragmentを設定する。 <br>
     * 引数がnullの場合のみ設定する。
     *
     * @param forNullCheck
     *            nullかどうかをチェックするインスタンス。nullの場合のみ、MapFragmentの設定が行われる。
     */
    private void setMapFragment(Object forNullCheck) {
        if (forNullCheck == null) {
            mapFragment = TesConvertorActivity.this.getFragmentManager().findFragmentById(R.id.map_view);
            mapProcessor.setMapFragment(mapFragment);
        }
    }

    /**
     * ユーティリティクラス。
     *
     * @author mkh
     *
     */
    private class Util {

        /**
         * ディレクトリに存在するファイル名の一覧を取得する。
         *
         * @param filePath
         *            ファイル名の一覧を取得するディレクトリ
         * @return ファイル名の一覧
         */
        public String[] getFileNames(String filePath) {
            if (filePath == null ||  filePath.length() == 0) {
                return new String[0];
            }
            File file = new File(filePath);
            File[] existFiles = file.listFiles();

            List<String> fileNames = new ArrayList<String>(existFiles.length);
            for (File item : existFiles) {
                if (item.isFile()) {
                    fileNames.add(item.getName());
                }
            }
            Collections.sort(fileNames);
            String[] ret = fileNames.size() == 0 ? null : objectArrayToStringArray(fileNames.toArray());
            return ret;
        }

        /**
         * {@code Object}クラスの配列を{@code String}クラスの配列に変換する。
         *
         * @param objArray
         * @return
         */
        private String[] objectArrayToStringArray(Object[] objArray) {
            int length = objArray.length;

            String[] ret = new String[length];
            for (int i = 0; i < length; i++) {
                ret[i] = (String) objArray[i];
            }

            return ret;
        }
    }

    /**
     * ダイアログボックスのフラグメントクラス。
     *
     * @author mkh
     *
     */
    public static class TesConvertorDialogFragment extends DialogFragment {

        /**
         * インスタンスを返却する。
         *
         * @param id
         *            表示するダイアログボックスを指定するID
         * @return インスタンス
         */
        public static TesConvertorDialogFragment newInstance(int id) {
            TesConvertorDialogFragment frag = new TesConvertorDialogFragment();
            Bundle args = new Bundle();
            args.putInt("id", id);
            frag.setArguments(args);
            return frag;
        }

        /**
         * 。
         * <ul>
         * <li>dialog_title_input_file:{@code setEditText}で{@code EditText}
         * を設定しておくこと
         * <li>dialog_title_output_file:{@code setEditText}で{@code EditText}
         * を設定しておくこと
         * <li>err_msg_fileAlreadyExist:{@code setTesToGpx}で
         * {@code FileBaseProcessor}を、setListで変換対象リストを 、{@code setWriter}で
         * {@code Writer}を、{@code setActivity}で{@code Activity}を、それぞれ設定しておくこと
         * <li>err_msg_err:{@code setExce@tionName }で例外名称を設定しておくこと
         * <li>
         * </ul>
         */
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int id = getArguments().getInt("id");

            AlertDialog ret = null;
            final Util util = new TesConvertorActivity().new Util();

            switch (id) {
            case R.string.err_msg_inputFileNotFound:
                ret = new AlertDialog.Builder(getActivity()).setTitle(R.string.dialog_title_err)
                        .setMessage(R.string.err_msg_inputFileNotFound).setPositiveButton(R.string.btn_text_ok, null)
                        .create();
                break;
            case R.string.err_msg_inputFileNotDefined:
                ret = new AlertDialog.Builder(getActivity()).setTitle(R.string.dialog_title_err)
                        .setMessage(R.string.err_msg_inputFileNotDefined).setPositiveButton(R.string.btn_text_ok, null)
                        .create();
                break;
            case R.string.err_msg_IOException:
                ret = new AlertDialog.Builder(getActivity()).setTitle(R.string.dialog_title_err)
                        .setMessage(R.string.err_msg_IOException).setPositiveButton(R.string.btn_text_ok, null)
                        .create();
                break;
            case R.string.err_msg_notConverted:
                ret = new AlertDialog.Builder(getActivity()).setTitle(R.string.dialog_title_err)
                        .setMessage(R.string.err_msg_notConverted).setPositiveButton(R.string.btn_text_ok, null)
                        .create();
                break;
            case R.string.err_msg_outputFileNotDefined:
                ret = new AlertDialog.Builder(getActivity()).setTitle(R.string.dialog_title_err)
                        .setMessage(R.string.err_msg_outputFileNotDefined)
                        .setPositiveButton(R.string.btn_text_ok, null).create();
                break;
            case R.string.err_msg_err:
                ret = new AlertDialog.Builder(getActivity()).setTitle(R.string.dialog_title_err)
                        .setMessage(R.string.err_msg_err + ":" + exceptionName)
                        .setPositiveButton(R.string.btn_text_ok, null).create();
                break;
            case R.string.err_msg_ParserConfigurationException:
                ret = new AlertDialog.Builder(getActivity()).setTitle(R.string.dialog_title_err)
                        .setMessage(R.string.err_msg_ParserConfigurationException)
                        .setPositiveButton(R.string.btn_text_ok, null).create();
                break;
            case R.string.err_msg_TransformerException:
                ret = new AlertDialog.Builder(getActivity()).setTitle(R.string.dialog_title_err)
                        .setMessage(R.string.err_msg_TransformerException)
                        .setPositiveButton(R.string.btn_text_ok, null).create();
                break;
            case R.string.err_msg_sdcard_not_found:
                ret = new AlertDialog.Builder(getActivity()).setTitle(R.string.dialog_title_err)
                        .setMessage(R.string.err_msg_sdcard_not_found).setPositiveButton(R.string.btn_text_ok, null)
                        .create();
                break;
            case R.string.info_msg_nofiles:
                ret = new AlertDialog.Builder(getActivity()).setTitle(R.string.dialog_title_info)
                        .setMessage(R.string.info_msg_nofiles).setPositiveButton(R.string.btn_text_ok, null).create();
                break;
            case R.string.dialog_title_input_file:
                ret = new AlertDialog.Builder(getActivity()).setTitle(R.string.dialog_title_input_file)
                        .setItems(util.getFileNames(INPUT_DIR), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                editText.setText(util.getFileNames(INPUT_DIR)[which]);
                            }
                        }).create();
                break;
            case R.string.dialog_title_output_file:
                ret = new AlertDialog.Builder(getActivity()).setTitle(R.string.dialog_title_output_file)
                        .setItems(util.getFileNames(OUTPUT_DIR), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                editText.setText(util.getFileNames(OUTPUT_DIR)[which]);
                            }
                        }).create();
                break;
            case R.string.err_msg_fileAlreadyExist:
                ret = new AlertDialog.Builder(getActivity()).setTitle(R.string.dialog_title_info)
                        .setMessage(R.string.err_msg_fileAlreadyExist)
                        .setPositiveButton(R.string.btn_text_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    UpdateProgressDialogForWrite progressDialog = new UpdateProgressDialogForWrite(
                                            activity, (DataSizeGettable) writer, fileBaseProcessor);
                                    progressDialog.execute("");

                                    fileBaseProcessor.write(list, writer);
                                } catch (Throwable e) {
                                    TesConvertorDialogFragment fragment = TesConvertorDialogFragment
                                            .newInstance(R.string.err_msg_err);
                                    fragment.setExceptionName(e.getClass().getName());
                                    fragment.show(getFragmentManager(), TAG);
                                }

                            }
                        }).setNegativeButton(R.string.btn_text_no, null).create();
                break;
            default:
                break;
            }
            return ret;
        }

        /** 入力欄 */
        private EditText editText;

        /**
         * 入力欄を設定する。
         *
         * @param editText
         *            入力欄
         */
        public void setEditText(EditText editText) {
            this.editText = editText;
        }

        /** 使用する変換機能クラス */
        private FileBaseProcessor fileBaseProcessor;

        /**
         * 使用する変換機能クラスを設定する。
         *
         * @param tesToGpx
         *            変換機能クラス
         */
        public void setTesToGpx(FileBaseProcessor tesToGpx) {
            this.fileBaseProcessor = tesToGpx;
        }

        /** 変換対象のリスト */
        private List<GpsData> list;

        /**
         * 変換対象のリストを設定する。
         *
         * @param list
         */
        public void setList(List<GpsData> list) {
            this.list = list;
        }

        /** 例外の名前 */
        private String exceptionName;

        /**
         * 例外の名前を設定する。
         *
         * @param exceptionName
         *            例外の名前
         */
        public void setExceptionName(String exceptionName) {
            this.exceptionName = exceptionName;
        }

        /** 使用する出力クラス */
        private Writer writer;

        /**
         * 使用する出力クラスを設定する。
         *
         * @param writer
         *            出力クラス
         */
        public void setWriter(Writer writer) {
            this.writer = writer;
        }

        /** アクティビティ */
        private TesConvertorActivity activity;

        /**
         * アクティビティを設定する。
         *
         * @param activity
         *            アクティビティ
         */
        public void setActivity(TesConvertorActivity activity) {
            this.activity = activity;
        }
    }
}