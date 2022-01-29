package com.ng.demo.test.fix;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ng.demo.R;
import com.ng.syringe.util.LogUtils;

/**
 * @author : jiangzhengnan.jzn@alibaba-inc.com
 * @creation : 2021/12/25
 * @description :
 * 测试热修复
 */
public class TestHotFixBugActivity extends Activity {

    Button btnTest;

    TextView showTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_hot_fix_bug);
        init();
    }

    private void init() {
        btnTest = findViewById(R.id.btn_1);
        showTv = findViewById(R.id.tv_test1);
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTest();
            }
        });
        setTitle("测试修复普通类");
    }

    private void startTest() {
        String result;
        try {
            result = new BugTest().getBug();
        } catch (Exception e) {
            result = e.getMessage();
        }
        LogUtils.d("测试结果:" + result);
        showTv.setText(result);
    }

}
