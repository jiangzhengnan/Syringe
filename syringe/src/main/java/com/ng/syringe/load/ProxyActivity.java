package com.ng.syringe.load;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ng.syringe.R;
import com.ng.syringe.util.LogUtils;


/**
 * 被代理器完全支配的activity
 *
 * @see ActivityProxyAbs
 */

public abstract class ProxyActivity extends Activity {
    ActivityProxyAbs proxy;
    protected AssetManager mAssetManager;
    protected Resources mResources;
    protected Resources.Theme mTheme;
    protected String mPackageName;

    protected String mTargetActivityClassName;
    protected String mTargetResPath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadPluginResource(mTargetResPath);
        LogUtils.d("传入的 res path: " + mTargetResPath);
        LogUtils.d("传入的 activity: " + mTargetActivityClassName);
        proxy = ObjectFactory.make(mTargetActivityClassName, this);
        if (proxy != null) {
            LogUtils.d("创建" + mTargetActivityClassName + "成功");
            proxy.onCreate(savedInstanceState);
        } else {
            setContentView(R.layout.activity_empty);
        }
    }

//    protected abstract String targetActivityClassName();
//
//    protected abstract String targetResPath();


    /**
     * 1.重新创建一个AssetManager资源管理器，通过反射调用addAssetPath()方法，可以加载插件apk中的资源。
     * <br/>
     * 2.依赖第一步创建的AssetManager，重新创建一个Resources对象，该Resources对象包含了插件apk中的资源。
     * <br/>
     * 3.插件apk中的资源是通过Context.getResources()来获取的，因此需要重写Context的getResources()方法，返回前面创建的Resources对象。
     *
     * @param apkPath 插件路径
     */
    protected void loadPluginResource(String apkPath) {
        LogUtils.d("[加载资源] apkPath:" + apkPath);
        try {
            mAssetManager = ObjectFactory.make(AssetManager.class);
            ObjectFactory.invokeMethod(mAssetManager, AssetManager.class.getName(),
                    "addAssetPath", apkPath);
            mResources = new Resources(mAssetManager,
                    super.getResources().getDisplayMetrics(),
                    super.getResources().getConfiguration());
            PackageManager packageManager = super.getPackageManager();
            PackageInfo info = packageManager.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
            if (info != null) {
                ApplicationInfo appInfo = info.applicationInfo;
                mPackageName = appInfo.packageName;
                LogUtils.d("[加载资源] packageName:" + mPackageName);
            }
            mTheme = mResources.newTheme();
            mTheme.setTo(super.getTheme());
        } catch (Exception e) {
            e.printStackTrace();
        }

        int result = SplitResUtils.getId("R.layout.activity_game_one", mResources, mPackageName);
        LogUtils.d("尝试从" + mPackageName + "找结果:" + result);
    }

    @Override
    public AssetManager getAssets() {
        return mAssetManager != null ? mAssetManager : super.getAssets();
    }

    @Override
    public Resources getResources() {
        return mResources != null ? mResources : super.getResources();
    }

    @Override
    public Resources.Theme getTheme() {
        return mTheme != null ? mTheme : super.getTheme();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (proxy != null) {
            proxy.onStart();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (proxy != null) {
            proxy.onBackPressed();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (proxy != null && proxy.onKeyDown(keyCode, event)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (proxy != null && proxy.onKeyUp(keyCode, event)) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (proxy != null && proxy.onKeyLongPress(keyCode, event)) {
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (proxy != null) {
            proxy.onResume();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (proxy != null) {
            proxy.onNewIntent(intent);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (proxy != null) {
            proxy.onRestart();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (proxy != null) {
            proxy.onPause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (proxy != null) {
            proxy.onStop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (proxy != null) {
            proxy.onDestroy();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (proxy != null) {
            proxy.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (proxy != null) {
            proxy.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
