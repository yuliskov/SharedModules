package com.liskovsoft.sharedutils.okhttp;

import okhttp3.OkHttpClient;
import okhttp3.Response;

import java.util.Map;

public class OkHttpHelpers {
    private static OkHttpManager sOkHttpManager;

    public static Response doOkHttpRequest(String url) {
        return getOkHttpManager().doOkHttpRequest(url);
    }

    public static Response doGetOkHttpRequest(String url, Map<String, String> headers) {
        return getOkHttpManager().doGetOkHttpRequest(url, headers);
    }

    public static Response doPostOkHttpRequest(String url, Map<String, String> headers, String postBody, String contentType) {
        return getOkHttpManager().doPostOkHttpRequest(url, headers, postBody, contentType);
    }

    public static Response doGetOkHttpRequest(String url) {
        return getOkHttpManager().doGetOkHttpRequest(url);
    }

    public static Response doHeadOkHttpRequest(String url) {
        return getOkHttpManager().doHeadOkHttpRequest(url);
    }

    /**
     * NOTE: default method is GET
     */
    public static Response doOkHttpRequest(String url, OkHttpClient client) {
        return getOkHttpManager().doOkHttpRequest(url, client);
    }

    /**
     * NOTE: default method is GET
     */
    public static Response doOkHttpRequest(String url, OkHttpClient client, Map<String, String> headers) {
        return getOkHttpManager().doOkHttpRequest(url, client, headers);
    }

    public static OkHttpClient.Builder setupBuilder(OkHttpClient.Builder builder) {
        return OkHttpClientHelper.setupBuilder(builder);
    }

    public static OkHttpClient getOkHttpClient() {
        return getOkHttpManager().getOkHttpClient();
    }

    private static OkHttpManager getOkHttpManager() {
        if (sOkHttpManager == null) {
            sOkHttpManager = OkHttpManager.instance();
        }

        return sOkHttpManager;
    }
}
