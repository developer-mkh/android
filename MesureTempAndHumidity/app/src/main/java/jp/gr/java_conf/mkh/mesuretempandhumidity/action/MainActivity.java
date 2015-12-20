package jp.gr.java_conf.mkh.mesuretempandhumidity.action;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import jp.gr.java_conf.mkh.mesuretempandhumidity.R;

public class MainActivity extends AppCompatActivity {

    // USBアクセサリ関連
    private UsbManager usbManager;
    private UsbAccessory usbAccessory;
    private boolean isReadable = false;

    // USBアクセサリ接続許可関連
    private PendingIntent permissionIntent;
    private boolean permissionRequestPending;
    private static final String ACTION_USB_PERMISSION = "jp.gr.java_conf.mkh.mesuretempandhumidity.action.USB_PERMISSION";

    // ファイルIO関連
    private ParcelFileDescriptor fileDescriptor;
    private OutputStream outputStream;
    private FileInputStream inputStream;

    // 画面要素
    private TextView textView;
    private Button button;

    // DB関連
    // DB名
    private static final String DB_NAME = "MESURED_DATA";
    // DBバージョン
    private static final int DB_VERSION = 1;
    // テーブル名
    private static final String TABLE_NAME = "MESURED_DATA";
    private static final String CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                    " ( _id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "EPOC, TEMP, HUMIDITY)";
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(getApplicationContext());

        ((Button) findViewById(R.id.button2)).setText(R.string.button_output);
        textView = (TextView) findViewById(R.id.textView);
        button = (Button) findViewById(R.id.button);
        button.setText(R.string.button_start);

        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);

        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        registerReceiver(usaReceiver, filter);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isReadable = !isReadable;
                if (isReadable) {
                    button.setText(R.string.button_stop);
                } else {
                    button.setText(R.string.button_start);
                }
                new Thread(new ReadAccessory()).start();
            }
        });

        ((Button) findViewById(R.id.button2)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isReadable = false;
                button.setText(R.string.button_start);
                Cursor cursor = dbHelper.query(TABLE_NAME, new String[]{"EPOC", "TEMP", "HUMIDITY"}, null, null, "EPOC");

                FileOutputStream fileOutputStream;
                BufferedWriter saveWriter = null;
                try {
                    String path = Environment.getExternalStorageDirectory().getPath() + "/data.csv";
                    File outFile = new File(path);
                    fileOutputStream = new FileOutputStream(outFile, false);
                    saveWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
                } catch (FileNotFoundException e) {
                    new AlertDialog.Builder(MainActivity.this).setMessage(e.getMessage()).setTitle("").show();
                }

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss ");
                String str;
                textView.setText("");
                while (cursor.moveToNext()) {
                    str = sdf.format(cursor.getLong(0)) + ", " + cursor.getString(1) + ", " + cursor.getString(2) + "\n";
                    textView.setText(textView.getText() + str);
                    try {
                        saveWriter.write(str);
                    } catch (IOException e) {
                        new AlertDialog.Builder(MainActivity.this).setMessage(e.getMessage()).setTitle("").show();
                    }
                }
                try {
                    saveWriter.flush();
                    saveWriter.close();
                } catch (IOException e) {
                    new AlertDialog.Builder(MainActivity.this).setMessage(e.getMessage()).setTitle("").show();
                } finally {
                    cursor.close();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        UsbAccessory[] accessories = usbManager.getAccessoryList();
        usbAccessory = (accessories == null ? null : accessories[0]);
        if (usbAccessory != null) {
            if (usbManager.hasPermission(usbAccessory)) {
                openAccessory(usbAccessory);
            } else {
                synchronized (this) {
                    if (!permissionRequestPending) {
                        usbManager.requestPermission(usbAccessory, permissionIntent);
                        permissionRequestPending = true;
                    }
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        closeAccessory();
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(usaReceiver);
        super.onDestroy();
    }

    /**
     * アクセサリのオープン。
     *
     * @param accessory 対象のアクセサリ
     */
    private void openAccessory(UsbAccessory accessory) {
        fileDescriptor = usbManager.openAccessory(accessory);
        if (fileDescriptor != null) {
            FileDescriptor fd = fileDescriptor.getFileDescriptor();
            outputStream = new FileOutputStream(fd);
            inputStream = new FileInputStream(fd);
            button.setEnabled(true);
        }
    }

    /**
     * アクセサリのクローズ。
     */
    private void closeAccessory() {
        try {
            if (fileDescriptor != null) {
                fileDescriptor.close();
            }
        } catch (IOException e) {
            new AlertDialog.Builder(MainActivity.this).setMessage(e.getMessage()).setTitle("").show();
        } finally {
            inputStream = null;
            outputStream = null;
            fileDescriptor = null;
            usbAccessory = null;
        }
    }

    /**
     * ブロードキャストレシーバー。
     */
    private final BroadcastReceiver usaReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        openAccessory(accessory);
                    }
                    permissionRequestPending = false;
                }
            } else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
                UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                if (accessory != null && accessory.equals(usbAccessory)) {
                    closeAccessory();
                }
            }
        }
    };

    /**
     * アクセサリからデータを読み取り、DBに記録する。
     */
    private class ReadAccessory implements Runnable {
        @Override
        public void run() {
            final byte[] buffer = new byte[11];
            while (isReadable && (inputStream != null)) {
                try {
                    inputStream.read(buffer);
                } catch (IOException e) {
                    new AlertDialog.Builder(MainActivity.this).setMessage(e.getMessage()).setTitle("").show();
                }
                final String data = new String(buffer);
                ContentValues content = new ContentValues();
                final long epoc = new Date().getTime();
                content.put("EPOC", epoc);
                content.put("TEMP", data.split(",")[0]);
                content.put("HUMIDITY", data.split(",")[1]);
                dbHelper.insert(TABLE_NAME, content);

                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView = (TextView) findViewById(R.id.textView);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss ");
                        textView.setText(textView.getText() + sdf.format(epoc) + data + "\n");
                    }
                });
            }
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((Button) findViewById(R.id.button)).setText(R.string.button_start);
                }
            });


        }
    }

    /**
     * データベースヘルパークラス
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // 今回は何もしない。
        }

        /**
         * 検索する。
         * @param tableName テーブル名
         * @param projection 取得カラム
         * @param selection where句
         * @param selectionOrder where句の引数
         * @param sortOrder order By 句
         * @return カーソル
         */
        public Cursor query(String tableName, String[] projection, String selection, String[] selectionOrder, String sortOrder) {
            SQLiteDatabase db = getWritableDatabase();
            return  db.query(
                    false,
                    tableName,
                    projection,
                    selection,
                    selectionOrder,
                    null,
                    null,
                    sortOrder,
                    null);
        }

        /**
         * 登録する。
         * @param tableName テーブル名
         * @param values 登録情報
         * @return 登録行数
         */
        public long insert(String tableName, ContentValues values) {
            SQLiteDatabase db = getWritableDatabase();
            long ret = db.insert(tableName, null, values);
            db.close();
            return ret;
        }

    }
}