package com.liskovsoft.sharedutils.cronet

import android.content.Context
import org.chromium.net.CronetEngine
import org.chromium.net.impl.NativeCronetProvider

object CronetManager {
    private var engine: CronetEngine? = null

    @JvmStatic
    fun getEngine(context: Context): CronetEngine {
        if (engine == null) {
            //val wrapper = CronetEngineWrapper(context)
            //engine = wrapper.cronetEngine

            //engine = CronetEngine.Builder(context)
            //    .enableQuic(true)
            //    .enableHttp2(true)
            //    .enableBrotli(true)
            //    .build()

            engine = NativeCronetProvider(context)
                .createBuilder()
                .enableQuic(true)
                .enableHttp2(true)
                .enableBrotli(true)
                .build()
        }

        return engine!!
    }
}