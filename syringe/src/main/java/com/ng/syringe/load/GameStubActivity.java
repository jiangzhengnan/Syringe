package com.ng.syringe.load;

import android.os.Bundle;

import androidx.annotation.Nullable;

/**
 * 占位代理Activity
 */
public class GameStubActivity extends ProxyActivity {

    public static final String INTENT_CLASS_NAME = "INTENT_CLASS_NAME";

    public static final String INTENT_RES_PATH = "INTENT_CLASS_PATH";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        mTargetActivityClassName = getIntent().getStringExtra(INTENT_CLASS_NAME);
        mTargetResPath = getIntent().getStringExtra(INTENT_RES_PATH);
        super.onCreate(savedInstanceState);
    }

}
