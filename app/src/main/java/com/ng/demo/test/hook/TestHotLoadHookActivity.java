package com.ng.demo.test.hook;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.ng.demo.R;
import com.ng.syringe.load.hook.stub.HookStubActivity;

/**
 * @author : jiangzhengnan.jzn@alibaba-inc.com
 * @creation : 2021/12/25
 * @description :
 * <p>
 * 测试通过Hook方式加载各种组件 (VirtualAPK等框架采用的方式)
 * todo
 * 目前需要被代理的Aty主动去反射 addAssetPath 加载资源，不然会出现资源加载不到的问题
 * 需要改为自动全局修改
 */
public class TestHotLoadHookActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_hot_load_hook_aty);

        findViewById(R.id.btn_1).setOnClickListener(this);
        setTitle("动态加载(Hook方式)");

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_1:
                startToNgGameLevelThreeActivity();
                break;
        }
    }

    /**
     * 目标Aty:game包中的 NgGameLevelThreeActivity
     */
    private void startToNgGameLevelThreeActivity() {
        Intent i = new Intent(this, HookStubActivity.class);
        i.putExtra("targetActivity", "com.ng.game.NgGameLevelThreeActivity");
        startActivity(i);
    }
}
