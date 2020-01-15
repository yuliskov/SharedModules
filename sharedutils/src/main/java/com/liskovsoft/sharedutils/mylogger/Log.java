package com.liskovsoft.sharedutils.mylogger;

import android.content.Context;

public class Log {
    public static final String LOG_TYPE_FILE = "log_type_file";
    public static final String LOG_TYPE_SYSTEM = "log_type_system";

    private static MyLogger sLogger = new SystemLogger();

    public static void d(String tag, Object msg) {
        if (msg != null) {
            sLogger.d(tag, msg.toString());
        }
    }

    public static void i(String tag, Object msg) {
        if (msg != null) {
            sLogger.i(tag, msg.toString());
        }
    }

    public static void w(String tag, Object msg) {
        if (msg != null) {
            sLogger.w(tag, msg.toString());
        }
    }

    public static void e(String tag, Object msg) {
        if (msg != null) {
            sLogger.e(tag, msg.toString());
        }
    }

    public static void i(String tag, Object msg, Throwable ex) {
        i(tag, msg + " " + ex.getMessage());
    }


    public static void e(String tag, Object msg, Throwable ex) {
        if (msg != null && ex != null) {
            e(tag, msg + " " + ex.getMessage());
        }
    }

    public static void d(String tag, Object msg, Throwable ex) {
        d(tag, msg + " " + ex.getMessage());
    }

    public static void w(String tag, Object msg, Throwable ex) {
        w(tag, msg + " " + ex.getMessage());
    }

    /**
     * In case of file, flushes all data to disk
     */
    public static void flush() {
        sLogger.flush();
    }

    public static void init(Context context, String logType, String customLabel) {
        if (sLogger.getLogType().equals(logType)) {
            return;
        }

        switch (logType) {
            case LOG_TYPE_FILE:
                sLogger = new FileLogger(context, customLabel);
                break;
            case LOG_TYPE_SYSTEM:
                sLogger = new SystemLogger();
                break;
        }
    }

    public static String getLogType() {
        if (sLogger != null) {
            return sLogger.getLogType();
        }

        return LOG_TYPE_SYSTEM;
    }
}
