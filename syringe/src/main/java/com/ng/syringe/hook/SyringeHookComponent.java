package com.ng.syringe.hook;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;

import com.ng.syringe.hook.impl.HookManagerApi28;
import com.ng.syringe.hook.impl.HookManagerApi30;
import com.ng.syringe.hook.impl.IHookManager;
import com.ng.syringe.util.LogUtils;

/**
 * @author : jiangzhengnan.jzn@alibaba-inc.com
 * @creation : 2022/01/09
 * @description :
 * 1.拦截发送的Intent, 伪装成StubActivity, 绕过ASM检查
 * 2.拦截ActivityThread中的handler, 处理启动StubActivity的消息，转发成真正要启动的Activity
 */
public class SyringeHookComponent {

    private IHookManager mHookManager;

    /**
     * hook activity跳转流程
     */
    public void hookStartActivity(@NonNull Context context) {
        initIfNeed(context);
        mHookManager.hookStartActivity();
    }

    private void initIfNeed(@NonNull Context context) {
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
