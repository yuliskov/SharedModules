package com.liskovsoft.appupdatechecker2;

import java.util.List;

import android.net.Uri;

public interface OnAppUpdateListener {
	void appUpdateStatus(boolean isLatestVersion, String latestVersionName, List<String> changelog, Uri[] downloadUris);
	boolean cancelPendingUpdate();
	boolean tryInstallPendingUpdate();
}