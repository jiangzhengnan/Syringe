package com.ng.syringe;

/**
 * @author : jiangzhengnan.jzn@alibaba-inc.com
 * @creation : 2021/12/18
 * @description :
 */
public class Syringe {

    static class Inner {
        static Syringe sInstance = new Syringe();
    }

    private Syringe() {
    }

    /**
     * 热加载文件
     */
    public void loadDex() {

    }


}
