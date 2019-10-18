package com.liskovsoft.sharedutils.prefs;

import android.content.Context;
import com.liskovsoft.sharedutils.helpers.Helpers;

public class GlobalPreferences extends SharedPreferencesBase {
    public static GlobalPreferences sInstance;
    private static final String SHARED_PREFERENCES_NAME = GlobalPreferences.class.getName();
    private final static String MESSAGE_AUTH_BODY = "message_auth_body";

    public GlobalPreferences(Context context) {
        super(context, SHARED_PREFERENCES_NAME);
    }

    public static GlobalPreferences instance(Context context) {
        if (sInstance == null){
            sInstance = new GlobalPreferences(context);
        }

        return sInstance;
    }

    public void setRawAuthData(String data) {
        if (Helpers.isAndroidTV(mContext)) {
            putString(MESSAGE_AUTH_BODY, data);
        }
    }

    public String getRawAuthData() {
        return getString(MESSAGE_AUTH_BODY, null);
    }
}
