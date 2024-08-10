package com.liskovsoft.sharedutils.prefs;

import android.annotation.SuppressLint;
import android.content.Context;

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
    private final static String OAUTH2_ACCOUNT_DATA = "oauth2_account_data";
    private final static String MEDIA_SERVICE_DATA = "media_service_data";
    private static final String RECOMMENDED_PLAYLIST_TYPE = "recommended_playlist_type";
    private static final String PREFERRED_LANGUAGE_DATA = "preferred_language_data";
    private static final String PREFERRED_COUNTRY_DATA = "preferred_country_data";
    private static final String ENABLE_CHANNELS_SERVICE = "enable_channels_service";
    private static final String PREFER_IPV_4_DNS = "prefer_ipv4_dns";
    private static final String ENABLE_DNS_OVER_HTTPS = "enable_dns_over_https";
    private static final String VISITOR_COOKIE = "visitor_cookie";
    private static final String HIDE_SHORTS_FROM_SUBSCRIPTIONS = "hide_shorts_from_subscriptions";
    private static final String HIDE_SHORTS_FROM_TRENDING = "hide_shorts_from_trending";
    private static final String HIDE_STREAMS_FROM_SUBSCRIPTIONS = "hide_streams_from_subscriptions";
    private static final String HIDE_WATCHED_FROM_HOME = "hide_watched_from_home";
    private static final String HIDE_WATCHED_FROM_SUBSCRIPTIONS = "hide_watched_from_subscriptions";
    private static final String HIDE_SHORTS_FROM_HOME = "hide_shorts_from_home";
    private static final String HIDE_SHORTS_FROM_HISTORY = "hide_shorts_from_history";
    private static final String HIDE_SHORTS_FROM_CHANNEL = "hide_shorts_from_channel";
    private static final String HIDE_UPCOMING = "hide_upcoming";
    private static final String HIDE_UPCOMING_FROM_CHANNEL = "hide_upcoming_from_channel";
    private static final String HIDE_UPCOMING_FROM_HOME = "hide_upcoming_from_home";
    private static final String HIDE_UPCOMING_FROM_SUBSCRIPTIONS = "hide_upcoming_from_subscriptions";
    private static final String CONTENT_BLOCK_ALT_SERVER = "content_block_alt_server";
    private static final String EXTENDED_HLS_FORMATS_ENABLED = "extended_hls_formats_enabled";
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

    public void setRecommendedPlaylistType(String type) {
        putString(RECOMMENDED_PLAYLIST_TYPE, type);
    }

    public String getRecommendedPlaylistType() {
        return getString(RECOMMENDED_PLAYLIST_TYPE, PLAYLIST_TYPE_RECOMMENDATIONS);
    }

    public void setMediaServiceAccountData(String data) {
        putString(MEDIA_SERVICE_ACCOUNT_DATA, data);
    }

    public String getMediaServiceAccountData() {
        return getString(MEDIA_SERVICE_ACCOUNT_DATA, null);
    }

    public void setOAuth2AccountData(String data) {
        putString(OAUTH2_ACCOUNT_DATA, data);
    }

    public String getOAuth2AccountData() {
        return getString(OAUTH2_ACCOUNT_DATA, null);
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

    public void hideWatchedFromHome(boolean enable) {
        putBoolean(HIDE_WATCHED_FROM_HOME, enable);
    }

    public boolean isHideWatchedFromHomeEnabled() {
        return getBoolean(HIDE_WATCHED_FROM_HOME, false);
    }

    public void hideWatchedFromSubscriptions(boolean enable) {
        putBoolean(HIDE_WATCHED_FROM_SUBSCRIPTIONS, enable);
    }

    public boolean isHideWatchedFromSubscriptionsEnabled() {
        return getBoolean(HIDE_WATCHED_FROM_SUBSCRIPTIONS, false);
    }

    public void hideShortsFromChannel(boolean enable) {
        putBoolean(HIDE_SHORTS_FROM_CHANNEL, enable);
    }

    public boolean isHideShortsFromChannelEnabled() {
        return getBoolean(HIDE_SHORTS_FROM_CHANNEL, false);
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

    public void hideShortsFromTrending(boolean enable) {
        putBoolean(HIDE_SHORTS_FROM_TRENDING, enable);
    }

    public boolean isHideShortsFromTrendingEnabled() {
        return getBoolean(HIDE_SHORTS_FROM_TRENDING, true);
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
        return getBoolean(HIDE_SHORTS_FROM_HISTORY, true);
    }

    public void hideUpcomingFromChannel(boolean enable) {
        putBoolean(HIDE_UPCOMING_FROM_CHANNEL, enable);
    }

    public boolean isHideUpcomingFromChannelEnabled() {
        return getBoolean(HIDE_UPCOMING_FROM_CHANNEL, true);
    }

    public void hideUpcomingFromHome(boolean enable) {
        putBoolean(HIDE_UPCOMING_FROM_HOME, enable);
    }

    public boolean isHideUpcomingFromHomeEnabled() {
        return getBoolean(HIDE_UPCOMING_FROM_HOME, true);
    }

    public void hideUpcomingFromSubscriptions(boolean enable) {
        putBoolean(HIDE_UPCOMING_FROM_SUBSCRIPTIONS, enable);
    }

    public boolean isHideUpcomingFromSubscriptionsEnabled() {
        return getBoolean(HIDE_UPCOMING_FROM_SUBSCRIPTIONS, false);
    }

    public void enableContentBlockAltServer(boolean enable) {
        putBoolean(CONTENT_BLOCK_ALT_SERVER, enable);
    }

    public boolean isContentBlockAltServerEnabled() {
        return getBoolean(CONTENT_BLOCK_ALT_SERVER, false);
    }

    public void enableExtendedHlsFormats(boolean enable) {
        putBoolean(EXTENDED_HLS_FORMATS_ENABLED, enable);
    }

    public boolean isExtendedHlsFormatsEnabled() {
        return getBoolean(EXTENDED_HLS_FORMATS_ENABLED, false);
    }
}
