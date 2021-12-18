package com.ng.demo;

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

import com.ng.syringe.download.DownloadCallBack;
import com.ng.syringe.download.DownloadHelper;
import com.ng.syringe.load.FixDexUtil;
import com.ng.syringe.util.LogUtils;

/**
 * @author : jiangzhengnan.jzn
 * @creation : 2021/12/15
 * @description :
 */
public class SplashActivity extends PermissionsActivity {

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

    String remoteDexPath = "https://qfile.okntc.com/BugTest.dex";

    @Override
    public void onALLGranted() {
        super.onALLGranted();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R ||
                Environment.isExternalStorageManager()) {
            Toast.makeText(this, "已获得访问所有文件权限", Toast.LENGTH_SHORT).show();
            //downLoadDex();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    beginHotFix();
                }
            });
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setMessage("本程序需要您同意允许访问所有文件权限")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION));
                        }
                    });
            builder.show();
        }
    }

    //从assets里读取
    private void downLoadDex() {
        LogUtils.d("下载插件: " + remoteDexPath);
        DownloadHelper.downloadPlug(
                this,
                remoteDexPath,
                "BugTest.dex",
                new DownloadCallBack() {
                    @Override
                    public void onStart() {
                        LogUtils.d("下载开始");
                    }

                    @Override
                    public void onCanceled() {

                    }

                    @Override
                    public void onCanceling() {

                    }

                    @Override
                    public void onProgress(long progress) {
                        LogUtils.d("下载中 " + progress);
                    }

                    @Override
                    public void onCompleted(String filePath) {
                        LogUtils.d("下载完成");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                beginHotFix();
                            }
                        });
                    }

                    @Override
                    public void onError(int errorCode, String errorMsg) {
                        LogUtils.d("下载错误 " + errorCode + " " + errorMsg);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                beginHotFix();
                            }
                        });
                    }
                });
    }

    //开始热修复
    private void beginHotFix() {
        if (FixDexUtil.isGoingToFix(this)) {
            LogUtils.d("需要热修复 dexPath");
            FixDexUtil.loadFixedDex(this);
        } else {
            LogUtils.d("不需要热修复");
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                jumpHome();
            }
        }).start();
    }

    private void jumpHome() {
        startActivity(new Intent(SplashActivity.this, HomeActivity.class));
        finish();
    }
}
