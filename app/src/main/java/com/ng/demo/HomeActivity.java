package com.ng.demo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ng.syringe.util.LogUtils;

/**
 * 简单插件化demo
 */
public class HomeActivity extends AppCompatActivity {

    Button btnTest;


    TextView showTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        init();

    }

    private void init() {
        btnTest = findViewById(R.id.btn_1);
        showTv = findViewById(R.id.tv_test1);
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String result = "";
                try {
                    Class bugTestClass = Class.forName("com.ng.demo.BugTest");
                    BugTest instance = (BugTest) bugTestClass.newInstance();
                    result = instance.getBug(HomeActivity.this);
                } catch (Exception e) {
                    result = e.getMessage();
                }
                LogUtils.d("结果:" + result);
                showTv.setText(result);
            }
        });
    }
}
