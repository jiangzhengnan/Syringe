package com.ng.syringe;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.ng.syringe.download.DownloadHelper;
import com.ng.syringe.hook.SyringeHookComponent;
import com.ng.syringe.load.FixDexUtil;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author : jiangzhengnan.jzn@alibaba-inc.com
 * @creation : 2021/12/18
 * @description :
 */
public class Syringe {

    @NonNull
    private volatile SyringeHookComponent mHookComponent;

    private ClassLoader mClassLoader;

    @NonNull
    private Context mContext;

    private static final AtomicReference<Syringe> sReference = new AtomicReference<>();

    public static void init(@NonNull Context context) {
        if (sReference.get() == null) {
            sReference.set(new Syringe(context));
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
        this.mHookComponent = new SyringeHookComponent();
    }


    public void hotLoad(@NonNull Activity activity) {
        // 模拟下载插件
        DownloadHelper.fakeDownLoadPlug(activity);
        // 加载目录下的插件
        if (FixDexUtil.isGoingToFix(activity)) {
            FixDexUtil.loadFixedDex(activity);
        }
        // hook activity跳转流程
        mHookComponent.hookStartActivity(activity);
    }

    public Class<?> loadClass(String classname) throws ClassNotFoundException {
        if (mClassLoader == null) {
            mClassLoader = mContext.getClassLoader();
        }
        return mClassLoader.loadClass(classname);
    }

}
