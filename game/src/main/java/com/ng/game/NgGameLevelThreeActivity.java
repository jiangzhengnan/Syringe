package com.ng.game;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.ng.syringe.Syringe;
import com.ng.syringe.util.LogUtils;

/**
 * @author : jiangzhengnan.jzn@alibaba-inc.com
 * @creation : 2021/12/25
 * @description :
 * 通过hook方式可以直接来到第三关,无需特殊处理，开发无感知
 */
public class NgGameLevelThreeActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.d("第三关 NgGameLevelThreeActivity onCreate");
        setContentView(R.layout.activity_game_three);
    }

    @Override
    public Resources getResources() {
        Syringe.instance().injectResources(this, super.getResources());
        return super.getResources();
    }


    //protected AssetManager mAssetManager;
    //protected Resources mResources;

    //    @Override
//    public Resources getResources() {
//        return mResources != null ? mResources : super.getResources();
//    }

//    protected void loadPluginResource(String apkPath) {
//        LogUtils.d("[加载资源] apkPath:" + apkPath);
//        try {
//            mAssetManager = ObjectFactoryUtil.make(this, AssetManager.class);
//            ObjectFactoryUtil.invokeMethod(this, mAssetManager, AssetManager.class.getName(),
//                    "addAssetPath", apkPath);
//            mResources = new Resources(mAssetManager,
//                    super.getResources().getDisplayMetrics(),
//                    super.getResources().getConfiguration());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
