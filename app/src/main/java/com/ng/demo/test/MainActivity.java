package com.ng.demo.test;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.ng.demo.R;
import com.ng.demo.permission.PermissionsActivity;
import com.ng.demo.test.fix.TestHotFixBugActivity;
import com.ng.demo.test.hook.TestHotLoadHookActivity;
import com.ng.demo.test.proxy.TestHotLoadProxyActivity;

/**
 * @author : jiangzhengnan.jzn
 * @creation : 2021/12/15
 * @description :
 */
public class MainActivity extends PermissionsActivity {
    private static final int requestFullFilePermissionCode = 1;
    private LinearLayout mContainerLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContainerLayout = findViewById(R.id.ll_container);
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
                    initView();

                    //startActivity(new Intent(MainActivity.this, TestHotLoadHookActivity.class));
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

    private void initView() {
        addFunction("热修复普通类", TestHotFixBugActivity.class);
        addFunction("热加载组件 (通过代理实现，开发有感知)", TestHotLoadProxyActivity.class);
        addFunction("热加载组件 (通过Hook实现，开发无感知)", TestHotLoadHookActivity.class);

    }

    private void addFunction(String showStr, Class targetClass) {
        Button btn = new Button(this);
        btn.setText(showStr);
        btn.setTextSize(12);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, targetClass));
            }
        });
        mContainerLayout.addView(btn);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == requestFullFilePermissionCode) {
            checkFullFilePermission();
        }
    }

}
