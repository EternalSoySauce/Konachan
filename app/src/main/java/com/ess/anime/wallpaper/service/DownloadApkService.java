package com.ess.anime.wallpaper.service;

import android.app.IntentService;
import android.content.Intent;

import com.ess.anime.wallpaper.bean.ApkBean;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.http.OkHttp;
import com.ess.anime.wallpaper.listener.DownloadApkProgressListener;
import com.ess.anime.wallpaper.utils.ComponentUtils;
import com.yanzhenjie.kalle.Kalle;
import com.yanzhenjie.kalle.download.SimpleCallback;

import java.io.File;

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
        if (!OkHttp.isUrlInProgressListener(url)) {
            listener = new DownloadApkProgressListener(this, apkBean, intent);
            OkHttp.addUrlToProgressListener(url, listener);
        } else {
            listener = (DownloadApkProgressListener) OkHttp.getProgressListener(url);
            listener.prepareNotification();
        }


        // 下载
        Kalle.Download.get(url)
                .directory(apkBean.localFileFolder)
                .fileName(apkBean.localFileName)
                .onProgress(listener::onProgress)
                .perform(new SimpleCallback() {
                    @Override
                    public void onFinish(String path) {
                        super.onFinish(path);
                        File apkFile = new File(path);
                        // 下载完成，自动启动安装
                        ComponentUtils.installApk(DownloadApkService.this, apkFile, true);
                        // 通知监听器完成下载
                        listener.onFinish();
                    }

                    @Override
                    public void onException(Exception e) {
                        super.onException(e);
                        listener.onError();
                    }

                    @Override
                    public void onEnd() {
                        super.onEnd();
                        OkHttp.removeUrlFromDownloadQueue(url);
                    }
                });
    }
}
