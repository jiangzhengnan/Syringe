package com.ng.syringe.load;

import android.content.res.Resources;

import com.ng.syringe.util.LogUtils;


public class SplitResUtils {

    public static int getId(String id, Resources resources, String pkgName) {
        String[] idElements = id.split("\\.");
        if (idElements.length < 3) {
            return -1;
        }
        String idname = idElements[idElements.length - 1];
        String idtype = idElements[idElements.length - 2];
        String R = idElements[idElements.length - 3];
        LogUtils.d("find id: " + id + " from pkg:" + pkgName);
        return resources.getIdentifier(idname, idtype, pkgName);
    }
}
