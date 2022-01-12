package com.ng.syringe.load.hook.impl;

import android.app.Activity;

/**
 * @author : jiangzhengnan.jzn@alibaba-inc.com
 * @creation : 2022/01/10
 * @description :
 */
public interface IHookManager {
    void hookStartActivity();

    void hookReceivers(Activity activity, String path);

}
