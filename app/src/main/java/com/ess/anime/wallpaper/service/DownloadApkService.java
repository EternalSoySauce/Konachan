package com.ess.anime.wallpaper.service;

import android.app.IntentService;
import android.content.Intent;

import com.ess.anime.wallpaper.bean.ApkBean;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.http.OkHttp;
import com.ess.anime.wallpaper.listener.DownloadApkProgressListener;
import com.ess.anime.wallpaper.utils.ComponentUtils;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;

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
        OkGo.<File>get(OkHttp.convertSchemeToHttps(url))
                .execute(new FileCallback(apkBean.localFileFolder, apkBean.localFileName) {
                    @Override
                    public void onSuccess(Response<File> response) {
                        File apkFile = response.body();
                        // 下载完成，自动启动安装
                        ComponentUtils.installApk(DownloadApkService.this, apkFile, true);
                        // 通知监听器完成下载
                        listener.onFinish();
                    }

                    @Override
                    public void onError(Response<File> response) {
                        super.onError(response);
                        listener.onError();
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                        OkHttp.removeUrlFromDownloadQueue(url);
                    }

                    @Override
                    public void downloadProgress(Progress progress) {
                        super.downloadProgress(progress);
                        listener.onProgress((int) (progress.fraction * 100), progress.currentSize, progress.speed);
                    }
                });
    }
}
