package com.liskovsoft.sharedutils.okhttp;

import okhttp3.OkHttpClient;
import okhttp3.Response;

import java.util.Map;

public class OkHttpHelpers {
    private static final OkHttpManager mOkHttpManager = OkHttpManager.instance();

    public static Response doOkHttpRequest(String url) {
        return mOkHttpManager.doOkHttpRequest(url);
    }

    public static Response doGetOkHttpRequest(String url, Map<String, String> headers) {
        return mOkHttpManager.doGetOkHttpRequest(url, headers);
    }

    public static Response doPostOkHttpRequest(String url, Map<String, String> headers, String postBody, String contentType) {
        return mOkHttpManager.doPostOkHttpRequest(url, headers, postBody, contentType);
    }

    public static Response doGetOkHttpRequest(String url) {
        return mOkHttpManager.doGetOkHttpRequest(url);
    }

    public static Response doHeadOkHttpRequest(String url) {
        return mOkHttpManager.doHeadOkHttpRequest(url);
    }

    /**
     * NOTE: default method is GET
     */
    public static Response doOkHttpRequest(String url, OkHttpClient client) {
        return mOkHttpManager.doOkHttpRequest(url, client);
    }

    /**
     * NOTE: default method is GET
     */
    public static Response doOkHttpRequest(String url, OkHttpClient client, Map<String, String> headers) {
        return mOkHttpManager.doOkHttpRequest(url, client, headers);
    }

    public static OkHttpClient createOkHttpClient() {
        return mOkHttpManager.createOkHttpClient();
    }

    public static OkHttpClient.Builder setupBuilder(OkHttpClient.Builder builder) {
        return mOkHttpManager.setupBuilder(builder);
    }

    public static OkHttpClient getOkHttpClient() {
        return mOkHttpManager.getOkHttpClient();
    }
}
