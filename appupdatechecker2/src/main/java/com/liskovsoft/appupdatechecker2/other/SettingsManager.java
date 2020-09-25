package com.liskovsoft.appupdatechecker2.other;

import android.content.Context;
import android.content.SharedPreferences;
import com.liskovsoft.sharedutils.mylogger.Log;
import edu.mit.mobile.android.appupdater.R;

import java.util.List;

public class SettingsManager {
    private static final String TAG = SettingsManager.class.getSimpleName();
    private static final String SHARED_PREFERENCES_NAME = "com.liskovsoft.appupdatechecker2.preferences";
    private static final String PREF_ENABLED = "enabled";
    private static final String PREF_MIN_INTERVAL = "min_interval";
    private static final String PREF_LAST_UPDATED = "last_checked";
    private static final String PREF_CHANGELOG = "changelog";
    private static final String PREF_APK_PATH = "apk_path";
    private final Context mContext;
    private final SharedPreferences mPrefs;

    public SettingsManager(Context context) {
        Log.d(TAG, "Starting...");

        mContext = context.getApplicationContext();

        mPrefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        // defaults are kept in the preference file for ease of tweaking
        android.preference.PreferenceManager.setDefaultValues(context, SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE, R.xml.preferences, true);
    }

    public long getLastUpdatedMs() {
        return mPrefs.getLong(PREF_LAST_UPDATED, 0);
    }

    public void setLastUpdatedMs(long milliseconds) {
        mPrefs.edit().putLong(PREF_LAST_UPDATED, milliseconds).apply();
    }

    public void setChangeLog(List<String> changeLog) {
        StringBuilder result = new StringBuilder();

        for (String change : changeLog) {
            if (result.length() != 0) {
                result.append(",");
            }

            result.append(change);
        }

        mPrefs.edit().putString(PREF_CHANGELOG, result.toString()).apply();
    }

    public void setApkPath(String path) {
        mPrefs.edit().putString(PREF_APK_PATH, path).apply();
    }

    /**
     * Min interval is stored as a string so a preference editor could potentially edit it using a text edit widget
     */
    public int getMinInterval() {
        return Integer.parseInt(mPrefs.getString(PREF_MIN_INTERVAL, "60"));
    }

    public void setMinInterval(int minutes) {
        mPrefs.edit().putString(PREF_MIN_INTERVAL, String.valueOf(minutes)).apply();
    }

    public boolean isEnabled() {
        return mPrefs.getBoolean(PREF_ENABLED, true);
    }

    public void setEnabled(boolean enabled) {
        mPrefs.edit().putBoolean(PREF_ENABLED, enabled).apply();
    }
}
