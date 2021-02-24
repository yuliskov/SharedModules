package com.liskovsoft.sharedutils.prefs;

import android.content.Context;
import com.liskovsoft.sharedutils.helpers.Helpers;

import java.util.ArrayList;
import java.util.List;

public class GlobalPreferences extends SharedPreferencesBase {
    public static GlobalPreferences sInstance;
    private static final String SHARED_PREFERENCES_NAME = GlobalPreferences.class.getName();
    private final static String MESSAGE_AUTH_BODY = "message_auth_body";
    private final static String MEDIA_SERVICE_REFRESH_TOKEN = "media_service_refresh_token";
    private final static String MEDIA_SERVICE_ACCOUNT_DATA = "media_service_account_data";
    private final static String MEDIA_SERVICE_DATA = "media_service_data";
    private static final String RECOMMENDED_PLAYLIST_TYPE = "recommended_playlist_type";
    public static final String PLAYLIST_TYPE_RECOMMENDATIONS = "playlist_type_recommendations";
    public static final String PLAYLIST_TYPE_SUBSCRIPTIONS = "playlist_type_subscriptions";
    public static final String PLAYLIST_TYPE_HISTORY = "playlist_type_history";
    public static final String PLAYLIST_TYPE_NONE = "playlist_type_none";
    private static final List<Runnable> sCallbacks = new ArrayList<>();

    // For testing purposes
    public GlobalPreferences() {
    }

    private GlobalPreferences(Context context) {
        super(context, SHARED_PREFERENCES_NAME);
    }

    public static GlobalPreferences instance(Context context) {
        if (sInstance == null) {
            sInstance = new GlobalPreferences(context.getApplicationContext());

            for (Runnable callback : sCallbacks) {
                new Thread(callback).start(); // fix network on main thread exception
            }

            // make callbacks garbage collected
            sCallbacks.clear();
        }

        return sInstance;
    }

    public static void setOnInit(Runnable callback) {
        sCallbacks.add(callback);
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

    public void setMediaServiceRefreshToken(String token) {
        putString(MEDIA_SERVICE_REFRESH_TOKEN, token);
    }

    public String getMediaServiceRefreshToken() {
        return getString(MEDIA_SERVICE_REFRESH_TOKEN, null);
    }

    public void setMediaServiceAccountData(String data) {
        putString(MEDIA_SERVICE_ACCOUNT_DATA, data);
    }

    public String getMediaServiceAccountData() {
        return getString(MEDIA_SERVICE_ACCOUNT_DATA, null);
    }

    public void setMediaServiceData(String data) {
        putString(MEDIA_SERVICE_DATA, data);
    }

    public String getMediaServiceData() {
        return getString(MEDIA_SERVICE_DATA, null);
    }
}
