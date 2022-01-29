package com.ng.novel;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

/**
 * @author : jiangzhengnan.jzn@alibaba-inc.com
 * @creation : 2021/12/25
 * @description :
 * 通过hook方式可以直接来到第三关,无需特殊处理
 * asm编织getResources，使得开发无感知
 */
public class NgNovelActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novel);
    }
}
