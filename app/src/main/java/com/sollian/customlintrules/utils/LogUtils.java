package com.sollian.customlintrules.utils;

import android.annotation.SuppressLint;

/**
 * @author lishouxian on 2019/2/22.
 */
public class LogUtils {
    public static void printStaceTrace(Throwable t) {
        if (t != null) {
            t.printStackTrace();
        }
    }
}
