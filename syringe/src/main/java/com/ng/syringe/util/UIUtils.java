package com.ng.syringe.util;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.Nullable;

/**
 * @author : jiangzhengnan.jzn@alibaba-inc.com
 * @creation : 2021/12/28
 * @description :
 */
public class UIUtils {

    public static void showToast(@Nullable Context context, @Nullable String str) {
        if (context != null && str != null) {
            Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
        }
    }
}
