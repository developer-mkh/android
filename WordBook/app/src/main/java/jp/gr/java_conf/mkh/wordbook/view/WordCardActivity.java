package jp.gr.java_conf.mkh.wordbook.view;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.TextView;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import jp.gr.java_conf.mkh.wordbook.R;
import jp.gr.java_conf.mkh.wordbook.contentprovider.WordBookContentProvider;

public class WordCardActivity extends Activity
        implements View.OnClickListener {

    /**
     * 単語をたどるためのカーソル
     */
    Cursor cursorForWord;

    /**
     * ボタンを押下された回数。負数は、カーソルが最終行まで行ったことを示す。
     */
    private int buttonPushCount;

    /**
     * 単語の状態(すでに表示されたかどうか)を保持する。
     */
    private boolean[] isShownWord;

    /**
     * 表示しているアイテムの位置
     */
    private int position;

    /**
     * 何週目か
     */
    private int lapCount;

    /** 開始時のカウントダウンスタートの秒数 */
    private int countDown;

    /** スケジューラ。単語を自動でめくる場合に使用 */
    private ScheduledExecutorService scheduler = null;

    /** スケジューラ設定後取得できるオブジェクト */
    private ScheduledFuture future;

    /** 自動でめくる時間(mSec) */
    private int period;

    // タイマー(スレッド処理)を終了してもよいか */
    private boolean canShutdown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_card);

        // コールバックの設定
        findViewById(R.id.next).setOnClickListener(this);
        findViewById(R.id.end_of_word_card).setOnClickListener(this);

        // アクションバーに「戻る」を表示。
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        cursorForWord = initCursor();
        findViewById(R.id.next).setEnabled(false);

        if (!cursorForWord.moveToNext()) {
            // 登録されている単語がない場合
            ((TextView) findViewById(R.id.word_word_card)).setText(getResources().getString(R.string.word_card_empty));
            return;
        }

        // タイマー初期設定
        if (scheduler == null || scheduler.isShutdown()) {
            scheduler = Executors.newSingleThreadScheduledExecutor();
        }
        canShutdown = false;

        // 1周目のみアニメーション
        if (savedInstanceState == null) {
            //　変数初期化
            lapCount = 1;
            isShownWord = new boolean[cursorForWord.getCount()];
            ((TextView) findViewById(R.id.word_card_count)).setText(getResources().getString(R.string.word_card_count) + String.valueOf(lapCount));

            // アニメーション
            countDown = 3;
            TextView animationText = (TextView) findViewById(R.id.word_word_card);
            drawCountDown(countDown, animationText, new Runnable() {
                @Override
                public void run() {
                    showSurface(true);
                    findViewById(R.id.next).setEnabled(true);
                }
            });
        }else {
            // 変数など復帰
            buttonPushCount = savedInstanceState.getInt("buttonPushCount");
            isShownWord = savedInstanceState.getBooleanArray("isShownWord");
            position = savedInstanceState.getInt("position");
            lapCount = savedInstanceState.getInt("lapCount");
            period = savedInstanceState.getInt("period");
            ((TextView) findViewById(R.id.word_card_count)).setText(getResources().getString(R.string.word_card_count) + String.valueOf(lapCount));

            if (buttonPushCount == 0) {
                // アニメーション
                countDown = 3;
                TextView animationText = (TextView) findViewById(R.id.word_word_card);
                drawCountDown(countDown, animationText, new Runnable() {
                    @Override
                    public void run() {
                        showSurface(position, true);
                        findViewById(R.id.next).setEnabled(true);
                    }
                });
            } else if  (cursorForWord.getCount() != 0) {
                findViewById(R.id.next).setEnabled(true);
                if (buttonPushCount % 2 == 0) {
                    showSurface(position, false);
                } else if ((buttonPushCount % 2 == 1) || (buttonPushCount < 0)) {
                    showRiversSide();
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            NavUtils.navigateUpTo(this, new Intent(this, WordListActivity.class));
            canShutdown = true;
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        buttonPushCount++;
        switch (v.getId()) {
            case R.id.next:
                if (buttonPushCount < 0) {
                    // 最後の単語まで表示しきって、再度開始する場合の処理
                    // 色々初期化
                    cursorForWord = initCursor();
                    buttonPushCount = 0;
                    isShownWord = new boolean[cursorForWord.getCount()];
                    countDown = 3;

                    // 初期表示
                    lapCount++;
                    final TextView textView = (TextView) findViewById((R.id.word_word_card));
                    Button nextButton = (Button) findViewById(R.id.next);
                    nextButton.setEnabled(false);
                    nextButton.setText(R.string.back_side);
                    drawCountDown(countDown, textView, new Runnable() {
                        @Override
                        public void run() {
                            ((TextView)findViewById(R.id.word_card_count)).setText(getResources().getString(R.string.word_card_count) + String.valueOf(lapCount));
                            showSurface(true);
                            findViewById(R.id.next).setEnabled(true);
                        }
                    });

                } else if (buttonPushCount % 2 == 1) {
                    showRiversSide();
                } else {
                    showSurface(true);
                }
                break;
            case R.id.end_of_word_card:
                canShutdown = true;
                NavUtils.navigateUpTo(this, new Intent(this, WordListActivity.class));
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cursorForWord.isClosed()) {
            cursorForWord.close();
        }
        if (canShutdown) {
            scheduler.shutdownNow();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("buttonPushCount", buttonPushCount);
        outState.putBooleanArray("isShownWord", isShownWord);
        outState.putInt("position", position);
        outState.putInt("lapCount", lapCount);
        outState.putInt("period", period);
    }

    /**
     * カーソルを初期化する。
     *
     * @return 初期化されたカーソル
     */
    private Cursor initCursor() {
        return getContentResolver().query(WordBookContentProvider.CONTENTS_URI_WORDBOOK,
                WordBookContentProvider.WORD_BOOK_ALL_COLUMNS,
                "TRAINING_TARGET = ?",
                new String[]{String.valueOf(WordBookContentProvider.TRUE)},
                "_id");
    }

    /**
     * 表示する単語を指定する数値を乱数を使って発生させる。
     * すべての単語を一回ずつ表示し終わるまで、同じ数字は発生させない。
     *
     * @return 0以上単語数未満の乱数
     */
    private int getRandomNumber() {
        // すべての番号を使い切っていないかどうかのチェック
        boolean isAllNumberUsed = true;
        for (boolean b : isShownWord) {
            if (!b) {
                isAllNumberUsed = false;
                break;
            }
        }
        if (isAllNumberUsed) {
            throw new IllegalStateException("There is no random number.");
        }

        boolean isNotMakeNumber = true;
        int ret = 0;
        while (isNotMakeNumber) {
            ret = (int) (Math.random() * cursorForWord.getCount());
            if (!isShownWord[ret]) {
                isNotMakeNumber = false;
            }
        }
        return ret;
    }

    /**
     * 　単語カード表面を表示するときの場合の処理
     * @param isInitPeriod 単語の意味自動表示のタイマーをリセットするか。リセットするときtrue
     */
    private void showSurface(boolean isInitPeriod) {
        position = getRandomNumber();
        showSurface(position, isInitPeriod);
    }

    /**
     * 　単語カード表面を単語を指定して、表示するときの場合の処理
     *
     * @param position 表示する単語の番号
     * @param isInitPeriod 単語の意味自動表示のタイマーをリセットするか。リセットするときtrue
     */
    private void showSurface(int position, boolean isInitPeriod) {
        cursorForWord.moveToPosition(position);
        ((TextView) findViewById(R.id.word_word_card)).setText(cursorForWord.getString(cursorForWord.getColumnIndex("WORD")));
        ((Button) findViewById(R.id.next)).setText(R.string.back_side);
        isShownWord[position] = true;
        if (!scheduler.isShutdown()) {
            processSchedule(isInitPeriod);
        }
    }

    /**
     * 単語カード裏面を表示する。
     */
    private void showRiversSide() {
        cursorForWord.moveToPosition(position);
        ((TextView) findViewById(R.id.word_word_card)).setText(cursorForWord.getString(cursorForWord.getColumnIndex("MEANING")));
        ((Button) findViewById(R.id.next)).setText(R.string.next);
        isShownWord[position] = true;

        if (future != null) {
            future.cancel(false);
        }
        ((TextView) findViewById(R.id.time_word_card)).setText("");

        // 終了判定
        boolean isEnd = true;
        for (boolean b : isShownWord) {
            if (!b) {
                isEnd = false;
                break;
            }
        }
        if (isEnd) {
            ((Button) findViewById(R.id.next)).setText(R.string.restart);
            // メソッドの頭でインクリメントし、裏面表示であることを剰余で判断するので、その分を引いておく。
            buttonPushCount = -3;
        }
    }

    /**
     * TextViewに対して表示値を設定して、postDelayedを実行する。
     * animationが指定されていれば、これも合わせてpostDelayedを実行する。
     * @param view postDelayedを実行するView
     * @param animation 実行したいアニメーション。なければnull
     * @param textView 表示先のTextView
     * @param displayString 表示文字列
     * @param duration 実行までの待ち時間(ミリ秒)
     */
    private void animationPostForTextView(View view, final Animation animation, final TextView textView, final String displayString, long duration) {
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                textView.setText(displayString);
                if (animation != null) {
                    textView.startAnimation(animation);
                }
            }
        }, duration);
    }

    /**
     * カウントダウンを表示する。
     * @param count カウント(秒)
     * @param textView 表示するTextView
     * @param postProcess カウント表示後に実行する処理
     */
    private void drawCountDown(int count, TextView textView, Runnable postProcess){

        ScaleAnimation animation = new ScaleAnimation(0.5f, 10.0f, 0.5f, 10.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(1000);
        animation.setInterpolator(new AccelerateInterpolator(0.5f));

        count++;
        for (int i = count; i >= 1; i-- ) {
            animationPostForTextView(textView, animation, textView, String.valueOf(i), (count - i) * 1000 );
            animationPostForTextView(textView, animation, textView, "", (count - i) * 1000 - 50);
        }
        textView.postDelayed(postProcess,  count * 1000 + 100);

    }

    /**
     * 単語カード表示間隔を取得する。
     * @return 表示間隔(mSec)。タイマー自体を使わない場合は、0
     */
    private int getInterval() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        int ret = 0;
        if (pref.getBoolean(WordSettingsActivity.KEY_USE_INTERVAL, false)) {
            ret = pref.getInt(WordSettingsActivity.KEY_INTERVAL, WordSettingsActivity.DEFAULT_INTERVAL);
        }
        return ret * 1000;
    }

    /**
     * スケジュール処理(裏面の自動表示)。
     * @param isInitPeriod タイマー(残り時間)をリセットするか否か。する場合true
     */
    private void processSchedule (boolean isInitPeriod) {
        // 更新間隔
        final int delay = 1000;

        if (isInitPeriod) {
            period = getInterval();
            period += delay;
        }

        if (period > 0) {
            future = scheduler.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            period -= delay;
                            if (period == 0) {
                                buttonPushCount++;
                                future.cancel(false);
                                showRiversSide();
                            } else {
                                if (!future.isCancelled()) {
                                    ((TextView) findViewById(R.id.time_word_card)).setText(String.format(getResources().getString(R.string.word_card_remaining), period / 1000));
                                }
                            }
                        }
                    });

                }
            }, 0, delay, TimeUnit.MILLISECONDS);
        }
    }
}





