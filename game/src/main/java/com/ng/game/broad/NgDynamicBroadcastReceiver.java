package com.ng.game.broad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ng.syringe.load.proxy.base.BroadCastProxyInterface;
import com.ng.syringe.util.LogUtils;
import com.ng.syringe.util.UIUtils;

/**
 * @author : jiangzhengnan.jzn@alibaba-inc.com
 * @creation : 2021/12/28
 * @description :
 * 动态广播
 */
public class NgDynamicBroadcastReceiver extends BroadcastReceiver implements BroadCastProxyInterface {

    @Override
    public void attach(Context context) {
        UIUtils.showToast(context, "broadcast bind success in " + context.getPackageName());
        LogUtils.d("broadcast bind success in " + context.getPackageName());
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        UIUtils.showToast(context, "on receive broadcast");
        LogUtils.d("on receive broadcast");
    }
}
