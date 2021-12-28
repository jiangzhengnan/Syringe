package com.ng.game.broad;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.ng.syringe.load.ProxyActivity;
import com.ng.syringe.load.base.ActivityProxyAbs;

/**
 * @author : jiangzhengnan.jzn@alibaba-inc.com
 * @creation : 2021/12/25
 * @description :
 */
public class NgGameBroadcastActivity extends ActivityProxyAbs {

    public NgGameBroadcastActivity(ProxyActivity acty) {
        super(acty);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mActy.setContentView((getSplitResId("R.layout.activity_game_broad_cast")));
        mActy.findViewById(getSplitResId("R.id.btn_test_broad_cast_1")).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerBroadcast();
            }
        });
        mActy.findViewById(getSplitResId("R.id.btn_test_broad_cast_2")).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroadcast();
            }
        });
    }

    private void sendBroadcast() {
        Intent intent = new Intent();
        intent.setAction("com.ng.game.receive");
        mActy.sendBroadcast(intent);
    }

    private void registerBroadcast() {
        IntentFilter intent = new IntentFilter();
        intent.addAction("com.ng.game.receive");
        NgDynamicBroadcastReceiver ngDynamicBroadcastReceiver = new NgDynamicBroadcastReceiver();
        //ngDynamicBroadcastReceiver.attach(mActy);
        mActy.registerReceiver(ngDynamicBroadcastReceiver, intent);
    }



}
