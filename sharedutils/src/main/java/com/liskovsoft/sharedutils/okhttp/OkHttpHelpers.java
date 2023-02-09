package com.liskovsoft.sharedutils.okhttp;

import okhttp3.OkHttpClient;
import okhttp3.Response;

import java.util.Map;

public class OkHttpHelpers {
    private static OkHttpManager sOkHttpManager;

    public static Response doRequest(String url) {
        return getOkHttpManager().doRequest(url);
    }

    public static Response doGetRequest(String url, Map<String, String> headers) {
        return getOkHttpManager().doGetRequest(url, headers);
    }

    public static Response doPostRequest(String url, Map<String, String> headers, String postBody, String contentType) {
        return getOkHttpManager().doPostRequest(url, headers, postBody, contentType);
    }

    public static Response doGetRequest(String url) {
        return getOkHttpManager().doGetRequest(url);
    }

    public static Response doHeadRequest(String url) {
        return getOkHttpManager().doHeadRequest(url);
    }

    /**
     * NOTE: default method is GET
     */
    public static Response doRequest(String url, OkHttpClient client) {
        return getOkHttpManager().doRequest(url, client);
    }

    /**
     * NOTE: default method is GET
     */
    public static Response doRequest(String url, OkHttpClient client, Map<String, String> headers) {
        return getOkHttpManager().doRequest(url, client, headers);
    }

    public static OkHttpClient.Builder setupBuilder(OkHttpClient.Builder builder) {
        return OkHttpCommons.setupBuilder(builder);
    }

    public static OkHttpClient getClient() {
        return getOkHttpManager().getClient();
    }

    private static OkHttpManager getOkHttpManager() {
        if (sOkHttpManager == null) {
            sOkHttpManager = OkHttpManager.instance();
        }

        return sOkHttpManager;
    }
}
