package com.ng.syringe.load;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.ng.syringe.download.DownloadHelper;
import com.ng.syringe.util.LogUtils;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

/**
 * @author : jiangzhengnan.jzn
 * @creation : 2021/12/15
 * @description :
 */
public class FixDexUtil {

    private static final String DEX_SUFFIX = ".dex";
    private static final String APK_SUFFIX = ".apk";
    private static final String JAR_SUFFIX = ".jar";
    private static final String ZIP_SUFFIX = ".zip";
    public static final String DEX_DIR = "odex";
    private static final String OPTIMIZE_DEX_DIR = "optimize_dex";
    private static HashSet<File> loadedDex = new HashSet<>();

    static {
        loadedDex.clear();
    }

    /**
     * 加载补丁，使用默认目录：data/data/包名/files/odex
     */
    public static void loadFixedDex(Activity activity) {
        loadFixedDex(activity, null);
    }

    /**
     * 加载补丁
     */
    public static void loadFixedDex(Activity activity, String filePath) {
        // dex合并之前的dex
        doDexInject(activity, loadedDex);
    }

    public static boolean isGoingToFix(@NonNull Activity activity) {
        File dexDir = new File(DownloadHelper.getDexDirFilePath(activity));
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
     * 通过解析清单文件来 拿到静态广播并且进行注册
     */
    @SuppressLint({"PrivateApi","DiscouragedPrivateApi"})
    private static void parseReceivers(Activity activity, String path) {
        try {
            ClassLoader classLoader = activity.getClassLoader();
            //我们知道解析一个apk文件的入口就是PackageParse.parsePackage 这个方法
            //所以我们使用反射 来调用这个方法 最终得到了一个 PackageParse$Package 这个类
            Class<?> mPackageParseClass = Class.forName("android.content.pm.PackageParser");
            Method mParsePackageMethod = mPackageParseClass.getDeclaredMethod("parsePackage", File.class, int.class);
            Object mPackageParseObj = mPackageParseClass.newInstance();
            //拿到Package对象
            Object mPackageObj = mParsePackageMethod.invoke(mPackageParseObj, new File(path), PackageManager.GET_ACTIVITIES);
            if (mPackageObj == null) {
                return;
            }
            //解析出来的receiver就存在PackageParse$Package 这个类里面的一个receivers集合里面
            Field mReceiversListField = mPackageObj.getClass().getDeclaredField("receivers");
            //然后得到反射得到这个属性的值 最终得到一个集合
            List mReceiverList = (List) mReceiversListField.get(mPackageObj);

            //接下来我们要拿到 IntentFilter 和name属性 这样才能反射创建对象，动态在宿主里面注册广播
            Class<?> mComponetClass = Class.forName("android.content.pm.PackageParser$Component");
            Field mIntentFields = mComponetClass.getDeclaredField("intents");

            //这两行是为了调用generateActivityInfo 而反射拿到的参数
            Class<?> mPackageParse$ActivityClass = Class.forName("android.content.pm.PackageParser$Activity");
            Class<?> mPackageUserStateClass = Class.forName("android.content.pm.PackageUserState");

            Object mPackzgeUserStateObj = mPackageUserStateClass.newInstance();

            // 拿到generateActivityInfo这个方法
            Method mGeneReceiverInfo = mPackageParseClass.getMethod("generateActivityInfo", mPackageParse$ActivityClass, int.class, mPackageUserStateClass, int.class);

            Class<?> mUserHandlerClass = Class.forName("android.os.UserHandle");
            Method getCallingUserIdMethod = mUserHandlerClass.getDeclaredMethod("getCallingUserId");

            int userId = (int) getCallingUserIdMethod.invoke(null);

            //然后for循环 去拿到name和 intentFilter
            for (Object activityObj : mReceiverList) {
                //调用generateActivityInfo
                // 这个是我们要调用的方法的形参 public static final ActivityInfo generateActivityInfo(Activity a, int flags,PackageUserState state, int userId);
                //得到一个ActivityInfo
                ActivityInfo info = (ActivityInfo) mGeneReceiverInfo.invoke(mPackageParseObj, activityObj, 0, mPackzgeUserStateObj, userId);
                //拿到这个name 相当于我们在清单文件中Android:name 这样，是一个全类名，然后通过反射去创建对象
                BroadcastReceiver broadcastReceiver = (BroadcastReceiver) classLoader.loadClass(info.name).newInstance();

                //在拿到IntentFilter
                List<? extends IntentFilter> intents = (List<? extends IntentFilter>) mIntentFields.get(activityObj);
                //然后直接调用registerReceiver方法发
                for (IntentFilter intentFilter : intents) {
                    LogUtils.d("[加载插件] 注册静态广播成功:" + intentFilter.toString());
                    activity.registerReceiver(broadcastReceiver, intentFilter);
                }
            }
        } catch (Exception e) {
            LogUtils.d("[加载插件] 注册静态广播失败:" + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void doDexInject(Activity activity, HashSet<File> loadedDex) {
        File dexDir = new File(DownloadHelper.getDexDirFilePath(activity));
        if (!dexDir.exists()) {
            dexDir.mkdirs();
        }
        try {
            // 1.加载应用程序dex的Loader
            PathClassLoader appClassLoader = (PathClassLoader) activity.getClassLoader();
            for (File dex : loadedDex) {
                // 2.加载指定的修复的dex文件的Loader
                DexClassLoader dexLoader = new DexClassLoader(
                        dex.getAbsolutePath(),// 修复好的dex（补丁）所在目录
                        dexDir.getAbsolutePath(),// 存放dex的解压目录（用于jar、zip、apk格式的补丁）
                        null,// 加载dex时需要的库
                        appClassLoader// 父类加载器
                );
                // 3.开始合并
                // 合并的目标是Element[],重新赋值它的值即可

                /**
                 * BaseDexClassLoader中有 变量: DexPathList pathList
                 * DexPathList中有 变量 Element[] dexElements
                 * 依次反射即可
                 */

                //3.1 准备好pathList的引用
                Object dexPathList = getPathList(dexLoader);
                Object pathPathList = getPathList(appClassLoader);
                //3.2 从pathList中反射出element集合
                Object leftDexElements = getDexElements(dexPathList);
                Object rightDexElements = getDexElements(pathPathList);
                //3.3 合并两个dex数组
                Object dexElements = combineArray(leftDexElements, rightDexElements);

                // 重写给PathList里面的Element[] dexElements;赋值
                Object pathList = getPathList(appClassLoader);// 一定要重新获取，不要用pathPathList，会报错
                setField(pathList, pathList.getClass(), "dexElements", dexElements);



                //注册静态广播
                if (dex.getName().endsWith(APK_SUFFIX)) {
                    LogUtils.d("[加载插件] 准备注册静态广播:" + dex.getAbsolutePath());
                    parseReceivers(activity, dex.getAbsolutePath());
                }

            }
            LogUtils.d("[加载插件] 修复完成:" + loadedDex.toString());
            Toast.makeText(activity, "修复完成", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.d("[加载插件] 修复异常:" + e.getMessage());
        }
    }

    /**
     * 反射给对象中的属性重新赋值
     */
    private static void setField(Object obj, Class<?> cl, String field, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field declaredField = cl.getDeclaredField(field);
        declaredField.setAccessible(true);
        declaredField.set(obj, value);
    }

    /**
     * 反射得到对象中的属性值
     */
    private static Object getField(Object obj, Class<?> cl, String field) throws NoSuchFieldException, IllegalAccessException {
        Field localField = cl.getDeclaredField(field);
        localField.setAccessible(true);
        return localField.get(obj);
    }


    /**
     * 反射得到类加载器中的pathList对象
     */
    private static Object getPathList(Object baseDexClassLoader) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        return getField(baseDexClassLoader, Class.forName("dalvik.system.BaseDexClassLoader"), "pathList");
    }

    /**
     * 反射得到pathList中的dexElements
     */
    private static Object getDexElements(Object pathList) throws NoSuchFieldException, IllegalAccessException {
        return getField(pathList, pathList.getClass(), "dexElements");
    }

    /**
     * 数组合并
     */
    private static Object combineArray(Object arrayLhs, Object arrayRhs) {
        Class<?> clazz = arrayLhs.getClass().getComponentType();
        int i = Array.getLength(arrayLhs);// 得到左数组长度（补丁数组）
        int j = Array.getLength(arrayRhs);// 得到原dex数组长度
        int k = i + j;// 得到总数组长度（补丁数组+原dex数组）
        Object result = Array.newInstance(clazz, k);// 创建一个类型为clazz，长度为k的新数组
        System.arraycopy(arrayLhs, 0, result, 0, i);
        System.arraycopy(arrayRhs, 0, result, i, j);
        return result;
    }
}