package com.ng.syringe;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.Nullable;

/**
 * @author : jiangzhengnan.jzn
 * @creation : 2021/12/15
 * @description :
 */
public class SplashActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        if (FixDexUtil.isGoingToFix(this)) {
            LogUtils.d("需要热修复");
            //从网络 下载补丁并存储到手机本地
            //https://raw.githubusercontent.com/jiangzhengnan/Syringe/master/DexDir/BugTest.dex


            FixDexUtil.loadFixedDex(this, Environment.getExternalStorageDirectory());
        } else {
            LogUtils.d("不需要热修复");
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    startActivity(new Intent(SplashActivity.this,MainActivity.class));
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}
