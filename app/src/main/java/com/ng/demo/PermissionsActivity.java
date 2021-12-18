package com.ng.demo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ng.demo.permission.RomUtils;
import com.ng.demo.permission.rom.HuaweiUtils;
import com.ng.demo.permission.rom.MeizuUtils;
import com.ng.demo.permission.rom.MiuiUtils;
import com.ng.demo.permission.rom.OppoUtils;
import com.ng.demo.permission.rom.QikuUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class PermissionsActivity extends AppCompatActivity implements IPermissionListener {

    private static final String TAG = "PermissionsActivity";

    protected final int requestPermissionCode = 0x123;

    private boolean floatingWindowsNeedCallBack = false;

    /**
     * 申请运行时权限
     *
     * @param permissions - 权限集合
     */
    protected void requestRuntimePermission(String... permissions) {
        if (permissions == null || permissions.length == 0 || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            onALLGranted();
            return;
        }
        // 检查权限
        List<String> permitList = new ArrayList<>();
        List<String> noPermitList = new ArrayList<>();
        for (String permission : permissions) {
            if (Manifest.permission.SYSTEM_ALERT_WINDOW.equals(permission)) {
                boolean result = checkFloatingWindowsPermission(this);
                if (result) {
                    onFloatingWindowsResult(true);
                } else {
                    applyFloatingWindowsPermission(this);
                    floatingWindowsNeedCallBack = true;
                }
                continue;
            }
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // 请求权限
                noPermitList.add(permission);
            } else {
                //已授权
                permitList.add(permission);
            }
        }
        if (permitList.size() > 0) {
            String[] array = new String[permitList.size()];
            array = permitList.toArray(array);
            onGranted(array);
        }
        if (noPermitList.size() > 0) {
            String[] array = new String[noPermitList.size()];
            array = noPermitList.toArray(array);
            ActivityCompat.requestPermissions(this, array, requestPermissionCode);
        } else {
            onALLGranted();
        }
    }

    /**
     * 申请权限结果
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == requestPermissionCode) {
            if (grantResults.length > 0) {
                List<String> permitList = new ArrayList<>();
                List<String> noPermitList = new ArrayList<>();
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        noPermitList.add(permissions[i]);
                    } else {
                        // 用户已授权
                        permitList.add(permissions[i]);
                    }
                }
                if (permitList.size() > 0) {
                    String[] array = new String[permitList.size()];
                    array = permitList.toArray(array);
                    onGranted(array);
                }
                if (noPermitList.size() > 0) {
                    String[] array = new String[noPermitList.size()];
                    array = noPermitList.toArray(array);
                    onDenied(array);
                } else {
                    onALLGranted();
                }
            }
        }
    }

    @Override
    public void onGranted(String... permissions) {

    }

    @Override
    public void onDenied(String... deniedPermissions) {

    }

    @Override
    public void onALLGranted() {

    }

    protected void onFloatingWindowsResult(boolean result) {

    }


    private boolean checkFloatingWindowsPermission(Context context) {
        //6.0 版本之后由于 google 增加了对悬浮窗权限的管理，所以方式就统一了
        if (Build.VERSION.SDK_INT < 23) {
            if (RomUtils.checkIsMiuiRom()) {
                return miuiPermissionCheck(context);
            } else if (RomUtils.checkIsMeizuRom()) {
                return meizuPermissionCheck(context);
            } else if (RomUtils.checkIsHuaweiRom()) {
                return huaweiPermissionCheck(context);
            } else if (RomUtils.checkIs360Rom()) {
                return qikuPermissionCheck(context);
            } else if (RomUtils.checkIsOppoRom()) {
                return oppoROMPermissionCheck(context);
            }
        }
        return commonROMPermissionCheck(context);
    }

    private boolean huaweiPermissionCheck(Context context) {
        return HuaweiUtils.checkFloatWindowPermission(context);
    }

    private boolean miuiPermissionCheck(Context context) {
        return MiuiUtils.checkFloatWindowPermission(context);
    }

    private boolean meizuPermissionCheck(Context context) {
        return MeizuUtils.checkFloatWindowPermission(context);
    }

    private boolean qikuPermissionCheck(Context context) {
        return QikuUtils.checkFloatWindowPermission(context);
    }

    private boolean oppoROMPermissionCheck(Context context) {
        return OppoUtils.checkFloatWindowPermission(context);
    }

    private boolean commonROMPermissionCheck(Context context) {
        //最新发现魅族6.0的系统这种方式不好用，单独适配一下
        if (RomUtils.checkIsMeizuRom()) {
            return meizuPermissionCheck(context);
        } else {
            Boolean result = true;
            if (Build.VERSION.SDK_INT >= 23) {
                try {
                    Class clazz = Settings.class;
                    Method canDrawOverlays = clazz.getDeclaredMethod("canDrawOverlays", Context.class);
                    result = (Boolean) canDrawOverlays.invoke(null, context);
                } catch (Exception e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
            }
            return result;
        }
    }

    private void applyFloatingWindowsPermission(Context context) {
        if (Build.VERSION.SDK_INT < 23) {
            if (RomUtils.checkIsMiuiRom()) {
                miuiROMPermissionApply(context);
            } else if (RomUtils.checkIsMeizuRom()) {
                meizuROMPermissionApply(context);
            } else if (RomUtils.checkIsHuaweiRom()) {
                huaweiROMPermissionApply(context);
            } else if (RomUtils.checkIs360Rom()) {
                ROM360PermissionApply(context);
            } else if (RomUtils.checkIsOppoRom()) {
                oppoROMPermissionApply(context);
            }
        } else {
            commonROMPermissionApply(context);
        }
    }

    private void ROM360PermissionApply(final Context context) {
        QikuUtils.applyPermission(context);
    }

    private void huaweiROMPermissionApply(final Context context) {
        HuaweiUtils.applyPermission(context);
    }

    private void meizuROMPermissionApply(final Context context) {
        MeizuUtils.applyPermission(context);
    }

    private void miuiROMPermissionApply(final Context context) {
        MiuiUtils.applyMiuiPermission(context);
    }

    private void oppoROMPermissionApply(final Context context) {
        OppoUtils.applyOppoPermission(context);
    }

    /**
     * 通用 rom 权限申请
     */
    private void commonROMPermissionApply(final Context context) {
        //这里也一样，魅族系统需要单独适配
        if (RomUtils.checkIsMeizuRom()) {
            meizuROMPermissionApply(context);
        } else {
            if (Build.VERSION.SDK_INT >= 23) {
                try {
                    commonROMPermissionApplyInternal(context);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void commonROMPermissionApplyInternal(Context context) throws NoSuchFieldException, IllegalAccessException {
        Class clazz = Settings.class;
        Field field = clazz.getDeclaredField("ACTION_MANAGE_OVERLAY_PERMISSION");

        Intent intent = new Intent(field.get(null).toString());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        context.startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (floatingWindowsNeedCallBack) {
            boolean result = checkFloatingWindowsPermission(this);
            onFloatingWindowsResult(result);
            floatingWindowsNeedCallBack = false;
        }
    }
}
