package com.ng.game.broad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ng.syringe.util.LogUtils;

public class NgStaticReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.d(context.getClass().getName() + "收到了静态广播");
    }
}
