package com.ng.syringe.load;

import android.app.Activity;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;

import com.ng.syringe.Syringe;
import com.ng.syringe.load.hook.impl.HookManagerApi28;
import com.ng.syringe.load.hook.impl.HookManagerApi30;
import com.ng.syringe.load.hook.impl.IHookManager;
import com.ng.syringe.util.LogUtils;

import java.io.File;
import java.util.HashSet;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

/**
 * @author : jiangzhengnan.jzn@alibaba-inc.com
 * @creation : 2022/01/09
 * @description :
 * 加载管理器
 * 1.classLoader dex或apk等插件加载
 * 2.addAssetPath 资源加载
 */
public class SyringeLoadComponent {

    private final String DEX_SUFFIX = ".dex";
    private final String APK_SUFFIX = ".apk";
    private final String JAR_SUFFIX = ".jar";
    private final String ZIP_SUFFIX = ".zip";
    public final String DEX_DIR = "odex";
    private final String OPTIMIZE_DEX_DIR = "optimize_dex";

    private HashSet<File> loadedDex = new HashSet<>();

    //hook实现类，区分版本
    private IHookManager mHookManager;

    /**
     * 加载补丁
     */
    public void loadPlug(@NonNull Context context, @NonNull String plugPath) {
        loadedDex.clear();
        if (isGoingToFix(plugPath)) {
            doDexInject(context, plugPath);
        }
    }

    // 加载plug到classLoader
    private void doDexInject(@NonNull Context context, @NonNull String plugPath) {
        File dexDir = new File(plugPath);
        if (!dexDir.exists()) {
            dexDir.mkdirs();
        }
        try {
            // 1.加载应用程序dex的Loader
            PathClassLoader appClassLoader = (PathClassLoader) context.getClassLoader();
            if (loadedDex.size() > 0) {
                for (File dex : loadedDex) {
                    String dexPath = dex.getAbsolutePath();
                    // 2.加载指定的修复的dex文件的Loader
                    DexClassLoader dexLoader = new DexClassLoader(
                            dexPath,// 修复好的dex（补丁）所在目录
                            dexDir.getAbsolutePath(),// 存放dex的解压目录（用于jar、zip、apk格式的补丁）
                            null,// 加载dex时需要的库
                            appClassLoader// 父类加载器
                    );
                    // 3.开始合并
                    // 合并的目标是Element[],重新赋值它的值即可

                    // BaseDexClassLoader中有 变量: DexPathList pathList
                    // DexPathList中有 变量 Element[] dexElements
                    // 依次反射即可
                    //3.1 准备好pathList的引用
                    Object dexPathList = FixDexUtil.getPathList(dexLoader);
                    Object pathPathList = FixDexUtil.getPathList(appClassLoader);
                    //3.2 从pathList中反射出element集合
                    Object leftDexElements = FixDexUtil.getDexElements(dexPathList);
                    Object rightDexElements = FixDexUtil.getDexElements(pathPathList);
                    //3.3 合并两个dex数组
                    Object dexElements = FixDexUtil.combineArray(leftDexElements, rightDexElements);

                    // 重写给PathList里面的Element[] dexElements;赋值
                    Object pathList = FixDexUtil.getPathList(appClassLoader);// 一定要重新获取，不要用pathPathList，会报错
                    FixDexUtil.setField(pathList, pathList.getClass(), "dexElements", dexElements);

                    // 加载资源
                    Syringe.instance().loadResources(dexPath);
                }
                LogUtils.d("[加载插件] 修复完成:" + loadedDex.toString());
                //Toast.makeText(context, "修复完成", Toast.LENGTH_SHORT).show();
            } else {
                LogUtils.d("[加载插件] 修复失败,loadedDex为空");
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.d("[加载插件] 修复异常:" + e.getMessage());
        }
    }

    public boolean isGoingToFix(@NonNull String plugPath) {
        File dexDir = new File(plugPath);
        LogUtils.d("[加载插件] 遍历查找dex暂存目录:" + dexDir);
        if (!dexDir.exists()) {
            LogUtils.d("[加载插件] 遍历查找dex目录为空" + dexDir);
            return false;
        }
        boolean result = false;
        // 遍历所有的修复dex , 因为可能是多个dex修复包
        File[] listFiles = dexDir.listFiles();
        if (listFiles != null) {
            LogUtils.d("[加载插件] 遍历查找dex 文件数量:" + listFiles.length);
            for (File file : listFiles) {
                if (file.getName().endsWith(DEX_SUFFIX)
                        || file.getName().endsWith(APK_SUFFIX)
                        || file.getName().endsWith(JAR_SUFFIX)
                        || file.getName().endsWith(ZIP_SUFFIX)) {

                    loadedDex.add(file);// 存入集合
                    //有目标dex文件, 需要修复
                    LogUtils.d("[加载插件] 有目标dex文件, 需要修复:" + file.getAbsolutePath() + " " + file.getName());
                    result = true;
                }
            }
        }
        if (!result) {
            LogUtils.d("[加载插件] 未找到dex文件，不执行修复");
        }
        return result;
    }


    /**
     * 对每个dex做单独的操作
     */
    public void hookActivity(Activity activity) {
        initHookManagerIfNeed(activity);

        for (File dex : loadedDex) {
            // hook start activity
            mHookManager.hookStartActivity();

            // hook注册静态广播
            if (dex.getName().endsWith(APK_SUFFIX)) {
                LogUtils.d("[加载插件] 准备注册静态广播:" + dex.getAbsolutePath());
                mHookManager.hookReceivers(activity, dex.getAbsolutePath());
            }
        }
    }

    private void initHookManagerIfNeed(@NonNull Context context) {
        if (mHookManager != null) {
            return;
        }
        LogUtils.d("当前环境Android版本:" + Build.VERSION.SDK_INT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            //版本28,9.0以上
            mHookManager = new HookManagerApi30(context);
        } else {
            //28,9.0以下，待测试
            mHookManager = new HookManagerApi28(context);
        }
    }
}
