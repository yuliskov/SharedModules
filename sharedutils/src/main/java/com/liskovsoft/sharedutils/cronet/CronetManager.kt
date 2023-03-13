package com.liskovsoft.sharedutils.cronet

import android.content.Context
import org.chromium.net.CronetEngine

object CronetManager {
    private var engine: CronetEngine? = null

    @JvmStatic
    fun getEngine(context: Context): CronetEngine {
        if (engine == null) {
            //val wrapper = CronetEngineWrapper(context)
            //engine = wrapper.cronetEngine

            engine = CronetEngine.Builder(context)
                .enableQuic(true)
                .enableHttp2(true)
                .enableBrotli(true)
                .build()
        }

        return engine!!
    }
}