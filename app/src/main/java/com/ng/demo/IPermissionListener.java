package com.ng.demo;

public interface IPermissionListener {

    void onGranted(String... permissions);

    void onDenied(String... deniedPermissions);

    void onALLGranted();
}
