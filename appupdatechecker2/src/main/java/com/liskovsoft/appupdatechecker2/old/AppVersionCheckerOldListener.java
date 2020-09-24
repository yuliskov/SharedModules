package com.liskovsoft.appupdatechecker2.old;

import java.util.List;

import android.net.Uri;

public interface AppVersionCheckerOldListener {
	void onChangelogReceived(boolean isLatestVersion, String latestVersionName, List<String> changelog, Uri[] downloadUris);
	boolean cancelPendingUpdate();
	boolean tryInstallPendingUpdate();
}