package com.ng.syringe.util;

import android.util.Log;

/**
 * @author : jiangzhengnan.jzn
 * @creation : 2021/12/15
 * @description :
 */
public class LogUtils {
    private static final String TAG = "nangua";

    public static void d(String s) {
        Log.d(TAG, s);
    }

    public static void d(String tag, String s) {
        Log.d(TAG + " " + tag, s);
    }

    public static void e(Exception e) {
        Log.d(TAG, "---异常---");
        Log.d(TAG, e.getMessage());
    }

    public static void e(String tag, Exception e) {
        Log.d(TAG + " " + tag, "---异常---");
        Log.d(TAG + " " + tag, e.getMessage());
    }

}
