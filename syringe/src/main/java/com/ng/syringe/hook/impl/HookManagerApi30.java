package com.ng.syringe.hook.impl;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.ng.syringe.Syringe;
import com.ng.syringe.util.LogUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * @author : jiangzhengnan.jzn@alibaba-inc.com
 * @creation : 2022/01/10
 * @description :
 * 1.拦截发送的Intent, 伪装成StubActivity, 绕过ASM检查
 * 2.拦截ActivityThread中的handler, 处理启动StubActivity的消息，转发成真正要启动的Activity
 */
public class HookManagerApi30 implements IHookManager {
    private static final String TAG = "[HookManagerApi30]";

    private Context context;

    public HookManagerApi30(Context context) {
        this.context = context;
    }

    /**
     * 第一步
     * hook startActivity 流程
     */
    @Override
    @SuppressLint("PrivateApi")
    public void hookStartActivity() {
        try {
            //1. 因为startActivity -> startActivityForResult -> mInstrumentation.execStartActivity -> ActivityManager.getService()
            //所以这里要先拿到 ActivityTaskManager 类
            Class<?> mActivityTaskManagerClass = Class.forName("android.app.ActivityTaskManager");

            //2. ActivityManager.getService() 返回 IActivityTaskManagerSingleton.get()， 所以要拿到 IActivityTaskManagerSingleton
            Field mActivityTaskManagerSingletonField = mActivityTaskManagerClass.getDeclaredField("IActivityTaskManagerSingleton");
            //由于是私有的,要设置为可访问的
            mActivityTaskManagerSingletonField.setAccessible(true);
            //由于是静态的,直接获取 singleton这个对像
            Object mSingletonObj = mActivityTaskManagerSingletonField.get(null);

            //3. IActivityManagerSingleton是由Singleton实现的单例模式，所以需要拿到mInstance,即IActivityManagerSingleton的实例
            Class<?> mSingletonClass = Class.forName("android.util.Singleton");
            Field mInstanceField = mSingletonClass.getDeclaredField("mInstance");
            mInstanceField.setAccessible(true);
            Object mIActivityManagerObj = mInstanceField.get(mSingletonObj);

            //4. 现在获取到了我们要处理的这个对象 ，然后通过动态代理 来生成一个代理对象
            StartActivityInvocation StartActivityInvocation = new StartActivityInvocation(mIActivityManagerObj);

            Object mProxyActivityManager = Proxy.newProxyInstance(getClass().getClassLoader(), mIActivityManagerObj.getClass().getInterfaces(), StartActivityInvocation);
            //5. 将生成的动态代理对象，替换原有的实现[注入]
            mInstanceField.set(mSingletonObj, mProxyActivityManager);

            LogUtils.d(TAG + "hook startActivity 成功");
            hookActivityThread();
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.d(TAG + "hook startActivity 出现了异常" + e.getMessage());
        }
    }

    /**
     * hook startActivity 方法，可以做一些数据处理。
     */
    static class StartActivityInvocation implements InvocationHandler {
        private final Object obj;

