package com.liskovsoft.sharedutils.cronet

import android.content.Context
import com.liskovsoft.sharedutils.mylogger.Log
import java.io.File
import org.chromium.net.CronetEngine
import org.chromium.net.ExperimentalCronetEngine
import org.chromium.net.impl.NativeCronetProvider

object CronetManager {
    private val TAG = CronetManager::class.java.simpleName
    private var engine: CronetEngine? = null

    @JvmStatic
    fun getEngine(context: Context): CronetEngine? {
        if (engine == null) {
            //val wrapper = CronetEngineWrapper(context)
            //engine = wrapper.cronetEngine

            //engine = CronetEngine.Builder(context)
            //    .enableQuic(true)
            //    .enableHttp2(true)
            //    .enableBrotli(true)
            //    .build()

            try {
                val cacheDir = File(context.cacheDir, "StCronet")
                cacheDir.mkdirs()

                val builder = NativeCronetProvider(context).createBuilder()

                builder
                    .enableQuic(true)
                    .enableHttp2(true)
                    .enableBrotli(true)
                    .setStoragePath(cacheDir.absolutePath)
                    .enableHttpCache(CronetEngine.Builder.HTTP_CACHE_DISK_NO_HTTP, 2 * 1024 * 1024)
                    //.addQuicHint("youtube.com", 80, 80)

                // Do these tweaks have negative side effects?
                //if (builder is ExperimentalCronetEngine.Builder) {
                //    val experimentalOptions = """
                //        {
                //            "AsyncResolver": {
                //                "enable_async_resolver": true
                //            },
                //            "StaleDNS": {
                //                "enable": true,
                //                "max_expired_time_ms": 86400000,
                //                "max_stale_uses": 5,
                //                "persist_to_disk": false,
                //                "use_stale_on_name_not_resolved": true
                //            },
                //            "QUIC": {
                //                "delay_tcp_race": true,
                //                "estimate_initial_rtt": true,
                //                "max_server_configs_stored_in_properties": 8,
                //                "idle_connection_timeout_seconds": 30,
                //                "goaway_sessions_on_ip_change": true,
                //                "initial_rtt_for_handshake_milliseconds": 100,
                //                "allow_other_network_reuse": true
                //            }
                //        }
                //    """.trimIndent()
                //
                //    builder.setExperimentalOptions(experimentalOptions)
                //}

                engine = builder.build()
            } catch (e: UnsatisfiedLinkError) {
                // Fatal Exception: java.lang.UnsatisfiedLinkError
                // Cannot load library: soinfo_relocate(linker.cpp:982): cannot locate symbol "getauxval" referenced by "libcronet.101.0.4951.41.so".
                e.printStackTrace()
                Log.e(TAG, e.message)
            }
        }

        return engine
    }
}
