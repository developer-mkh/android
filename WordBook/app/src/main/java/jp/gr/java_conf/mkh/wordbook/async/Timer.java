package jp.gr.java_conf.mkh.wordbook.async;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.Map;

/**
 * タイマー
 */
public class Timer extends AsyncTaskLoader<Map<String, Object>> {

    private long sec;

    public Timer(Context ctx, long sec){
        super(ctx);
        this.sec = sec;
    }

    @Override
    public Map<String, Object> loadInBackground() {
        try {
            Thread.sleep(sec);
        } catch (InterruptedException e) {

        }
        return null;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }
}
