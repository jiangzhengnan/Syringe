package com.ng.syringe.load.resources;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.ng.syringe.util.HiddenApiReflection;
import com.ng.syringe.util.LogUtils;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author : jiangzhengnan.jzn@alibaba-inc.com
 * @creation : 2022/01/19
 * @description :
 * 资源加载管理器
 * 兼容性适配问题参考(copy)Qigsaw
 */
public class ResourcesLoadManager {
    private static final String TAG = "[ResourcesLoadManager]";

    @NonNull
    private final Context mContext;

    private List<String> mLoadedResPathList;

    //对象锁
    private static final Object sLock = new Object();

    public ResourcesLoadManager(@NonNull Context context) {
        this.mContext = context;
        this.mLoadedResPathList = new ArrayList<>();
    }

    /**
     * 获取已经加载过的资源路径
     */
    public static List<String> getLoadedResourcesPath(Resources resources) {
        List<String> loadedResPathList = new ArrayList<>();
        try {
            AssetManager assetManager = resources.getAssets();
            if (Build.VERSION.SDK_INT >= 28) {
                Object[] apkAssets = (Object[]) HiddenApiReflection.findMethod(assetManager, "getApkAssets").invoke(assetManager);
                if (apkAssets != null) {
                    for (Object apkAsset : apkAssets) {
                        Class clazz = Class.forName("android.content.res.ApkAssets");
                        String path = (String) HiddenApiReflection.findMethod(clazz, "getAssetPath").invoke(apkAsset);
                        loadedResPathList.add(path);
                    }
                }
            } else {
                Object[] appStringBlocks = (Object[]) HiddenApiReflection.findField(assetManager, "mStringBlocks").get(assetManager);
                if (appStringBlocks != null && appStringBlocks.length > 0) {
                    int totalResCount = appStringBlocks.length;
                    LogUtils.d(TAG, "资源 数量: " + totalResCount);
                    for (int appResIndex = 1; appResIndex <= totalResCount; ++appResIndex) {
                        String inApp;
                        try {
                            inApp = (String) HiddenApiReflection.findMethod(assetManager, "getCookieName", int.class).invoke(assetManager, appResIndex);
                        } catch (Throwable e) {
                            //some phone like LG and SONY, may occur empty cookie error.
                            LogUtils.d(TAG, "Unable to get cookie name for resources index " + appResIndex + e.getMessage());
                            continue;
                        }
                        loadedResPathList.add(inApp);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e(e);
        }
        //LogUtils.d(TAG, "已加载资源: " + loadedResPathList.size() + " " + loadedResPathList.toString());
        return loadedResPathList;
    }

    /**
     * 加载dex到Resources中
     */
    public void loadResources(@NonNull String dexPath) {
        if (mLoadedResPathList.contains(dexPath)) {
            return;
        }
        mLoadedResPathList.add(dexPath);
        LogUtils.d("[加载资源] loadResources:" + dexPath + " 集合:" + mLoadedResPathList.toString());
    }

    /**
     * 注入Resources
     */
    public void injectResources(final @Nullable Context context, final @Nullable Resources resources) {
        List<String> loadedDexPath = getLoadedResourcesPath(resources);
        List<String> needLoadDexPath = new ArrayList<>();
        for (String dexPath : mLoadedResPathList) {
            if (!loadedDexPath.contains(dexPath)) {
                needLoadDexPath.add(dexPath);
            }
        }
        if (needLoadDexPath.size() == 0) {
            return;
        }
        LogUtils.d(TAG, "注入Res开始， 需要注入：" + needLoadDexPath.toString());
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Method method = HiddenApiReflection.findMethod(AssetManager.class, "addAssetPath", String.class);
                for (String dexPath : needLoadDexPath) {
                    LogUtils.d(TAG, "注入Res , dexPath:" + dexPath);
                    method.invoke(resources.getAssets(), dexPath);
                }
                LogUtils.d(TAG, "加载结果:" + getLoadedResourcesPath(resources));
            } else {
                //run on UI Thread
                //some rom like zte 4.2.2, fetching @ActivityThread instance in work thread will return null.
                if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
                    LogUtils.d(TAG, "Install res on main thread");
                    V14.installSplitResDirs(context, resources, needLoadDexPath);
                } else {
                    synchronized (sLock) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                synchronized (sLock) {
                                    try {
                                        V14.installSplitResDirs(context, resources, mLoadedResPathList);
                                    } catch (Throwable throwable) {
                                        throw new RuntimeException(throwable);
                                    }
                                    sLock.notify();
                                }
                            }
                        });
                        sLock.wait();
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            LogUtils.d(TAG, "注入Res失败：" + e.getMessage());
            return;
        }
        LogUtils.d(TAG, "注入Res Success");
    }

    private static class V21 extends VersionCompat {

        private static void installSplitResDirs(Resources preResources, List<String> splitResPaths) throws Throwable {
            Method method = VersionCompat.getAddAssetPathMethod();
            for (String splitResPath : splitResPaths) {
                method.invoke(preResources.getAssets(), splitResPath);
            }
        }
    }

    private static class V14 extends VersionCompat {

        private static Context getBaseContext(Context context) {
            Context ctx = context;
            while (ctx instanceof ContextWrapper) {
                ctx = ((ContextWrapper) ctx).getBaseContext();
            }
            return ctx;
        }

        private static void checkOrUpdateResourcesForContext(Context context, Resources preResources, Resources newResources)
                throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
            //if context is a ContextThemeWrapper.
            if (context instanceof ContextThemeWrapper && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                Resources themeWrapperResources = (Resources) mResourcesInContextThemeWrapper().get(context);
                if (themeWrapperResources == preResources) {
                    LogUtils.d(TAG, "context %s type is @ContextThemeWrapper, and it has its own resources instance!" + context.getClass().getSimpleName());
                    mResourcesInContextThemeWrapper().set(context, newResources);
                    mThemeInContextThemeWrapper().set(context, null);
                }
            }
            //find base context
            Context baseContext = getBaseContext(context);
            if (baseContext.getClass().getName().equals("android.app.ContextImpl")) {
                Resources baseContextRes = (Resources) mResourcesInContextImpl().get(baseContext);
                if (baseContextRes == preResources) {
                    mResourcesInContextImpl().set(baseContext, newResources);
                    mThemeInContentImpl().set(baseContext, null);
                }
            } else {
                //some rom customize ContextImpl for base context of Application
                try {
                    Resources baseContextRes = (Resources) HiddenApiReflection.findField(baseContext, "mResources").get(baseContext);
                    if (baseContextRes == preResources) {
                        HiddenApiReflection.findField(baseContext, "mResources").set(baseContext, newResources);
                        HiddenApiReflection.findField(baseContext, "mTheme").set(baseContext, null);
                    }
                } catch (NoSuchFieldException e) {
                    LogUtils.d(TAG, "Can not find mResources in " + baseContext.getClass().getName() + " " + e.getMessage());
                }
                Resources baseContextRes = (Resources) mResourcesInContextImpl().get(baseContext);
                if (baseContextRes == preResources) {
                    mResourcesInContextImpl().set(baseContext, newResources);
                    mThemeInContentImpl().set(baseContext, null);
                }
            }
        }

        @SuppressLint("PrivateApi")
        private static void installSplitResDirs(Context context, Resources preResources, List<String> splitResPaths) throws Throwable {
            //create a new Resources.
            Resources newResources = createResources(context, preResources, splitResPaths);
            checkOrUpdateResourcesForContext(context, preResources, newResources);
            Object activityThread = getActivityThread();
            Map<IBinder, Object> activities = (Map<IBinder, Object>) mActivitiesInActivityThread().get(activityThread);
            for (Map.Entry<IBinder, Object> entry : activities.entrySet()) {
                Object activityClientRecord = entry.getValue();
                Activity activity = (Activity) HiddenApiReflection.findField(activityClientRecord, "activity").get(activityClientRecord);
                if (context != activity) {
                    LogUtils.d(TAG, "pre-resources found in @mActivities");
                    checkOrUpdateResourcesForContext(activity, preResources, newResources);
                }
            }

            Map<Object, WeakReference<Resources>> activeResources;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                activeResources = (Map<Object, WeakReference<Resources>>) mActiveResourcesInActivityThread().get(activityThread);
            } else {
                Object resourcesManager = getResourcesManager();
                activeResources = (Map<Object, WeakReference<Resources>>) mActiveResourcesInResourcesManager().get(resourcesManager);
            }
            for (Map.Entry<Object, WeakReference<Resources>> entry : activeResources.entrySet()) {
                Resources res = entry.getValue().get();
                if (res == null) {
                    continue;
                }
                if (res == preResources) {
                    activeResources.put(entry.getKey(), new WeakReference<>(newResources));
                    LogUtils.d(TAG, "pre-resources found in @mActiveResources");
                    break;
                }
            }

            Map<String, WeakReference<Object>> instance_mPackages =
                    (Map<String, WeakReference<Object>>) mPackagesInActivityThread().get(activityThread);
            for (Map.Entry<String, WeakReference<Object>> entry : instance_mPackages.entrySet()) {
                Object packageInfo = entry.getValue().get();
                if (packageInfo == null) {
                    continue;
                }
                Resources resources = (Resources) mResourcesInLoadedApk().get(packageInfo);
                if (resources == preResources) {
                    LogUtils.d(TAG, "pre-resources found in @mPackages");
                    mResourcesInLoadedApk().set(packageInfo, newResources);
                }
            }

            Map<String, WeakReference<Object>> instance_mResourcePackages =
                    (Map<String, WeakReference<Object>>) mResourcePackagesInActivityThread().get(activityThread);
            for (Map.Entry<String, WeakReference<Object>> entry : instance_mResourcePackages.entrySet()) {
                Object packageInfo = entry.getValue().get();
                if (packageInfo == null) {
                    continue;
                }
                Resources resources = (Resources) mResourcesInLoadedApk().get(packageInfo);
                if (resources == preResources) {
                    LogUtils.d(TAG, "pre-resources found in @mResourcePackages");
                    mResourcesInLoadedApk().set(packageInfo, newResources);
                }
            }
        }

        private static List<String> getAppResDirs(String appResDir, AssetManager asset) throws NoSuchFieldException,
                IllegalAccessException, NoSuchMethodException, InvocationTargetException {
            List<String> existedAppResDirList;
            AssetManager sysAsset = Resources.getSystem().getAssets();
            Object[] sysStringBlocks = (Object[]) mStringBlocksInAssetManager().get(sysAsset);
            Object[] appStringBlocks = (Object[]) mStringBlocksInAssetManager().get(asset);
            int totalResCount = appStringBlocks.length;
            int sysResCount = sysStringBlocks.length;
            existedAppResDirList = new ArrayList<>(totalResCount - sysResCount);
            for (int appResIndex = sysResCount + 1; appResIndex <= totalResCount; ++appResIndex) {
                String inApp = (String) getGetCookieNameMethod().invoke(asset, appResIndex);
                existedAppResDirList.add(inApp);
            }
            if (!existedAppResDirList.contains(appResDir)) {
                boolean inSystem = false;
                for (int i = 1; i <= sysResCount; i++) {
                    final String cookieNameSys = (String) getGetCookieNameMethod().invoke(sysAsset, i);
                    if (appResDir.equals(cookieNameSys)) {
                        inSystem = true;
                        break;
                    }
                }
                if (!inSystem) {
                    existedAppResDirList.add(0, appResDir);
                }
            }
            return existedAppResDirList;
        }

        private static Resources createResources(Context context, Resources oldRes, List<String> splitResPaths) throws NoSuchFieldException,
                IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
            String appResDir = context.getPackageResourcePath();
            AssetManager oldAsset = oldRes.getAssets();
            List<String> resDirs = getAppResDirs(appResDir, oldAsset);
            resDirs.addAll(0, splitResPaths);
            AssetManager newAsset = createAssetManager();
            for (String recent : resDirs) {
                int ret = (int) getAddAssetPathMethod().invoke(newAsset, recent);
                if (ret == 0) {
                    LogUtils.d(TAG, "Split Apk res path : " + recent);
                    throw new RuntimeException("invoke addAssetPath failure! apk format maybe incorrect");
                }
            }
            return newResources(oldRes, newAsset);
        }

        private static Resources newResources(Resources originRes, AssetManager asset)
                throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
            return (Resources) HiddenApiReflection.findConstructor(originRes, AssetManager.class, DisplayMetrics.class, Configuration.class)
                    .newInstance(asset, originRes.getDisplayMetrics(), originRes.getConfiguration());
        }

        private static AssetManager createAssetManager() throws IllegalAccessException, InstantiationException {
            return AssetManager.class.newInstance();
        }
    }

    private static abstract class VersionCompat {

        private static Field mStringBlocksField;

        private static Method addAssetPathMethod;

        private static Method getCookieNameMethod;

        private static Method getAssetPathMethod;

        private static Method getApkAssetsMethod;

        private static Field mActivitiesInActivityThread;

        private static Object activityThread;

        private static Class<?> activityThreadClass;

        private static Class<?> contextImplClass;

        private static Field mResourcesInContextImpl;

        private static Field mThemeInContentImpl;

        private static Field mPackagesInActivityThread;

        private static Field mResourcePackagesInActivityThread;

        private static Field mActiveResourcesInActivityThread;

        private static Field mActiveResourcesInResourcesManager;

        private static Class<?> resourcesManagerClass;

        private static Object resourcesManager;

        private static Field mResourcesInContextThemeWrapper;

        private static Field mThemeInContextThemeWrapper;

        private static Class<?> loadedApkClass;

        private static Field mResourcesInLoadedApk;

        @SuppressLint("PrivateApi")
        static Object getActivityThread() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
            if (activityThread == null) {
                activityThread = HiddenApiReflection.findMethod(getActivityThreadClass(), "currentActivityThread").invoke(null);
            }
            return activityThread;
        }

        @SuppressLint("PrivateApi")
        static Object getResourcesManager() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
            if (resourcesManager == null) {
                resourcesManager = HiddenApiReflection.findMethod(getResourcesManagerClass(), "getInstance").invoke(null);
            }
            return resourcesManager;
        }

        @SuppressLint("PrivateApi")
        static Class<?> getActivityThreadClass() throws ClassNotFoundException {
            if (activityThreadClass == null) {
                activityThreadClass = Class.forName("android.app.ActivityThread");
            }
            return activityThreadClass;
        }

        @SuppressLint("PrivateApi")
        static Class<?> getResourcesManagerClass() throws ClassNotFoundException {
            if (resourcesManagerClass == null) {
                resourcesManagerClass = Class.forName("android.app.ResourcesManager");
            }
            return resourcesManagerClass;
        }

        @SuppressLint("PrivateApi")
        static Class<?> getLoadedApkClass() throws ClassNotFoundException {
            if (loadedApkClass == null) {
                loadedApkClass = Class.forName("android.app.LoadedApk");
            }
            return loadedApkClass;
        }

        @SuppressLint("PrivateApi")
        static Class<?> getContextImplClass() throws ClassNotFoundException {
            if (contextImplClass == null) {
                contextImplClass = Class.forName("android.app.ContextImpl");
            }
            return contextImplClass;
        }

        static Field mResourcesInLoadedApk() throws ClassNotFoundException, NoSuchFieldException {
            if (mResourcesInLoadedApk == null) {
                mResourcesInLoadedApk = HiddenApiReflection.findField(getLoadedApkClass(), "mResources");
            }
            return mResourcesInLoadedApk;
        }

        static Field mResourcesInContextImpl() throws ClassNotFoundException, NoSuchFieldException {
            if (mResourcesInContextImpl == null) {
                mResourcesInContextImpl = HiddenApiReflection.findField(getContextImplClass(), "mResources");
            }
            return mResourcesInContextImpl;
        }

        static Field mResourcesInContextThemeWrapper() throws NoSuchFieldException {
            if (mResourcesInContextThemeWrapper == null) {
                mResourcesInContextThemeWrapper = HiddenApiReflection.findField(ContextThemeWrapper.class, "mResources");
            }
            return mResourcesInContextThemeWrapper;
        }

        static Field mThemeInContextThemeWrapper() throws NoSuchFieldException {
            if (mThemeInContextThemeWrapper == null) {
                mThemeInContextThemeWrapper = HiddenApiReflection.findField(ContextThemeWrapper.class, "mTheme");
            }
            return mThemeInContextThemeWrapper;
        }

        static Field mThemeInContentImpl() throws ClassNotFoundException, NoSuchFieldException {
            if (mThemeInContentImpl == null) {
                mThemeInContentImpl = HiddenApiReflection.findField(getContextImplClass(), "mTheme");
            }
            return mThemeInContentImpl;
        }

        static Field mPackagesInActivityThread() throws ClassNotFoundException, NoSuchFieldException {
            if (mPackagesInActivityThread == null) {
                mPackagesInActivityThread = HiddenApiReflection.findField(getActivityThreadClass(), "mPackages");
            }
            return mPackagesInActivityThread;
        }

        static Field mActiveResourcesInActivityThread() throws ClassNotFoundException, NoSuchFieldException {
            if (mActiveResourcesInActivityThread == null) {
                mActiveResourcesInActivityThread = HiddenApiReflection.findField(getActivityThreadClass(), "mActiveResources");
            }
            return mActiveResourcesInActivityThread;
        }

        static Field mActiveResourcesInResourcesManager() throws ClassNotFoundException, NoSuchFieldException {
            if (mActiveResourcesInResourcesManager == null) {
                mActiveResourcesInResourcesManager = HiddenApiReflection.findField(getResourcesManagerClass(), "mActiveResources");
            }
            return mActiveResourcesInResourcesManager;
        }

        static Field mResourcePackagesInActivityThread() throws ClassNotFoundException, NoSuchFieldException {
            if (mResourcePackagesInActivityThread == null) {
                mResourcePackagesInActivityThread = HiddenApiReflection.findField(getActivityThreadClass(), "mResourcePackages");
            }
            return mResourcePackagesInActivityThread;
        }

        static Field mActivitiesInActivityThread() throws NoSuchFieldException, ClassNotFoundException {
            if (mActivitiesInActivityThread == null) {
                mActivitiesInActivityThread = HiddenApiReflection.findField(getActivityThreadClass(), "mActivities");
            }
            return mActivitiesInActivityThread;
        }

        static Field mStringBlocksInAssetManager() throws NoSuchFieldException {
            if (mStringBlocksField == null) {
                mStringBlocksField = HiddenApiReflection.findField(AssetManager.class, "mStringBlocks");
            }
            return mStringBlocksField;
        }

        static Method getAddAssetPathMethod() throws NoSuchMethodException {
            if (addAssetPathMethod == null) {
                addAssetPathMethod = HiddenApiReflection.findMethod(AssetManager.class, "addAssetPath", String.class);
            }
            return addAssetPathMethod;
        }

        static Method getGetCookieNameMethod() throws NoSuchMethodException {
            if (getCookieNameMethod == null) {
                getCookieNameMethod = HiddenApiReflection.findMethod(AssetManager.class, "getCookieName", int.class);
            }
            return getCookieNameMethod;
        }

        @SuppressLint("PrivateApi")
        @RequiresApi(Build.VERSION_CODES.P)
        static Method getGetAssetPathMethod() throws ClassNotFoundException, NoSuchMethodException {
            if (getAssetPathMethod == null) {
                Class clazz = Class.forName("android.content.res.ApkAssets");
                getAssetPathMethod = HiddenApiReflection.findMethod(clazz, "getAssetPath");
            }
            return getAssetPathMethod;
        }

        @RequiresApi(Build.VERSION_CODES.P)
        static Method getGetApkAssetsMethod() throws NoSuchMethodException {
            if (getApkAssetsMethod == null) {
                getApkAssetsMethod = HiddenApiReflection.findMethod(AssetManager.class, "getApkAssets");
            }
            return getApkAssetsMethod;
        }
    }

}
