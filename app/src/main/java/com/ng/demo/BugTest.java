package com.ng.demo;


import android.content.Context;

/**
 * @author : jiangzhengnan.jzn
 * @creation : 2021/12/15
 * @description :
 */
public class BugTest {

    public String getBug(Context context) {
        //模拟一个bug
        int i = 10;
        int a = 0;
        return "Hello,Jzn:" + i / a;
    }

//    public String getBug(Context context) {
//        int i = 10;
//        int a = 1;
//        return "Fix bug:" + i / a;
//    }
}
