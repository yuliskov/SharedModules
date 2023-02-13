package com.liskovsoft.sharedutils.prefs;

import android.annotation.SuppressLint;
import android.content.Context;
import com.liskovsoft.sharedutils.helpers.Helpers;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

final public class GlobalPreferences extends SharedPreferencesBase {
    public static final String PLAYLIST_TYPE_RECOMMENDATIONS = "playlist_type_recommendations";
    public static final String PLAYLIST_TYPE_SUBSCRIPTIONS = "playlist_type_subscriptions";
    public static final String PLAYLIST_TYPE_HISTORY = "playlist_type_history";
    public static final String PLAYLIST_TYPE_NONE = "playlist_type_none";
    @SuppressLint("StaticFieldLeak")
    public static GlobalPreferences sInstance;
    private static final String SHARED_PREFERENCES_NAME = GlobalPreferences.class.getName();
    private final static String MESSAGE_AUTH_BODY = "message_auth_body";
    private final static String MEDIA_SERVICE_REFRESH_TOKEN = "media_service_refresh_token";
    private final static String MEDIA_SERVICE_ACCOUNT_DATA = "media_service_account_data";
    private final static String MEDIA_SERVICE_DATA = "media_service_data";
    private static final String RECOMMENDED_PLAYLIST_TYPE = "recommended_playlist_type";
    private static final String PREFERRED_LANGUAGE_DATA = "preferred_language_data";
    private static final String PREFERRED_COUNTRY_DATA = "preferred_country_data";
    private static final String ENABLE_CHANNELS_SERVICE = "enable_channels_service";
    private static final String PREFER_IPV_4_DNS = "prefer_ipv4_dns";
    private static final String ENABLE_DNS_OVER_HTTPS = "enable_dns_over_https";
    private static final String VISITOR_COOKIE = "visitor_cookie";
    private static final String HIDE_SHORTS_FROM_SUBSCRIPTIONS = "hide_shorts_from_subscriptions";
    private static final String HIDE_STREAMS_FROM_SUBSCRIPTIONS = "hide_streams_from_subscriptions";
    private static final String HIDE_SHORTS_FROM_HOME = "hide_shorts_from_home";
    private static final String HIDE_SHORTS_FROM_HISTORY = "hide_shorts_from_history";
    private static final String HIDE_UPCOMING = "hide_upcoming";
    private static final String CONTENT_BLOCK_ALT_SERVER = "content_block_alt_server";
    private static final List<Runnable> sCallbacks = new CopyOnWriteArrayList<>(); // fix ConcurrentModificationException

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
        // Fix lost account after reboot bug
        if (sInstance == null) {
            sCallbacks.add(callback);
        } else {
            new Thread(callback).start(); // fix network on main thread exception
        }
    }

    public static boolean isInitialized() {
        return sInstance != null && sInstance.getContext() != null;
    }

    public static String getVisitorCookie() {
        return isInitialized() ? sInstance.getString(VISITOR_COOKIE, null) : null;
    }

    public static void setVisitorCookie(String visitorCookie) {
        if (isInitialized()) {
            sInstance.putString(VISITOR_COOKIE, visitorCookie);
        }
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
        return getString(RECOMMENDED_PLAYLIST_TYPE, PLAYLIST_TYPE_RECOMMENDATIONS);
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

    public void setPreferredLanguage(String langData) {
        putString(PREFERRED_LANGUAGE_DATA, langData);
    }

    public String getPreferredLanguage() {
        return getString(PREFERRED_LANGUAGE_DATA, null);
    }

    public void setPreferredCountry(String countryData) {
        putString(PREFERRED_COUNTRY_DATA, countryData);
    }

    public String getPreferredCountry() {
        return getString(PREFERRED_COUNTRY_DATA, null);
    }

    public void enableChannelsService(boolean enable) {
        putBoolean(ENABLE_CHANNELS_SERVICE, enable);
    }

    public boolean isChannelsServiceEnabled() {
        return getBoolean(ENABLE_CHANNELS_SERVICE, true);
    }

    public void preferIPv4Dns(boolean enable) {
        putBoolean(PREFER_IPV_4_DNS, enable);
    }

    public boolean isIPv4DnsPreferred() {
        return getBoolean(PREFER_IPV_4_DNS, false);
    }

    public void enableDnsOverHttps(boolean enable) {
        putBoolean(ENABLE_DNS_OVER_HTTPS, enable);
    }

    public boolean isDnsOverHttpsEnabled() {
        return getBoolean(ENABLE_DNS_OVER_HTTPS, false);
    }

    public void hideShortsFromHome(boolean enable) {
        putBoolean(HIDE_SHORTS_FROM_HOME, enable);
    }

    public boolean isHideShortsFromHomeEnabled() {
        return getBoolean(HIDE_SHORTS_FROM_HOME, false);
    }

    public void hideShortsFromSubscriptions(boolean enable) {
        putBoolean(HIDE_SHORTS_FROM_SUBSCRIPTIONS, enable);
    }

    public boolean isHideShortsFromSubscriptionsEnabled() {
        return getBoolean(HIDE_SHORTS_FROM_SUBSCRIPTIONS, true);
    }

    public void hideStreamsFromSubscriptions(boolean enable) {
        putBoolean(HIDE_STREAMS_FROM_SUBSCRIPTIONS, enable);
    }

    public boolean isHideStreamsFromSubscriptionsEnabled() {
        return getBoolean(HIDE_STREAMS_FROM_SUBSCRIPTIONS, false);
    }

    public void hideShortsFromHistory(boolean enable) {
        putBoolean(HIDE_SHORTS_FROM_HISTORY, enable);
    }

    public boolean isHideShortsFromHistoryEnabled() {
        return getBoolean(HIDE_SHORTS_FROM_HISTORY, false);
    }

    public void hideUpcoming(boolean enable) {
        putBoolean(HIDE_UPCOMING, enable);
    }

    public boolean isHideUpcomingEnabled() {
        return getBoolean(HIDE_UPCOMING, false);
    }

    public void enableContentBlockAltServer(boolean enable) {
        putBoolean(CONTENT_BLOCK_ALT_SERVER, enable);
    }

    public boolean isContentBlockAltServerEnabled() {
        return getBoolean(CONTENT_BLOCK_ALT_SERVER, false);
    }
}
