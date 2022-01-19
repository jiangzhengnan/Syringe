package com.ng.syringe.load;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ng.syringe.load.hook.HookLoadManager;
import com.ng.syringe.load.resources.ResourcesLoadManager;

/**
 * @author : jiangzhengnan.jzn@alibaba-inc.com
 * @creation : 2022/01/09
 * @description :
 * 加载组件
 */
public class SyringeLoadComponent {
    private final Context mContext;

    private HookLoadManager mHookLoadManager;

    private ResourcesLoadManager mResLoadManager;

    public SyringeLoadComponent(Context context) {
        this.mContext = context;
        this.mResLoadManager = new ResourcesLoadManager(context);
        this.mHookLoadManager = new HookLoadManager(context);
    }

    /**
     * classLoader 插件加载
     */
    public void loadPlug(@NonNull Context context, @NonNull String plugPath) {
        mHookLoadManager.loadPlug(context, plugPath);
    }

    /**
     * hook Activity 启动流程
     * hook 广播动态注册
     */
    public void hookActivity(@NonNull Activity activity) {
        mHookLoadManager.hookActivity(activity);
    }

    /**
     * 插入dex到资源集合
     */
    public void loadResources(@NonNull String dexPath) {
        mResLoadManager.loadResources(dexPath);
    }

    /**
     * 注入资源
     */
    public void injectResources(@Nullable Context context, @Nullable Resources resources) {
        mResLoadManager.injectResources(context, resources);
    }
}
