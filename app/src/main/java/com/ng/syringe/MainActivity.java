package com.ng.syringe;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 简单插件化demo
 */
public class MainActivity extends AppCompatActivity {

    Button btnTest;


    TextView showTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                    result = new BugTest().getBug(MainActivity.this);
                } catch (Exception e) {
                    result = e.getMessage();
                }
                LogUtils.d("结果:" + result);
            }
        });
    }
}
