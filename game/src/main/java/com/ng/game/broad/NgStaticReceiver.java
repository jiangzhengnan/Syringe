package com.ng.game.broad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ng.syringe.util.LogUtils;

/**
 * Created by Dou on 2019/7/28.
 */
public class NgStaticReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.d("收到了静态广播");
    }
}
