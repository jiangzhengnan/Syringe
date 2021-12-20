package com.ng.syringe.download;

import android.content.Context;
import android.os.Environment;

import androidx.annotation.NonNull;

import com.ng.syringe.util.LogUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author : jiangzhengnan.jzn@alibaba-inc.com
 * @creation : 2021/12/18
 * @description :
 */
public class DownloadHelper {
    private static final String dexDirPath = "/AAAAA";

    public static String getDexDirFilePath(@NonNull Context context) {
        return getSDPath(context) +"/AAAAA";
    }

    public static void downloadPlug(
            @NonNull Context context,
            @NonNull String downloadUrl,
            @NonNull String fileName,
            @NonNull DownloadCallBack callBack) {
        callBack.onStart();
        new Thread(() -> {
            try {
                String saveDirPath = getDexDirFilePath(context);
                String savePath = saveDirPath + "/" + fileName;
                LogUtils.d("下载文件夹路径:" + saveDirPath);
                File downLoadDir = new File(saveDirPath);
                if (!downLoadDir.exists()) {
                    downLoadDir.mkdir();
                }
                //下载后的文件名
                File downLoadFile = new File(savePath);
                LogUtils.d("下载文件路径:" + savePath);
                if (downLoadFile.exists()) {
                    downLoadFile.delete();
                } else {
                    downLoadFile.createNewFile();
                }
                URL url = new URL(downloadUrl);
                //打开连接
                URLConnection conn = url.openConnection();
                //打开输入流
                InputStream is = conn.getInputStream();
                //获得长度
                int contentLength = conn.getContentLength();

                //创建字节流
                byte[] bs = new byte[1024];
                int len;
                int nowLen = 0;
                OutputStream os = new FileOutputStream(downLoadFile);
                //写数据
                while ((len = is.read(bs)) != -1) {
                    os.write(bs, 0, len);
                    nowLen += len;
                    int progress = (int) (((float) nowLen / (float) contentLength) * 100);
                    callBack.onProgress(progress);
                }
                //完成后关闭流
                os.close();
                is.close();
                callBack.onCompleted(savePath);
            } catch (Exception e) {
                e.printStackTrace();
                callBack.onError(-1, e.getMessage());
            }
        }).start();
    }


    public static String getSDPath(Context context) {
        File sdDir;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);// 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();// 获取SD卡根目录
//            if (Build.VERSION.SDK_INT >= 29) {
//                //Android10之后
//                sdDir = context.getExternalFilesDir(null);
//            } else {
//                sdDir = Environment.getExternalStorageDirectory();// 获取SD卡根目录
//            }
        } else {
            sdDir = Environment.getRootDirectory();// 获取跟目录
        }
        return sdDir.toString();
    }
}
