package com.liskovsoft.sharedutils.okhttp

import com.liskovsoft.sharedutils.BuildConfig
import com.liskovsoft.sharedutils.helpers.Helpers
import com.liskovsoft.sharedutils.okhttp.DohProviders.buildGoogle
import com.liskovsoft.sharedutils.okhttp.interceptors.RateLimitInterceptor
import com.liskovsoft.sharedutils.okhttp.interceptors.UnzippingInterceptor
import com.localebro.okhttpprofiler.OkHttpProfilerInterceptor
import okhttp3.Dns
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.net.Inet4Address
import java.net.InetAddress

object OkHttpClientHelper {
    @JvmStatic
    var enableProfiler: Boolean = true

    @JvmStatic
    fun createOkHttpClient(): OkHttpClient {
        val okBuilder = OkHttpClient.Builder()

        setupBuilder(okBuilder)

        var client = okBuilder.build()

        //if (GlobalPreferences.sInstance != null && GlobalPreferences.sInstance.isDnsOverHttpsEnabled()) {
        //    client = wrapDnsOverHttps(client);
        //}

        // SSLHandshakeException on Android 4 (sslv3 alert handshake failure)
        // https://stackoverflow.com/questions/49980508/okhttp-sslhandshakeexception-ssl-handshake-aborted-failure-in-ssl-library-a-pro/49981435#49981435
        //if (Build.VERSION.SDK_INT > 19) {
        //    client = wrapDnsOverHttps(client)
        //}

        return client
    }

    @JvmStatic
    fun setupBuilder(okBuilder: OkHttpClient.Builder): OkHttpClient.Builder {
        //if (GlobalPreferences.sInstance != null && GlobalPreferences.sInstance.isIPv4DnsPreferred()) {
        //    // Cause hangs and crashes (especially on Android 8 devices or Dune HD)
        //    preferIPv4Dns(okBuilder);
        //}
        OkHttpCommons.setupConnectionFix(okBuilder)
        OkHttpCommons.setupConnectionParams(okBuilder)
        OkHttpCommons.configureToIgnoreCertificate(okBuilder)
        OkHttpCommons.fixStreamResetError(okBuilder)
        addCommonHeaders(okBuilder)
        enableDecompression(okBuilder)
        //enableRateLimiter(okBuilder)

        //disableCache(okBuilder);
        debugSetup(okBuilder)

        return okBuilder
    }

    private fun disableCache(okBuilder: OkHttpClient.Builder) {
        // Disable cache (could help with dlfree error on Eltex)
        // Spoiler: no this won't help with dlfree error on Eltex
        okBuilder.cache(null)
    }

    private fun addCommonHeaders(builder: OkHttpClient.Builder) {
        builder.addInterceptor { chain: Interceptor.Chain ->
            val requestBuilder = chain.request().newBuilder()
            requestBuilder.header("User-Agent", DefaultHeaders.APP_USER_AGENT)

            // Enable compression in production
            requestBuilder.header("Accept-Encoding", DefaultHeaders.ACCEPT_ENCODING_DEFAULT)

            // Emulate browser request
            //requestBuilder.header("Connection", "keep-alive");
            //requestBuilder.header("Cache-Control", "max-age=0");
            requestBuilder.header("Referer", "https://www.youtube.com/tv")
            chain.proceed(requestBuilder.build())
        }
    }

    /**
     * Checks that response is compressed and do uncompress if needed.
     */
    private fun enableDecompression(builder: OkHttpClient.Builder) {
        // Add gzip/deflate/br support
        //builder.addInterceptor(BrotliInterceptor.INSTANCE);
        builder.addInterceptor(UnzippingInterceptor())
    }

    private fun enableRateLimiter(builder: OkHttpClient.Builder) {
        builder.addInterceptor(RateLimitInterceptor())
    }

    private fun debugSetup(okBuilder: OkHttpClient.Builder) {
        if (BuildConfig.DEBUG) {
            // Profiler could cause OutOfMemoryError when testing.
            // Also outputs to logcat tons of info.
            // If you enable it to all requests - expect slowdowns.
            if (enableProfiler) {
                addProfiler(okBuilder)
            }
            addLogger(okBuilder)
        }
    }

    private fun addProfiler(okBuilder: OkHttpClient.Builder) {
        okBuilder.addInterceptor(OkHttpProfilerInterceptor())
    }

    private fun addLogger(okBuilder: OkHttpClient.Builder) {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        okBuilder.addInterceptor(logging)
    }

    private fun preferIPv4Dns(okBuilder: OkHttpClient.Builder) {
        okBuilder.dns(OkHttpDNSSelector(OkHttpDNSSelector.IPvMode.IPV4_FIRST))
        //okBuilder.dns(new PreferIpv4Dns());
    }

    private fun forceIPv4Dns(okBuilder: OkHttpClient.Builder) {
        okBuilder.dns { hostname: String ->
            val lookup = Dns.SYSTEM.lookup(hostname)
            val filter = Helpers.filter(
                lookup
            ) { value: InetAddress? -> value is Inet4Address }
            filter ?: lookup
        }
    }

    /**
     * Usage: `OkHttpClient newClient = wrapDns(client)`<br></br>
     * https://github.com/square/okhttp/blob/master/okhttp-dnsoverhttps/src/test/java/okhttp3/dnsoverhttps/DohProviders.java
     */
    private fun wrapDnsOverHttps(client: OkHttpClient): OkHttpClient {
        return client.newBuilder().dns(buildGoogle(client)).build()
    }
}