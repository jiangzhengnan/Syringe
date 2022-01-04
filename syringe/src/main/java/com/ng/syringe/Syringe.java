package com.ng.syringe;

import android.app.Activity;

import com.ng.syringe.download.DownloadHelper;
import com.ng.syringe.load.FixDexUtil;

/**
 * @author : jiangzhengnan.jzn@alibaba-inc.com
 * @creation : 2021/12/18
 * @description :
 */
public class Syringe {

    private static volatile Syringe instance = null;

    public static Syringe getInstance(Activity activity) {
        if (instance == null) {
            synchronized (Syringe.class) {
                if (instance == null) {
                    instance = new Syringe(activity);
                }
            }
        }
        return instance;
    }

    public static Syringe get() {
        if (instance == null) {
            throw new IllegalStateException("Syringe 没有初始化");
        }
        return instance;
    }

    private Syringe(Activity activity) {
        this.mActivity = activity;
    }

    public void init() {
        DownloadHelper.fakeDownLoadPlug(mActivity);
        if (FixDexUtil.isGoingToFix(mActivity)) {
            FixDexUtil.loadFixedDex(mActivity);
        }
    }

    private ClassLoader mClassLoader;

    private Activity mActivity;

    /**
     * 热加载文件
     */
    public void loadDex() {

    }


    public Class<?> loadClass(String classname) throws ClassNotFoundException {
        if (mClassLoader == null) {
            return mActivity.getClassLoader().loadClass(classname);
        }
        return mClassLoader.loadClass(classname);
    }

}
