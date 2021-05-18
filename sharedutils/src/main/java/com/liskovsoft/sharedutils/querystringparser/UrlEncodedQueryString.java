package com.liskovsoft.sharedutils.querystringparser;

import androidx.annotation.NonNull;
import com.liskovsoft.sharedutils.helpers.Helpers;

import java.net.URI;
import java.net.URISyntaxException;

public class UrlEncodedQueryString implements UrlQueryString {
    private String mQueryPrefix;
    private UrlEncodedQueryStringBase mQueryString;
    private String mUrl;

    private UrlEncodedQueryString(String url) {
        if (url == null) {
            return;
        }

        mUrl = url;

        if (Helpers.isValidUrl(url)) {
            URI parsedUrl = getURI(url);
            mQueryPrefix = String.format("%s://%s%s", parsedUrl.getScheme(), parsedUrl.getHost(), parsedUrl.getPath());
            mQueryString = UrlEncodedQueryStringBase.parse(parsedUrl);
        } else {
            mQueryString = UrlEncodedQueryStringBase.parse(url);
        }
    }

    private URI getURI(String url) {
        if (url == null) {
            return null;
        }

        try {
            // Fix illegal character exception. E.g.
            // https://www.youtube.com/results?search_query=Джентльмены удачи
            // https://www.youtube.com/results?search_query=|FR|+Mrs.+Doubtfire
            // https://youtu.be/wTw-jreMgCk\ (last char isn't valid)
            return new URI(url.length() > 100 ? // OOM fix
                    url : url
                      .replace(" ", "+")
                      .replace("|", "%7C")
                      .replace("\\", "/")
            );
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static UrlEncodedQueryString parse(String url) {
        return new UrlEncodedQueryString(url);
    }

    @Override
    public void remove(String key) {
        mQueryString.remove(key);
    }

    @Override
    public String get(String key) {
        return mQueryString.get(key);
    }

    @Override
    public float getFloat(String key) {
        String val = get(key);
        return val != null ? Float.parseFloat(val) : 0;
    }

    @Override
    public void set(String key, String value) {
        mQueryString.set(key, value);
    }

    @Override
    public void set(String key, float value) {
        set(key, String.valueOf(value));
    }

    @Override
    public void set(String key, int value) {
        set(key, String.valueOf(value));
    }

    @NonNull
    @Override
    public String toString() {
        return mQueryPrefix != null ? String.format("%s?%s", mQueryPrefix, mQueryString) : mQueryString.toString();
    }

    /**
     * Check query string
     */
    @Override
    public boolean isValid() {
        if (mUrl == null) {
            return false;
        }

        return Helpers.matchAll(mUrl, "[^\\/?&]+=[^\\/&]+");
    }

    @Override
    public boolean isEmpty() {
        return mUrl == null || mUrl.isEmpty();
    }

    @Override
    public boolean contains(String key) {
        return mQueryString.contains(key);
    }
}
