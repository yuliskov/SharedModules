package com.liskovsoft.sharedutils.prefs;

import android.content.Context;
import com.liskovsoft.sharedutils.helpers.Helpers;

public class GlobalPreferences extends SharedPreferencesBase {
    public static GlobalPreferences sInstance;
    private static final String SHARED_PREFERENCES_NAME = GlobalPreferences.class.getName();
    private final static String MESSAGE_AUTH_BODY = "message_auth_body";
    private static final String SUBS_CHANNEL_ID = "subs_channel_id";

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

    public void setSubsChannelId(long channelId) {
        putLong(SUBS_CHANNEL_ID, channelId);
    }

    public long getSubsChannelId() {
        return getLong(SUBS_CHANNEL_ID, -1);
    }
}
