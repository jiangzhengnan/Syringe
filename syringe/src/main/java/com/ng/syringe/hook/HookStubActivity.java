package com.ng.syringe.hook;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.ng.syringe.R;

/**
 * @author : jiangzhengnan.jzn@alibaba-inc.com
 * @creation : 2022/01/10
 * @description :
 * 占坑Aty
 */
public class HookStubActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty);
    }

}
