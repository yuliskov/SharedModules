package com.liskovsoft.sharedutils.prefs;

import android.content.Context;
import com.liskovsoft.sharedutils.helpers.Helpers;

public class GlobalPreferences extends SharedPreferencesBase {
    public static GlobalPreferences sInstance;
    private static final String SHARED_PREFERENCES_NAME = GlobalPreferences.class.getName();
    private final static String MESSAGE_AUTH_BODY = "message_auth_body";
    private static final String RECOMMENDED_PLAYLIST_TYPE = "recommended_playlist_type";
    public static final String PLAYLIST_TYPE_RECOMMENDATIONS = "playlist_type_recommendations";
    public static final String PLAYLIST_TYPE_SUBSCRIPTIONS = "playlist_type_subscriptions";
    public static final String PLAYLIST_TYPE_HISTORY = "playlist_type_history";
    public static final String PLAYLIST_TYPE_NONE = "playlist_type_none";

    private GlobalPreferences(Context context) {
        super(context, SHARED_PREFERENCES_NAME);
    }

    public static GlobalPreferences instance(Context context) {
        if (sInstance == null) {
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

    public void setRecommendedPlaylistType(String type) {
        putString(RECOMMENDED_PLAYLIST_TYPE, type);
    }

    public String getRecommendedPlaylistType() {
        return getString(RECOMMENDED_PLAYLIST_TYPE, PLAYLIST_TYPE_SUBSCRIPTIONS);
    }
}
