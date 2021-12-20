package com.ng.demo.permission;

public interface IPermissionListener {

    void onGranted(String... permissions);

    void onDenied(String... deniedPermissions);

    void onALLGranted();
}
