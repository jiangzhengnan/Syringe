package com.ng.demo;

import android.content.Context;
import android.content.res.Resources;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.ng.syringe.Syringe;

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
        Syringe.init(this);
        Syringe.instance().injectResources(this, super.getResources());
        return super.getResources();
    }

}

