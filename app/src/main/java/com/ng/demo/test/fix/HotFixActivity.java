package com.ng.demo.test.fix;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ng.demo.R;
import com.ng.demo.test.BugTest;
import com.ng.syringe.download.DownloadCallBack;
import com.ng.syringe.download.DownloadHelper;
import com.ng.syringe.load.FixDexUtil;
import com.ng.syringe.util.LogUtils;

/**
 * 测试热修复
 */
public class HotFixActivity extends AppCompatActivity {
    String remoteDexPath = "https://qfile.okntc.com/BugTest.dex";

    Button btnTest;

    TextView showTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        hotFix();
        init();

    }

    private void hotFix() {

    }

    private void init() {
        btnTest = findViewById(R.id.btn_1);
        showTv = findViewById(R.id.tv_test1);
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String result = "";
                try {
                    result = new BugTest().getBug(HotFixActivity.this);
//                    Class bugTestClass = Class.forName("com.ng.demo.test.BugTest");
//                    BugTest instance = (BugTest) bugTestClass.newInstance();
//                    result = instance.getBug(HomeActivity.this);
                } catch (Exception e) {
                    result = e.getMessage();
                }
                LogUtils.d("结果:" + result);
                showTv.setText(result);
            }
        });
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
    }

}
