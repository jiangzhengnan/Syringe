package com.ng.game;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.ng.syringe.Syringe;

/**
 * @author : jiangzhengnan.jzn@alibaba-inc.com
 * @creation : 2021/12/25
 * @description :
 * 通过hook方式可以直接来到第三关,无需特殊处理
 * todo 需要改为asm编织getResources，使得开发无感知
 *
 */
public class NgGameLevelThreeActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_three);
    }

    @Override
    public Resources getResources() {
        Syringe.instance().injectResources(this, super.getResources());
        return super.getResources();
    }

}
