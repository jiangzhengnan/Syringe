package com.ng.syringe.load.proxy.base;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ng.syringe.load.proxy.ProxyActivity;
import com.ng.syringe.load.SplitResUtils;

public abstract class ActivityProxyAbs {

    public ProxyActivity mActy;

    public ActivityProxyAbs(ProxyActivity acty) {
        mActy = acty;
    }

    public abstract void onCreate(@Nullable Bundle savedInstanceState);

    public int getSplitResId(String id) {
        int result = SplitResUtils.getId(id, mActy.getResources(), mActy.mPackageName);
        return result;
    }


    public void onStart() {
    }

    public void onResume() {
    }

    public void onRestart() {
    }

    public void onPause() {
    }

    public void onStop() {
    }

    public void onDestroy() {
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    public void onBackPressed() {
    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    public void onNewIntent(Intent intent) {

    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return false;
    }

    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        return false;
    }
}
