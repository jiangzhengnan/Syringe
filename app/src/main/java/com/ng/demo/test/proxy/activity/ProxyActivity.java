package com.ng.demo.test.proxy.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


/**
 * 被代理器完全支配的activity
 *
 * @see ActivityProxy
 */

public class ProxyActivity extends Activity {
    ActivityProxy proxy;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        proxy = ObjectFactory.make(TextActivityProxy.class.getName(),this);

        if (proxy != null) {
            proxy.onCreate(savedInstanceState);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (proxy != null) {
            proxy.onStart();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (proxy!=null) {
            proxy.onBackPressed();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (proxy!=null&&proxy.onKeyDown(keyCode, event)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (proxy!=null&&proxy.onKeyUp(keyCode, event)) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (proxy!=null&&proxy.onKeyLongPress(keyCode, event)) {
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (proxy != null) {
            proxy.onResume();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (proxy!=null) {
            proxy.onNewIntent(intent);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (proxy != null) {
            proxy.onRestart();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (proxy != null) {
            proxy.onPause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (proxy != null) {
            proxy.onStop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (proxy != null) {
            proxy.onDestroy();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (proxy != null) {
            proxy.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (proxy != null) {
            proxy.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
