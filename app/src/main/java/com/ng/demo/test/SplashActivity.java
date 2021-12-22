package com.ng.demo.test;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.ng.demo.R;
import com.ng.demo.permission.PermissionsActivity;
import com.ng.demo.test.proxy.activity.TempActivity;
import com.ng.syringe.Syringe;

/**
 * @author : jiangzhengnan.jzn
 * @creation : 2021/12/15
 * @description :
 */
public class SplashActivity extends PermissionsActivity {
    private static final int requestFullFilePermissionCode = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        requestRuntimePermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.INTERNET);
    }

    @Override
    public void onALLGranted() {
        super.onALLGranted();
        checkFullFilePermission();
    }


    private void checkFullFilePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R ||
                Environment.isExternalStorageManager()) {
            Toast.makeText(this, "已获得访问所有文件权限", Toast.LENGTH_SHORT).show();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    jumpHome();
                }
            });
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setMessage("本程序需要您同意允许访问所有文件权限")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivityForResult(new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION), requestFullFilePermissionCode);
                        }
                    });
            builder.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == requestFullFilePermissionCode) {
            checkFullFilePermission();
        }
    }

    private void jumpHome() {
        Syringe.getInstance(this).init();


        //startActivity(new Intent(SplashActivity.this, HotFixActivity.class));
        //启动一个受到代理的activity
        startActivity(new Intent(this, TempActivity.class));
        finish();
    }
}
