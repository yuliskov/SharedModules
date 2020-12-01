package com.liskovsoft.appupdatechecker2;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import com.liskovsoft.appupdatechecker2.core.AppDownloader;
import com.liskovsoft.appupdatechecker2.core.AppDownloaderListener;
import com.liskovsoft.appupdatechecker2.core.AppVersionChecker;
import com.liskovsoft.appupdatechecker2.core.AppVersionCheckerListener;
import com.liskovsoft.appupdatechecker2.other.SettingsManager;
import com.liskovsoft.sharedutils.helpers.FileHelpers;
import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.sharedutils.helpers.PermissionHelpers;
import com.liskovsoft.sharedutils.mylogger.Log;

import java.util.List;

public class AppUpdateChecker implements AppVersionCheckerListener, AppDownloaderListener {
    private static final String TAG = AppUpdateChecker.class.getSimpleName();
    private static final int FRESH_FILE_HOURS = 1;
    private final Context mContext;
    private final AppVersionChecker mVersionChecker;
    private final AppDownloader mDownloader;
    private final AppUpdateCheckerListener mListener;
    private final SettingsManager mSettingsManager;
    private List<String> mChangeLog;
    private String mLatestVersionName;

    public AppUpdateChecker(Context context, AppUpdateCheckerListener listener) {
        Log.d(TAG, "Starting...");

        mContext = context.getApplicationContext();
        mListener = listener;
        mVersionChecker = new AppVersionChecker(mContext, this);
        mDownloader = new AppDownloader(mContext, this);
        mSettingsManager = new SettingsManager(mContext);
    }

    /**
     * You normally shouldn't need to call this, as {@link #checkForUpdates(String[] versionListUrls)} checks it before doing any updates.
     *
     * @return true if the updater should check for updates
     */
    private boolean isStale() {
        if (mSettingsManager.getMinIntervalMs() < 0) {
            return false;
        }

        return System.currentTimeMillis() - mSettingsManager.getLastCheckedMs() > mSettingsManager.getMinIntervalMs();
    }

    public void checkForUpdates(String updateManifestUrl) {
        checkForUpdates(new String[]{updateManifestUrl});
    }

    /**
     * Checks for updates if updates haven't been checked for recently and if checking is enabled.
     */
    public void checkForUpdates(String[] updateManifestUrls) {
        if (isEnabled() && isStale()) {
            checkForUpdatesInt(updateManifestUrls);
        }
    }

    public void forceCheckForUpdates(String updateManifestUrl) {
        forceCheckForUpdates(new String[]{updateManifestUrl});
    }

    public void forceCheckForUpdates(String[] updateManifestUrls) {
        checkForUpdatesInt(updateManifestUrls);
    }

    private void checkForUpdatesInt(String[] updateManifestUrls) {
        // Workaround for Android 6 (cannot write to app cache dir)
        if (Build.VERSION.SDK_INT == 23) {
            PermissionHelpers.verifyStoragePermissions(mContext);
        }

        if (!checkPostponed()) {
            mVersionChecker.checkForUpdates(updateManifestUrls);
        }
    }

    private boolean checkPostponed() {
        return false;
    }

    @Override
    public void onChangelogReceived(boolean isLatestVersion, String latestVersionName, int latestVersionNumber, List<String> changelog, Uri[] downloadUris) {
        if (!isLatestVersion) {
            if (downloadUris != null) {
                mChangeLog = changelog;
                mLatestVersionName = latestVersionName;
                mSettingsManager.setLatestVersionName(latestVersionName);
                mSettingsManager.setLatestVersionNumber(latestVersionNumber);

                if (latestVersionNumber == mSettingsManager.getLatestVersionNumber() &&
                        FileHelpers.isFreshFile(mSettingsManager.getApkPath(), FRESH_FILE_HOURS)) {
                    mListener.onUpdateFound(latestVersionName, changelog, mSettingsManager.getApkPath());
                } else {
                    mDownloader.download(downloadUris);
                }
            }
        } else {
            // No update is needed.
            mSettingsManager.setLastCheckedMs(System.currentTimeMillis());
            //  Cleanup the storage. I don't want to accidentally install old version.
            FileHelpers.delete(mSettingsManager.getApkPath());

            mListener.onError(new IllegalStateException(AppUpdateCheckerListener.LATEST_VERSION));
        }
    }

    @Override
    public void onApkDownloaded(String path) {
        if (path != null) {
            mSettingsManager.setApkPath(path);

            Log.d(TAG, "App update received. Apk path: " + path);
            Log.d(TAG, "App update received. Changelog: " + mChangeLog);

            mListener.onUpdateFound(mLatestVersionName, mChangeLog, path);
        }
    }

    public boolean isEnabled() {
        return mSettingsManager.isEnabled();
    }

    public void setEnabled(boolean enabled) {
        mSettingsManager.setEnabled(enabled);
    }

    @Override
    public void onCheckError(Exception e) {
        mListener.onError(e);
    }

    @Override
    public void onDownloadError(Exception e) {
        mListener.onError(e);
    }

    public void installUpdate() {
        Helpers.installPackage(mContext, mSettingsManager.getApkPath());
    }

    public void enableUpdateCheck(boolean enable) {
        mSettingsManager.setMinIntervalMs(enable ? SettingsManager.CHECK_INTERVAL_DEFAULT : -1);
    }

    public boolean isUpdateCheckEnabled() {
        return mSettingsManager.getMinIntervalMs() > 0;
    }
}
