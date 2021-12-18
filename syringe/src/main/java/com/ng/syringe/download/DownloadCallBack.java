package com.ng.syringe.download;

/**
 * @author : jiangzhengnan.jzn@alibaba-inc.com
 * @creation : 2021/12/18
 * @description :
 */
public interface DownloadCallBack {

    void onStart();

    void onCanceled();

    void onCanceling();

    void onProgress(long progress);

    void onCompleted(String filePath);

    void onError(int errorCode, String errorMsg);

}
