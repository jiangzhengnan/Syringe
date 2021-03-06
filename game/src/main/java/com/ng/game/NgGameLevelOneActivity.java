package com.ng.game;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.ng.syringe.load.proxy.base.ActivityProxyAbs;
import com.ng.syringe.load.proxy.stub.ProxyStubActivity;
import com.ng.syringe.load.proxy.ProxyActivity;

/**
 * @author : jiangzhengnan.jzn@alibaba-inc.com
 * @creation : 2021/12/25
 * @description :
 */
public class NgGameLevelOneActivity extends ActivityProxyAbs {

    public NgGameLevelOneActivity(ProxyActivity acty) {
        super(acty);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mActy.setContentView((getSplitResId("R.layout.activity_game_one")));
        mActy.findViewById(getSplitResId("R.id.btn_jump_next")).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpNext();
            }
        });
    }

    private void jumpNext() {
        Intent i = new Intent(mActy, ProxyStubActivity.class);
        i.putExtra(ProxyStubActivity.INTENT_CLASS_NAME,"com.ng.game.NgGameLevelTwoActivity");
        i.putExtra(ProxyStubActivity.INTENT_RES_PATH,"/storage/emulated/0/AAAAA/game-debug.apk");
        mActy.startActivity(i);
        mActy.finish();
    }

}
