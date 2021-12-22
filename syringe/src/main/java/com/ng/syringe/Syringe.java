package com.ng.syringe;

import android.content.Context;

/**
 * @author : jiangzhengnan.jzn@alibaba-inc.com
 * @creation : 2021/12/18
 * @description :
 */
public class Syringe {

    private static volatile Syringe instance = null;

    public static Syringe getInstance(Context context) {
        if (instance == null) {
            synchronized (Syringe.class) {
                if (instance == null) {
                    instance = new Syringe(context);
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

    private Syringe(Context context) {
    }

    public void init() {

    }

    private ClassLoader mClassLoader;

    private Context mContext;

    public Context getContext() {
        return mContext;
    }

    /**
     * 热加载文件
     */
    public void loadDex() {

    }


    public Class<?> loadClass(String classname) throws ClassNotFoundException {
        if (mClassLoader == null) {
            return mContext.getClassLoader().loadClass(classname);
        }
        return mClassLoader.loadClass(classname);
    }

}
