package com.ng.game;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.ng.syringe.load.proxy.base.ActivityProxyAbs;
import com.ng.syringe.load.proxy.ProxyActivity;

/**
 * @author : jiangzhengnan.jzn@alibaba-inc.com
 * @creation : 2021/12/25
 * @description :
 */
public class NgGameLevelTwoActivity extends ActivityProxyAbs {

    public NgGameLevelTwoActivity(ProxyActivity acty) {
        super(acty);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        //setContentView(R.layout.activity_game_two);
        mActy.setContentView((getSplitResId("R.layout.activity_game_two")));
    }

}
