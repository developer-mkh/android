package jp.gr.java_conf.mkh.wordbook.view;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import jp.gr.java_conf.mkh.wordbook.R;
import jp.gr.java_conf.mkh.wordbook.preference.NumberPickerPreference;

/**
 * 設定画面。
 */
public class WordSettingsActivity extends Activity {

    /** 単語カード表示間隔のキー */
    public static final String KEY_INTERVAL = "interval";

    /** 単語カード 意味自動表示の使用/非使用のキー */
    public static final String KEY_USE_INTERVAL = "use_interval";

    /** 初期表示間隔 */
    public static final int DEFAULT_INTERVAL = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // アクションバーに「戻る」を表示。
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new WordPreferenceFragment())
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            Intent intent = new Intent(this, WordListActivity.class);
            intent.putExtra(WordDetailFragment.ARG_ITEM_ID, getIntent().getLongExtra(WordDetailFragment.ARG_ITEM_ID, -1));
            NavUtils.navigateUpTo(this, intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 設定画面フラグメント。
     */
    public static class WordPreferenceFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
            NumberPickerPreference picker = (NumberPickerPreference) getPreferenceManager().findPreference(KEY_INTERVAL);
            picker.setSummary(getResources().getString(R.string.pref_word_card_interval_summary, getPreferenceManager().getSharedPreferences().getInt(KEY_INTERVAL, DEFAULT_INTERVAL)));
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (KEY_INTERVAL.equals(key)) {
                NumberPickerPreference picker = (NumberPickerPreference) getPreferenceManager().findPreference(key);
                picker.setSummary(getResources().getString(R.string.pref_word_card_interval_summary, sharedPreferences.getInt(key, DEFAULT_INTERVAL)));
            } else if (KEY_USE_INTERVAL.equals(key)) {
                // NOP
            }
        }
    }
}
