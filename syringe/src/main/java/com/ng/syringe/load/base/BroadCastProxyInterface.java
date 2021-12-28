package com.ng.syringe.load.base;

import android.content.Context;
import android.content.Intent;

/**
 * @author : jiangzhengnan.jzn@alibaba-inc.com
 * @creation : 2021/12/28
 * @description :
 */
public interface BroadCastProxyInterface {

    void attach(Context context);

    void onReceive(Context context, Intent intent);

}
