package com.ng.syringe.load.proxy.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ng.syringe.util.LogUtils;

import java.lang.reflect.Constructor;

/**
 * @author : jiangzhengnan.jzn@alibaba-inc.com
 * @creation : 2021/12/28
 * @description :
 */
public class ProxyReceive extends BroadcastReceiver {
    String className;
    private BroadCastProxyInterface receiveObj;

    public ProxyReceive(String className, Context context) {
        this.className = className;
        //这里通过classname 得到class对象，然后
        try {
            Class<?> receiverClass = context.getClassLoader().loadClass(className);
            if (receiverClass == null) {
                LogUtils.d("获取代理的广播为空");
                return;
            }
            Constructor constructorReceiver = receiverClass.getConstructor(new Class[]{});
            receiveObj = (BroadCastProxyInterface) constructorReceiver.newInstance(new Object[]{});
            receiveObj.attach(context);
            LogUtils.d("获取代理的广播成功");
        } catch (Exception e) {
            LogUtils.d("获取代理的广播失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        receiveObj.onReceive(context, intent);
    }
}