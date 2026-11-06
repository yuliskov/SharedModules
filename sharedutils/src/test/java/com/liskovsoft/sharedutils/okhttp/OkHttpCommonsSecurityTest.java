package com.liskovsoft.sharedutils.okhttp;

import android.os.Build.VERSION;
import okhttp3.OkHttpClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {19, 24, 28})
public class OkHttpCommonsSecurityTest {
    @Test
    public void connectionSetupKeepsLegacyTls12AndModernDefaults() throws Exception {
        OkHttpClient original = new OkHttpClient();

        OkHttpClient configured = OkHttpCommons.setupBuilder(original.newBuilder()).build();

        if (VERSION.SDK_INT < 21) {
            assertTrue(configured.sslSocketFactory() instanceof Tls12SocketFactory);
            try (SSLSocket socket = (SSLSocket) configured.sslSocketFactory().createSocket()) {
                assertTrue(Arrays.asList(socket.getEnabledProtocols()).contains("TLSv1.2"));
            }
        } else {
            assertSame(original.sslSocketFactory(), configured.sslSocketFactory());
        }
        assertSame(original.hostnameVerifier(), configured.hostnameVerifier());
    }

    @Test
    public void connectionSetupPreservesCallerHostnamePolicy() {
        HostnameVerifier rejectAll = (hostname, session) -> false;
        OkHttpClient original = new OkHttpClient.Builder().hostnameVerifier(rejectAll).build();

        OkHttpClient configured = OkHttpCommons.setupBuilder(original.newBuilder()).build();

        assertSame(rejectAll, configured.hostnameVerifier());
    }

    @Test
    public void legacyTrustManagerRejectsAnUntrustedEmptyChain() throws Exception {
        try {
            OkHttpCommons.systemTrustManager().checkServerTrusted(new X509Certificate[0], "RSA");
            fail("An empty certificate chain must not be trusted");
        } catch (CertificateException | IllegalArgumentException expected) {
            // The platform may reject the invalid chain before attempting certificate validation.
        }
    }
}
