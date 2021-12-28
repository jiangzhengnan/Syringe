package com.ng.demo.test.proxy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.ng.demo.R;
import com.ng.syringe.load.GameStubActivity;

/**
 * @author : jiangzhengnan.jzn@alibaba-inc.com
 * @creation : 2021/12/25
 * @description :
 * 测试通过代理方式热加载各种组件
 * 在MainActivity中已经加载了可加载资源至classloader中
 *
 * 1.Activity
 * 通过代理实现，在ProxyActivity中，onCreate先去加载插件apk，然后通过classLoad加载真正的实现Activity。
 *
 * 2.动态广播
 * 通过重写代理Activity(ProxyActivity)的registerReceiver，将插件aty中的注册行为拦截，委托给广播代理类
 * ProxyReceive实现，在ProxyReceive中通过classLoad加载真正的广播实现类并进行注册。
 * (其实对于动态广播意义不大，直接在代理Aty中注册也是可以的)
 *
 * 3.静态广播
 */
public class TestHotLoadProxyActivity extends Activity implements View.OnClickListener {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_hot_fix_aty);
        findViewById(R.id.btn_1).setOnClickListener(this);
        findViewById(R.id.btn_2).setOnClickListener(this);
        findViewById(R.id.btn_3).setOnClickListener(this);
        setTitle("动态加载(代理方式)");
    }

    @Override
    public void onClick(View v) {
        Intent i = new Intent(this, GameStubActivity.class);
        switch (v.getId()) {
            case R.id.btn_1:
                i.putExtra(GameStubActivity.INTENT_CLASS_NAME, "com.ng.game.NgGameLevelOneActivity");
                i.putExtra(GameStubActivity.INTENT_RES_PATH, "/storage/emulated/0/AAAAA/game-debug.apk");
                startActivity(i);
                break;
            case R.id.btn_2:
                i.putExtra(GameStubActivity.INTENT_CLASS_NAME, "com.ng.game.broad.NgGameBroadcastActivity");
                i.putExtra(GameStubActivity.INTENT_RES_PATH, "/storage/emulated/0/AAAAA/game-debug.apk");
                startActivity(i);
                break;
            case R.id.btn_3:

                break;

        }
    }
}