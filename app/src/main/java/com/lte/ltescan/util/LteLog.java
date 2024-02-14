package com.lte.ltescan.util;

import android.util.Log;

public class LteLog {

    private static final boolean LOGGING = false;

    public static void v(String tag, String message) {
        if (LOGGING) {
            Log.v(tag, message);
        }
    }

    public static void i(String tag, String message) {
        if (LOGGING) {
            Log.i(tag, message);
        }
    }

    public static void d(String tag, String message) {
        if (LOGGING) {
            Log.d(tag, message);
        }
    }

    public static void e(String tag, String message, Throwable throwable) {
        if (LOGGING) {
            Log.e(tag, message, throwable);
        }
    }
}
