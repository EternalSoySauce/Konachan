package com.lss.anime.wallpaper.service;

import android.app.IntentService;
import android.content.Intent;

import com.lss.anime.wallpaper.bean.ApkBean;
import com.lss.anime.wallpaper.global.Constants;
import com.lss.anime.wallpaper.http.OkHttp;
import com.lss.anime.wallpaper.listener.DownloadApkProgressListener;
import com.lss.anime.wallpaper.utils.ComponentUtils;
import com.lss.anime.wallpaper.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import me.jessyan.progressmanager.ProgressManager;
import okhttp3.Response;

public class DownloadApkService extends IntentService {

    public DownloadApkService() {
        super("DownloadApkService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            downloadApk(intent);
        }
    }

    private void downloadApk(Intent intent) {
        ApkBean apkBean = intent.getParcelableExtra(Constants.APK_BEAN);
        String url = apkBean.apkUrl;

        // 绑定下载进度监听器
        DownloadApkProgressListener listener;
        if (!OkHttp.getInstance().isUrlInProgressListener(url)) {
            listener = new DownloadApkProgressListener(this, apkBean, intent);
            ProgressManager.getInstance().addResponseListener(url, listener);
            OkHttp.getInstance().addUrlToProgressListener(url, listener);
        } else {
            listener = (DownloadApkProgressListener) OkHttp.getInstance().getProgressListener(url);
            listener.prepareNotification();
        }

        Response response = null;
        try {
            // 下载
            File apkFile = new File(apkBean.localFilePath);
            response = OkHttp.getInstance().execute(url);
            InputStream inputStream = response.body().byteStream();
            FileUtils.streamToFile(inputStream, apkFile);
            ComponentUtils.installApk(this, apkFile, true);
        } catch (IOException e) {
            e.printStackTrace();
            ProgressManager.getInstance().notifyOnErorr(url, e);
        } finally {
            if (response != null) {
                response.close();
            }
            OkHttp.getInstance().removeUrlFromDownloadQueue(url);
        }
    }
}
