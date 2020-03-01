package com.liskovsoft.sharedutils.mylogger;

import android.util.Log;
import com.liskovsoft.sharedutils.BuildConfig;

class SystemLogger extends MyLogger {
    @Override
    public void d(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, msg);
        }
    }

    @Override
    public void i(String tag, String msg) {
        Log.i(tag, msg);
    }

    @Override
    public void w(String tag, String msg) {
        Log.w(tag, msg);
    }

    @Override
    public void e(String tag, String msg) {
        Log.e(tag, msg);
    }

    @Override
    public String getLogType() {
        return com.liskovsoft.sharedutils.mylogger.Log.LOG_TYPE_SYSTEM;
    }
}
