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
 */
public class TestHotFixProxyActivity extends Activity implements View.OnClickListener {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_hot_fix_aty);
        findViewById(R.id.btn_1).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_1:
                Intent i = new Intent(this, GameStubActivity.class);
                i.putExtra(GameStubActivity.INTENT_CLASS_NAME,"com.ng.game.NgGameLevelOneActivity");
                i.putExtra(GameStubActivity.INTENT_RES_PATH,"/storage/emulated/0/AAAAA/game-debug.apk");
                startActivity(i);
                break;
        }
    }
}
