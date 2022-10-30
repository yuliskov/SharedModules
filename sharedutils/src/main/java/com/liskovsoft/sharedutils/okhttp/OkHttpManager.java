package com.liskovsoft.sharedutils.okhttp;

import com.localebro.okhttpprofiler.OkHttpProfilerInterceptor;
import com.liskovsoft.sharedutils.BuildConfig;
import com.liskovsoft.sharedutils.mylogger.Log;
import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.TlsVersion;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class OkHttpManager {
    private static final String TAG = OkHttpManager.class.getSimpleName();
    private static final int NUM_TRIES = 3;
    private static OkHttpManager sInstance;
    private final OkHttpClient mClient;
    private final boolean mEnableProfilerWhenDebugging;

    private OkHttpManager(boolean enableProfilerWhenDebugging) {
        // Profiler could cause OutOfMemoryError when testing.
        // Also outputs to logcat tons of info.
        mEnableProfilerWhenDebugging = enableProfilerWhenDebugging;

        mClient = createOkHttpClient();
    }

    public static OkHttpManager instance() {
        return instance(true); // profiler is enabled by default
    }

    public static OkHttpManager instance(boolean enableProfilerWhenDebugging) {
        if (sInstance == null) {
            sInstance = new OkHttpManager(enableProfilerWhenDebugging);
        }

        return sInstance;
    }

    public Response doOkHttpRequest(String url) {
        return doOkHttpRequest(url, mClient);
    }

    public Response doGetOkHttpRequest(String url, Map<String, String> headers) {
        if (headers == null) {
            Log.d(TAG, "Headers are null... doing regular request...");
            return doGetOkHttpRequest(url, mClient);
        }

        return doGetOkHttpRequest(url, mClient, headers);
    }

    public Response doPostOkHttpRequest(String url, Map<String, String> headers, String postBody, String contentType) {
        return doPostOkHttpRequest(url, mClient, headers, postBody, contentType);
    }

    public Response doGetOkHttpRequest(String url) {
        return doGetOkHttpRequest(url, mClient);
    }

    public Response doHeadOkHttpRequest(String url) {
        return doHeadOkHttpRequest(url, mClient);
    }

    /**
     * NOTE: default method is GET
     */
    public Response doOkHttpRequest(String url, OkHttpClient client) {
        Request okHttpRequest = new Request.Builder()
                .url(url)
                .build();

        return doOkHttpRequest(client, okHttpRequest);
    }

    /**
     * NOTE: default method is GET
     */
    public Response doOkHttpRequest(String url, OkHttpClient client, Map<String, String> headers) {
        if (headers == null) {
            headers = new HashMap<>();
        }
        
        Request okHttpRequest = new Request.Builder()
                .url(url)
                .headers(Headers.of(headers))
                .build();

        return doOkHttpRequest(client, okHttpRequest);
    }

    private Response doPostOkHttpRequest(String url, OkHttpClient client, Map<String, String> headers, String body, String contentType) {
        if (headers == null) {
            headers = new HashMap<>();
        }

        Request okHttpRequest = new Request.Builder()
                .url(url)
                .headers(Headers.of(headers))
                .post(RequestBody.create(MediaType.parse(contentType), body))
                .build();

        return doOkHttpRequest(client, okHttpRequest);
    }

    private Response doGetOkHttpRequest(String url, OkHttpClient client, Map<String, String> headers) {
        Request okHttpRequest = new Request.Builder()
                .url(url)
                .headers(Headers.of(headers))
                .get()
                .build();

        return doOkHttpRequest(client, okHttpRequest);
    }

    private Response doGetOkHttpRequest(String url, OkHttpClient client) {
        Request okHttpRequest = new Request.Builder()
                .url(url)
                .get()
                .build();

        return doOkHttpRequest(client, okHttpRequest);
    }

    private Response doHeadOkHttpRequest(String url, OkHttpClient client) {
        Request okHttpRequest = new Request.Builder()
                .url(url)
                .head()
                .build();

        return doOkHttpRequest(client, okHttpRequest);
    }

    private Response doOkHttpRequest(OkHttpClient client, Request okHttpRequest) {
        if (client == null) {
            client = createOkHttpClient();
        }

        Response okHttpResponse = null;
        Exception lastEx = null;

        for (int tries = NUM_TRIES; tries > 0; tries--) {
            try {
                okHttpResponse = client.newCall(okHttpRequest).execute();
                if (!okHttpResponse.isSuccessful()) {
                    throw new IllegalStateException("Unexpected code " + okHttpResponse);
                }

                break; // no exception is thrown - job is done
            } catch (Exception ex) {
                //Log.e(TAG, ex.getMessage()); // network error, just return null
                okHttpResponse = null;
                lastEx = ex;
            }
        }

        if (lastEx != null && okHttpResponse == null) { // request failed
            lastEx.printStackTrace();
            Log.e(TAG, lastEx.getMessage());
        }

        return okHttpResponse;
    }

    public OkHttpClient createOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        // Profiler could cause OutOfMemoryError when testing.
        // Also outputs to logcat tons of info.
        if (BuildConfig.DEBUG && mEnableProfilerWhenDebugging) {
            builder.addInterceptor(new OkHttpProfilerInterceptor());
        }

        builder.addInterceptor(new RateLimitInterceptor());
        builder.addInterceptor(new UnzippingInterceptor());

        //configureToIgnoreCertificate(builder);

        return setupBuilder(builder).build();
    }

    //public OkHttpClient.Builder setupBuilder(OkHttpClient.Builder builder) {
    //    ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
    //            .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_1, TlsVersion.TLS_1_0)
    //            .cipherSuites(
    //                    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
    //                    CipherSuite.TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256,
    //                    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
    //                    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA)
    //            .build();
    //
    //    builder
    //        .connectTimeout(CONNECT_TIMEOUT_S, TimeUnit.SECONDS)
    //        .readTimeout(READ_TIMEOUT_S, TimeUnit.SECONDS)
    //        .writeTimeout(WRITE_TIMEOUT_S, TimeUnit.SECONDS)
    //        .connectionSpecs(Collections.singletonList(spec));
    //
    //    return enableTls12OnPreLollipop(builder);
    //}

    public OkHttpClient.Builder setupBuilder(OkHttpClient.Builder builder) {
        OkHttpCommons.setupConnectionFix(builder);
        OkHttpCommons.setupConnectionParams(builder);
        OkHttpCommons.configureToIgnoreCertificate(builder);

        return builder;
    }

    private void setupConnectionSpec(OkHttpClient.Builder builder) {
        ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
                .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_1, TlsVersion.TLS_1_0)
                .cipherSuites(
                        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                        CipherSuite.TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256,
                        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
                        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA)
                .build();

        builder.connectionSpecs(Collections.singletonList(spec));
    }

    public OkHttpClient getOkHttpClient() {
        return mClient;
    }
}
