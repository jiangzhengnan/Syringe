package com.ng.demo.test.fix;


/**
 * @author : jiangzhengnan.jzn
 * @creation : 2021/12/15
 * @description :
 */
public class BugTest {

    public String getBug() {
        int i = 10;
        int a = 0;
        return "Hello,Jzn:" + i / a;
    }

//    public String getBug() {
//        //模拟一个bug
//        int i = 10;
//        int a = 1;
//        return "Fix bug:" + i / a;
//    }
}
