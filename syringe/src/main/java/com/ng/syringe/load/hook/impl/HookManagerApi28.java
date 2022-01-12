package com.ng.syringe.load.hook.impl;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ng.syringe.load.proxy.ProxyActivity;
import com.ng.syringe.util.LogUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * @author : jiangzhengnan.jzn@alibaba-inc.com
 * @creation : 2022/01/10
 * @description :
 */
public class HookManagerApi28 implements IHookManager {
    private Context context;

    public HookManagerApi28(Context context) {
        this.context = context;
    }


    /**
     * hook startActivity 让开启activity走自己的代理类
     */
    @Override
    public void hookStartActivity() {
        this.context = context;
        //if (Build.VERSION.SDK_INT < 23) {

        try {
            // 基于28的源码
            Class<?> mActivityManagerClass = Class.forName("android.app.ActivityManager");
            //在拿到 IActivityManagerSingleton 这个静态属性
            Field mActivitySigletonField = mActivityManagerClass.getDeclaredField("IActivityManagerSingleton");

            //由于是私有的 要设置为可访问的
            mActivitySigletonField.setAccessible(true);
            // 由于是静态的 我直接获取 singleton这个对像
            Object mSingletonObj = mActivitySigletonField.get(null);
            //拿到了这个对象 但是我不需要这个对象，我是是需要它内部的mInstance这个对象


            Class<?> mSingletonClass = Class.forName("android.util.Singleton");
            Field mInstanceField = mSingletonClass.getDeclaredField("mInstance");
            mInstanceField.setAccessible(true);
            //然后获取这个mInstance
            Object mIactivityManagerObj = mInstanceField.get(mSingletonObj);

            // 现在获取到了我们要处理的这个对象 ，然后我们通过动态代理 来生成一个代理对象
            StartActivityInvocation StartActivityInvocation = new StartActivityInvocation(mIactivityManagerObj);

            Object mProxyActivityManager = Proxy.newProxyInstance(getClass().getClassLoader(), mIactivityManagerObj.getClass().getInterfaces(), StartActivityInvocation);
            mInstanceField.set(mSingletonObj, mProxyActivityManager);// 给将我们生成的动态代理的对象设置进去


            hookActivityThreadMH();
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.d("hook start Activity 出现了异常" + e.getMessage());
        }
    }

    /**
     * 通过解析清单文件来 拿到静态广播并且进行注册
     */
    @Override
    @SuppressLint({"PrivateApi", "DiscouragedPrivateApi"})
    public void hookReceivers(Activity activity, String path) {
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


    class StartActivityInvocation implements InvocationHandler {
        private final Object obj;

        // 代理对象内部持有真实对象的引用
        public StartActivityInvocation(Object mIActivityManagerObj) {
            this.obj = mIActivityManagerObj;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Intent oldIntent = null;
            if ("startActivity".equals(method.getName())) {
                LogUtils.d("hook到了startActivity");
                // hook startActivity 我们拿到要启动的Intent
                int index = 0;
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof Intent) {
                        index = i;
                        oldIntent = (Intent) args[i];
                    }
                }
                Intent newIntent = new Intent();
                //为什么要用ProxyActivity 因为这一步是要绕过AMS的检查，清单文件注册了的
                newIntent.setComponent(new ComponentName(context, ProxyActivity.class));
                newIntent.putExtra("oldIntent", oldIntent);

                //将真实的Intent隐藏起来创建新的 然后给系统
                args[index] = newIntent;
            }
            return method.invoke(obj, args);
        }
    }


    /**
     * hook ActivityThread中的handler，处理我们的启动actiivty的消息，
     */

    private void hookActivityThreadMH() throws Exception {

        //1 、我们要想办法代理处理那个HandleMessage()  所以就去找静态的，
        //  找handler，但是发现handler是new出来的，所以在找ActivityThread这个类，最后找到
        // 一个 sCurrentActivityThread 这个字段，是静态，而且是它持有了自己，那么直接获取class对象
        Class<?> mActivityThreadClass = Class.forName("android.app.ActivityThread");

        Field mSCurrntActivityThreadField = mActivityThreadClass.getDeclaredField("sCurrentActivityThread");

        //然后我们通过静态对象 可以获取到ActivityThread的对象，而且是系统创建的对象
        mSCurrntActivityThreadField.setAccessible(true);
        Object mActivityThreadObj = mSCurrntActivityThreadField.get(null);
        //在获取反射获取mh

        Field mHandlerField = mActivityThreadClass.getDeclaredField("mH");
        mHandlerField.setAccessible(true);

        //这一步我们就拿到了mH这个对象
        Object mHandlerObj = mHandlerField.get(mActivityThreadObj);


        // 下面我就要考虑是用动态代理还是用设置接口，来让handlerMessage先处理我们的，通过handler的dispatchMessage的方法 而且内部是提供接口，
        // 那就这里不用动态代理了，用提供的接口

        //接着还是用反射 ，给注入一个callback

        Class<?> mHandlerClass = Class.forName("android.os.Handler");

        Field mHandlerCallbackField = mHandlerClass.getDeclaredField("mCallback");

        mHandlerCallbackField.setAccessible(true);


        //给注入一个callback
        HandlerCallback handlerCallback = new HandlerCallback((Handler) mHandlerObj);
        mHandlerCallbackField.set(mHandlerObj, handlerCallback);

    }


    class HandlerCallback implements Handler.Callback {


        private final Handler handler;

        public HandlerCallback(Handler handler) {
            this.handler = handler;
        }

        @Override
        public boolean handleMessage(Message msg) {
            Log.e("handle", msg.what + "");
            // 继续让系统处理  9.0 的是159, 9.0之下的是100
            if (msg.what == 100 || msg.what == 159) {
                handleLauchActivity(msg);
            }
            handler.handleMessage(msg);
            return true;
        }
    }

    /**
     * 绕过AMS检查后，将我们真实的取出来 进行还原
     *
     * @param msg
     */
    private void handleLauchActivity(Message msg) {
        Object obj = msg.obj;

        try {
            Field mIntentField = obj.getClass().getDeclaredField("intent");
            mIntentField.setAccessible(true);


            // proxyActivity 取出来是这样的
            Intent realIntent = (Intent) mIntentField.get(obj);

            Intent oldIntent = realIntent.getParcelableExtra("oldIntent");

            if (oldIntent != null) {
                // 然后在这里统一处理跳转activity

                //判断有没有登陆成功
                SharedPreferences sharedPreferences = context.getSharedPreferences("plugin", context.MODE_PRIVATE);
                boolean isLogin = sharedPreferences.getBoolean("login", false);
//                if (isLogin) {
//                    // 登陆了 按照原来的目标
//                    realIntent.setComponent(oldIntent.getComponent());
//                } else {
//                    // 没有登陆
//                    ComponentName newComponent=new ComponentName(context,LogActivity.class);
//                    realIntent.setComponent(newComponent);
//                    realIntent.putExtra("extraIntent",oldIntent.getComponent().getClassName());
//
//                }
            }


        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.d("hook handle Activity 出现异常:" + e.getMessage().toString());
        }

    }

    /**
     *
     * //  这个是handler内部的处理方法
     * //  我们发现它会 先看mCallback 不为空，去执行callback里面的
     * //  handleMessage 方法，返回为true 就直接返回了
     *
     *  public void dispatchMessage(Message msg) {
     *         if (msg.callback != null) {
     *             handleCallback(msg);
     *         } else {
     *             if (mCallback != null) {
     *                 if (mCallback.handleMessage(msg)) {
     *                     return;
     *                 }
     *             }
     *             handleMessage(msg);
     *         }
     *     }
     */
}