        // 代理对象内部持有真实对象的引用
        public StartActivityInvocation(Object mIActivityManagerObj) {
            this.obj = mIActivityManagerObj;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Intent oldIntent = null;
            if ("startActivity".equals(method.getName())) {
                LogUtils.d("hook到了startActivity方法");
                // hook startActivity 我们拿到要启动的Intent
                int index = 0;
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof Intent) {
                        index = i;
                        oldIntent = (Intent) args[i];
                    }
                }
                if (oldIntent != null && !TextUtils.isEmpty(oldIntent.getStringExtra("targetActivity"))) {
                    String targetActivity = oldIntent.getStringExtra("targetActivity");
                    LogUtils.d("hook startActivity intent 传参:" + targetActivity);
                }
            }
            return method.invoke(obj, args);
        }
    }


    /**
     * 第二步
     * hook ActivityThread中的handler，处理启动Activity的流程
     */
    private void hookActivityThread() {
        try {
            //1. 需要处理HandleMessage()  所以就去找静态的ActivityThread对象
            Class<?> mActivityThreadClass = Class.forName("android.app.ActivityThread");

            //2. 因为ActivityThread中有一个对象静态持有自己：private static volatile ActivityThread sCurrentActivityThread;
            //所以这里直接拿到它
            Field mSCurrentActivityThreadField = mActivityThreadClass.getDeclaredField("sCurrentActivityThread");
            //然后通过静态对象 可以获取到ActivityThread的对象，而且是系统创建的对象
            mSCurrentActivityThreadField.setAccessible(true);
            Object mActivityThreadObj = mSCurrentActivityThreadField.get(null);

            //3. 反射获取mH (ActivityThread 内部成员变量 final H mH = new H();)
            Field mHandlerField = mActivityThreadClass.getDeclaredField("mH");
            mHandlerField.setAccessible(true);
            //4. 拿到了原来的mh对象
            Object mHandlerObj = mHandlerField.get(mActivityThreadObj);

            //接着还是用反射 ，给系统Handler注入一个callback
            Class<?> mHandlerClass = Class.forName("android.os.Handler");
            Field mHandlerCallbackField = mHandlerClass.getDeclaredField("mCallback");
            mHandlerCallbackField.setAccessible(true);
            //给mh对象注入自定义的callback
            HandlerCallback handlerCallback = new HandlerCallback((Handler) mHandlerObj);
            mHandlerCallbackField.set(mHandlerObj, handlerCallback);

            LogUtils.d(TAG + "hook handle Activity 成功");
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.d(TAG + "hook handle Activity 出现异常:" + e.getMessage());
        }
    }


    class HandlerCallback implements Handler.Callback {
        private int EXECUTE_TRANSACTION = 159;
        private final Handler handler;

        public HandlerCallback(Handler handler) {
            this.handler = handler;
        }

        @Override
        public boolean handleMessage(Message msg) {
            LogUtils.d(TAG + "hook Activity Thread Handler:" + msg.what + "");
            //Handler的dispatchMessage有3个callback优先级，首先是msg自带的callback，其次是Handler的成员mCallback,最后才是Handler类自身的handlerMessage方法,
            //它成员mCallback.handleMessage的返回值为true，则不会继续往下执行 Handler.handlerMessage
            //我们这里只是要hook，插入逻辑，所以必须返回false，让Handler原本的handlerMessage能够执行.
            if (msg.what == EXECUTE_TRANSACTION) {//这是跳转的时候,要对intent进行还原
                try {
                    //先把相关@hide的类都建好
                    Class<?> ClientTransactionClz = Class.forName("android.app.servertransaction.ClientTransaction");
                    Class<?> LaunchActivityItemClz = Class.forName("android.app.servertransaction.LaunchActivityItem");

                    Field mActivityCallbacksField = ClientTransactionClz.getDeclaredField("mActivityCallbacks");//ClientTransaction的成员
                    mActivityCallbacksField.setAccessible(true);
                    //类型判定，好习惯
                    if (!ClientTransactionClz.isInstance(msg.obj)) return true;
                    //根据源码，在这个分支里面,msg.obj就是 ClientTransaction类型,所以，直接用
                    Object mActivityCallbacksObj = mActivityCallbacksField.get(msg.obj);
                    //拿到了ClientTransaction的List<ClientTransactionItem> mActivityCallbacks;
                    List list = (List) mActivityCallbacksObj;
                    if (list.size() == 0) return true;
                    //所以这里直接就拿到第一个就好了
                    Object LaunchActivityItemObj = list.get(0);

                    //这里必须判定 LaunchActivityItemClz，
                    // 因为 最初的ActivityResultItem传进去之后都被转化成了这LaunchActivityItemClz的实例
                    if (!LaunchActivityItemClz.isInstance(LaunchActivityItemObj)) return true;

                    // LaunchActivityItem 中的intent即为真实的intent
                    Field mIntentField = LaunchActivityItemClz.getDeclaredField("mIntent");
                    mIntentField.setAccessible(true);

                    Intent mIntent = (Intent) mIntentField.get(LaunchActivityItemObj);
                    if (mIntent != null && !TextUtils.isEmpty(mIntent.getStringExtra("targetActivity"))) {
                        String targetActivity = mIntent.getStringExtra("targetActivity");
                        LogUtils.d("需要跳转的Activity 类名:" + targetActivity);
                        Class targetActivityClz = Syringe.instance().loadClass(targetActivity);
                        LogUtils.d("需要跳转的Activity class name:" + targetActivityClz.getName());

                        ComponentName newComponent = new ComponentName(context, targetActivityClz);
                        mIntent.setComponent(newComponent);

                        mIntentField.set(LaunchActivityItemObj, mIntent);
                    }

//                    Intent oriIntent = (Intent) mIntent.getExtras().get(ORI_INTENT_TAG);
//                    //那么现在有了最原始的intent，应该怎么处理呢？
//                    mIntentField.set(LaunchActivityItemObj, oriIntent);

                    //返回true,参考(Handler dispatchMessage(@NonNull Message msg))
                    return false;
                } catch (Exception e) {
                    LogUtils.d("hook activity handle 失败 :" + e.getMessage());
                    e.printStackTrace();
                }
            }
            return false;
        }
    }

//    private static void hookPMAfter28(Context context) throws ClassNotFoundException,
//            NoSuchFieldException, IllegalAccessException, NoSuchMethodException,
//            InvocationTargetException {
//        String pmName = Util.getPMName(context);
//        String hostClzName = Util.getHostClzName(context, pmName);
//
//        Class<?> forName = Class.forName("android.app.ActivityThread");//PM居然是来自ActivityThread
//        Field field = forName.getDeclaredField("sCurrentActivityThread");
//        field.setAccessible(true);
//        Object activityThread = field.get(null);
//        Method getPackageManager = activityThread.getClass().getDeclaredMethod("getPackageManager");
//        Object iPackageManager = getPackageManager.invoke(activityThread);
//
//        String packageName = Util.getPMName(context);
//        PMSInvocationHandler handler = new PMSInvocationHandler(iPackageManager, packageName, hostClzName);
//        Class<?> iPackageManagerIntercept = Class.forName("android.content.pm.IPackageManager");
//        Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new
//                Class<?>[]{iPackageManagerIntercept}, handler);
//        // 获取 sPackageManager 属性
//        Field iPackageManagerField = activityThread.getClass().getDeclaredField("sPackageManager");
//        iPackageManagerField.setAccessible(true);
//        iPackageManagerField.set(activityThread, proxy);
//    }
//
//    static class PMSInvocationHandler implements InvocationHandler {
//
//        private Object base;
//        private String packageName;
//        private String hostClzName;
//
//        public PMSInvocationHandler(Object base, String packageName, String hostClzName) {
//            this.packageName = packageName;
//            this.base = base;
//            this.hostClzName = hostClzName;
//        }
//
//        @Override
//        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//
//            if (method.getName().equals("getActivityInfo")) {
//                ComponentName componentName = new ComponentName(packageName, hostClzName);
//                return method.invoke(base, componentName, PackageManager.GET_META_DATA, 0);//破费，一定是这样
//            }
//
//            return method.invoke(base, args);
//        }
//    }

}
