package com.ng.game;

import android.app.Activity;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.ng.syringe.load.ObjectFactoryUtil;
import com.ng.syringe.util.LogUtils;

/**
 * @author : jiangzhengnan.jzn@alibaba-inc.com
 * @creation : 2021/12/25
 * @description :
 * 通过hook方式可以直接来到第三关,无需特殊处理
 * todo 需要改为asm编织getResources，使得开发无感知
 */
public class NgGameLevelThreeActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_three);
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
