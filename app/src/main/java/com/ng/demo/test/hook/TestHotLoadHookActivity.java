package com.ng.demo.test.hook;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.ng.demo.R;
import com.ng.syringe.Syringe;

/**
 * @author : jiangzhengnan.jzn@alibaba-inc.com
 * @creation : 2021/12/25
 * @description :
 * <p>
 * 测试通过Hook方式加载各种组件
 */
public class TestHotLoadHookActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_hot_load_hook_aty);
        Syringe.getInstance(this).init();

        findViewById(R.id.btn_1).setOnClickListener(this);
        setTitle("动态加载(Hook方式)");
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_1:

                break;

        }
    }
}
