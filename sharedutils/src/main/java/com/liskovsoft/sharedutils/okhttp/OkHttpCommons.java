package com.liskovsoft.sharedutils.okhttp;

import android.os.Build.VERSION;
import com.liskovsoft.sharedutils.mylogger.Log;
import okhttp3.CipherSuite;
import okhttp3.ConnectionPool;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.TlsVersion;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class OkHttpCommons {
    private static final String TAG = OkHttpCommons.class.getSimpleName();
    private static final long CONNECT_TIMEOUT_S = 30;
    private static final long READ_TIMEOUT_S = 30;
    private static final long WRITE_TIMEOUT_S = 30;

    // This is nearly equal to the cipher suites supported in Chrome 51, current as of 2016-05-25.
    // All of these suites are available on Android 7.0; earlier releases support a subset of these
    // suites. https://github.com/square/okhttp/issues/1972
    private static final CipherSuite[] APPROVED_CIPHER_SUITES_ALT = new CipherSuite[] {
            // TLSv1.3
            CipherSuite.TLS_AES_128_GCM_SHA256,
            CipherSuite.TLS_AES_256_GCM_SHA384,
            CipherSuite.TLS_CHACHA20_POLY1305_SHA256,
            CipherSuite.TLS_AES_128_CCM_SHA256,
            CipherSuite.TLS_AES_256_CCM_8_SHA256,

            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
            CipherSuite.TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256,
            CipherSuite.TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256,

            // Note that the following cipher suites are all on HTTP/2's bad cipher suites list. We'll
            // continue to include them until better suites are commonly available. For example, none
            // of the better cipher suites listed above shipped with Android 4.4 or Java 7.
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
            CipherSuite.TLS_RSA_WITH_AES_128_GCM_SHA256,
            CipherSuite.TLS_RSA_WITH_AES_256_GCM_SHA384,
            CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA,
            CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA, // should be commented out?
            CipherSuite.TLS_RSA_WITH_3DES_EDE_CBC_SHA,

            // Change TLS fingerprint by altering default cipher list
            // From original fix
            CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,
            // From NewPipe Downloader
            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA
    };

    public static void setupConnectionParams(OkHttpClient.Builder okBuilder) {
        // Setup default timeout
        // https://stackoverflow.com/questions/39219094/sockettimeoutexception-in-retrofit
        okBuilder.connectTimeout(CONNECT_TIMEOUT_S, TimeUnit.SECONDS);
        okBuilder.readTimeout(READ_TIMEOUT_S, TimeUnit.SECONDS);
        okBuilder.writeTimeout(WRITE_TIMEOUT_S, TimeUnit.SECONDS);

        // Imitate 'keepAlive' = false
        // https://stackoverflow.com/questions/63047533/connection-pool-okhttp
        okBuilder.connectionPool(new ConnectionPool(10, READ_TIMEOUT_S, TimeUnit.SECONDS));
        //okBuilder.protocols(listOf(Protocol.HTTP_1_1));
    }

    /**
     * Fixing SSL handshake timed out (probably provider issues in some countries)
     */
    public static void setupConnectionFix(Builder okBuilder) {
        // Alter cipher list to create unique TLS fingerprint
        ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .cipherSuites(APPROVED_CIPHER_SUITES_ALT)
                .build();
        okBuilder.connectionSpecs(Arrays.asList(cs, ConnectionSpec.CLEARTEXT));
    }

    /**
     * Fixing SSL handshake timed out (probably provider issues in some countries)
     */
    public static void setupConnectionFixOrigin(Builder okBuilder) {
        // TLS 1.2 not supported on pre Lollipop (fallback to TLS 1.0)
        // Note, TLS 1.0 doesn't have SNI support. So, fix should work.
        if (VERSION.SDK_INT <= 19) {
            return;
        }

        ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
                .tlsVersions(TlsVersion.TLS_1_2)
                .cipherSuites(
                        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                        CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256
                )
                .build();
        okBuilder.connectionSpecs(Collections.singletonList(cs));
    }

    private OkHttpClient.Builder enableTls12OnPreLollipop(OkHttpClient.Builder builder) {
        //if (Build.VERSION.SDK_INT >= 16 && Build.VERSION.SDK_INT < 22) {
        //    return custom builder;
        //}

        try {
            SSLContext sc = SSLContext.getInstance("TLSv1.2");
            sc.init(null, null, null);
            builder.sslSocketFactory(new Tls12SocketFactory(sc.getSocketFactory()));

            ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .tlsVersions(TlsVersion.TLS_1_2)
                    .build();

            List<ConnectionSpec> specs = new ArrayList<>();
            specs.add(cs);
            specs.add(ConnectionSpec.COMPATIBLE_TLS);
            specs.add(ConnectionSpec.CLEARTEXT);

            builder.connectionSpecs(specs);
        } catch (Exception exc) {
            Log.e("OkHttpTLSCompat", "Error while setting TLS 1.2", exc);
        }

        return builder;
    }

    private OkHttpClient.Builder enableTls12OnPreLollipop2(OkHttpClient.Builder builder) {
        //if (Build.VERSION.SDK_INT >= 16 && Build.VERSION.SDK_INT < 22) {
        //    return custom builder;
        //}

        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:"
                        + Arrays.toString(trustManagers));
            }
            X509TrustManager trustManager = (X509TrustManager) trustManagers[0];

            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, new TrustManager[] { trustManager }, null);
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            builder.sslSocketFactory(sslSocketFactory, trustManager);

            ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .tlsVersions(TlsVersion.TLS_1_2)
                    .build();

            List<ConnectionSpec> specs = new ArrayList<>();
            specs.add(cs);
            specs.add(ConnectionSpec.COMPATIBLE_TLS);
            specs.add(ConnectionSpec.CLEARTEXT);

            builder.connectionSpecs(specs);
        } catch (Exception exc) {
            Log.e("OkHttpTLSCompat", "Error while setting TLS 1.2", exc);
        }

        return builder;
    }

    //Setting testMode configuration. If set as testMode, the connection will skip certification check
    private void configureToIgnoreCertificate(OkHttpClient.Builder builder) {
        Log.w(TAG, "Ignore Ssl Certificate");
        try {

            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
                                throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
                                throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        } catch (Exception e) {
            Log.w(TAG, "Exception while configuring IgnoreSslCertificate: " + e, e);
        }
    }
}
