package com.ng.syringe;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.ng.syringe.download.SyringeDownLoadComponent;
import com.ng.syringe.load.SyringeLoadComponent;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author : jiangzhengnan.jzn@alibaba-inc.com
 * @creation : 2021/12/18
 * @description :
 * 统一收束入口
 */
public class Syringe {

    @NonNull
    private volatile SyringeLoadComponent mLoadComponent;

    @NonNull
    private volatile SyringeDownLoadComponent mDownLoadComponent;

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
        this.mLoadComponent = new SyringeLoadComponent();
        this.mDownLoadComponent = new SyringeDownLoadComponent();
    }

    public void loadPlug(@NonNull Activity activity) {
        // 下载插件
        mDownLoadComponent.fakeDownLoadPlug(activity, null);
        // 安装插件
        mLoadComponent.loadPlug(activity, mDownLoadComponent.getDexDirFilePath(mContext));
    }

    public Class<?> loadClass(String classname) throws ClassNotFoundException {
        return mContext.getClassLoader().loadClass(classname);
    }

}
