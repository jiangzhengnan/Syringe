package com.ng.demo;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.ng.syringe.Syringe;
import com.ng.syringe.load.ObjectFactoryUtil;
import com.ng.syringe.util.LogUtils;

/**
 * @author : jiangzhengnan.jzn@alibaba-inc.com
 * @creation : 2021/12/18
 * @description :
 */
public class MyApplication extends MultiDexApplication {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
        Syringe.init(this);
    }

    @Override
    public Resources getResources() {
        return loadPluginResource(super.getResources());
    }

    /**
     * ● 合并式：addAssetPath 时加入所有插件和主工程的路径。
     * ● 独立式：各个插件只添加自己 APK 路径。
     * 因为这里的Resources是宿主工程的，所以只能使用new出来的res去加载资源。不然找不到插件中的资源？
     */
    protected Resources loadPluginResource(Resources preResources) {
        String apkPath = "/storage/emulated/0/AAAAA/game-debug.apk";
        LogUtils.d("[加载资源] apkPath:" + apkPath);
        Resources newResources = null;
        try {
            AssetManager mAssetManager = ObjectFactoryUtil.make(this, AssetManager.class);
            ObjectFactoryUtil.invokeMethod(this, mAssetManager, AssetManager.class.getName(),
                    "addAssetPath", apkPath);
            newResources = new Resources(mAssetManager,
                    super.getResources().getDisplayMetrics(),
                    super.getResources().getConfiguration());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newResources == null ? preResources : newResources;
    }
}

