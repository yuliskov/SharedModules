package com.liskovsoft.appupdatechecker2;

import java.util.List;

public interface AppUpdateCheckerListener {
    int ACTION_CANCEL = 0;
    int ACTION_INSTALL = 1;
    /**
     * Wait app reboot and then do install
     */
    int ACTION_POSTPONE = 2;
    /**
     * Callback fired when update is found and apk is downloaded and ready to install.
     * @param changelog items what is changed
     * @return action: {@link #ACTION_CANCEL}, {@link #ACTION_INSTALL}, {@link #ACTION_POSTPONE}
     */
    int onUpdateFound(List<String> changelog);
    void onError(Exception error);
}
