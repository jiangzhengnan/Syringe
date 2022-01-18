package com.ng.syringe;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ng.syringe.download.SyringeDownLoadComponent;
import com.ng.syringe.load.ObjectFactoryUtil;
import com.ng.syringe.load.SyringeLoadComponent;
import com.ng.syringe.util.LogUtils;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author : jiangzhengnan.jzn@alibaba-inc.com
 * @creation : 2021/12/18
 * @description :
 * 统一收束入口
 */
public class Syringe {

    @NonNull
    private final SyringeLoadComponent mLoadComponent;

    @NonNull
    private final SyringeDownLoadComponent mDownLoadComponent;

    @NonNull
    private final Context mContext;

    private static final AtomicReference<Syringe> sReference = new AtomicReference<>();

    public static void init(@NonNull Context context) {
        if (sReference.get() == null) {
            sReference.set(new Syringe(context));
            instance().attachBaseContext(context);
        }
    }

    public static Syringe instance() {
        if (sReference.get() == null) {
            throw new RuntimeException("Syringe haven't init");
        }
        return sReference.get();
    }

    private Syringe(@NonNull Context context) {
        this.mContext = context;
        this.mLoadComponent = new SyringeLoadComponent();
        this.mDownLoadComponent = new SyringeDownLoadComponent();
    }

    public void attachBaseContext(@NonNull Context context) {
        // 下载插件
        mDownLoadComponent.fakeDownLoadPlug(context, null);
        // 安装插件
        mLoadComponent.loadPlug(context, mDownLoadComponent.getDexDirFilePath(mContext));
    }

    protected AssetManager mAssetManager;
    protected Resources mResources;

    /**
     * 加载dex到Resources中
     */
    public void loadResources(String dexPath) {
        LogUtils.d("[加载资源] dexPath:" + dexPath);
        try {
            mAssetManager = ObjectFactoryUtil.make(mContext, AssetManager.class);
            ObjectFactoryUtil.invokeMethod(mContext, mAssetManager, AssetManager.class.getName(),
                    "addAssetPath", dexPath);
            mResources = new Resources(mAssetManager,
                    mContext.getResources().getDisplayMetrics(),
                    mContext.getResources().getConfiguration());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 注入Resources
     */
    public void injectResources(@Nullable Activity activity,@Nullable Resources resources) {

    }

    //todo old 逻辑 改为自动注入？
    /**
     * 1.hook aty 启动流程
     * 2.代理注册广播
     */
    public void hookActivity(@NonNull Activity activity) {
        mLoadComponent.hookActivity(activity);
    }
}
