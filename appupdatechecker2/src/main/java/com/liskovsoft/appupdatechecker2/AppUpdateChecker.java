package com.liskovsoft.appupdatechecker2;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import com.liskovsoft.appupdatechecker2.core.AppDownloader;
import com.liskovsoft.appupdatechecker2.core.AppDownloaderListener;
import com.liskovsoft.appupdatechecker2.core.AppVersionChecker;
import com.liskovsoft.appupdatechecker2.core.AppVersionCheckerListener;
import com.liskovsoft.sharedutils.helpers.Helpers;
import edu.mit.mobile.android.appupdater.R;

import java.util.List;

public class AppUpdateChecker implements AppVersionCheckerListener, AppDownloaderListener {
    public static final String SHARED_PREFERENCES_NAME = "com.liskovsoft.appupdatechecker2.preferences";
    public static final String PREF_ENABLED = "enabled", PREF_MIN_INTERVAL = "min_interval", PREF_LAST_UPDATED = "last_checked";
    private static final int MILLISECONDS_IN_MINUTE = 60_000;
    private final Context mContext;
    private final SharedPreferences mPrefs;
    private final AppVersionChecker mVersionChecker;
    private final AppDownloader mDownloader;

    public AppUpdateChecker(Context context) {
        mContext = context.getApplicationContext();
        mVersionChecker = new AppVersionChecker(mContext, this);
        mDownloader = new AppDownloader(mContext, this);

        mPrefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        // defaults are kept in the preference file for ease of tweaking
        PreferenceManager.setDefaultValues(context, SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE, R.xml.preferences, true);
    }

    // min interval is stored as a string so a preference editor could potentially edit it using a text edit widget

    public int getMinInterval() {
        return Integer.parseInt(mPrefs.getString(PREF_MIN_INTERVAL, "60"));
    }

    public void setMinInterval(int minutes) {
        mPrefs.edit().putString(PREF_MIN_INTERVAL, String.valueOf(minutes)).apply();
    }

    public boolean getEnabled() {
        return mPrefs.getBoolean(PREF_ENABLED, true);
    }

    public void setEnabled(boolean enabled) {
        mPrefs.edit().putBoolean(PREF_ENABLED, enabled).apply();
    }

    /**
     * You normally shouldn't need to call this, as {@link #checkForUpdates(String[] versionListUrls)} checks it before doing any updates.
     *
     * @return true if the updater should check for updates
     */
    public boolean isStale() {
        return System.currentTimeMillis() - mPrefs.getLong(PREF_LAST_UPDATED, 0) > getMinInterval() * MILLISECONDS_IN_MINUTE;
    }

    /**
     * Checks for updates if updates haven't been checked for recently and if checking is enabled.
     */
    public void checkForUpdates(String[] versionListUrls) {
        if (getEnabled() && isStale()) {
            mVersionChecker.forceCheckForUpdates(versionListUrls);
        }
    }

    /**
     * Check for updates is enabled
     */
    public void forceCheckForUpdatesIfEnabled(String[] versionListUrls) {
        if (getEnabled()) {
            mVersionChecker.forceCheckForUpdates(versionListUrls);
        }
    }

    /**
     * Minimize server payload!<br/>
     * Check for updates only if prev update was long enough
     */
    public void forceCheckForUpdatesIfStalled(String[] versionListUrls) {
        if (isStale()) {
            mVersionChecker.forceCheckForUpdates(versionListUrls);
        }
    }

    @Override
    public void onChangelogReceived(boolean isLatestVersion, String latestVersionName, List<String> changelog, Uri[] downloadUris) {
        if (!isLatestVersion && downloadUris != null) {
            mDownloader.download(downloadUris);
        }
    }

    @Override
    public void onApkDownloaded(String path) {
        if (path != null) {
            Helpers.installPackage(mContext, path);
            // this line may not be executed because of json error above
            mPrefs.edit().putLong(PREF_LAST_UPDATED, System.currentTimeMillis()).apply();
        }
    }

    @Override
    public void onCheckError(Exception e) {

    }

    @Override
    public void onDownloadError(Exception e) {

    }
}
