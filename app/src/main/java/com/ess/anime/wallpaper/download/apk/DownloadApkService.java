package com.ess.anime.wallpaper.download.apk;

import android.app.IntentService;
import android.content.Intent;

import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.http.OkHttp;
import com.ess.anime.wallpaper.utils.ComponentUtils;
import com.lzy.okgo.model.Progress;
import com.lzy.okserver.download.DownloadListener;

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
        try {
            OkHttp.startDownloadFile(OkHttp.convertSchemeToHttps(url), apkBean.localFileFolder, apkBean.localFileName, null,
                    new DownloadListener(url) {
                        @Override
                        public void onStart(Progress progress) {
                        }

                        @Override
                        public void onProgress(Progress progress) {
                            listener.onProgress((int) (progress.fraction * 100), progress.currentSize, progress.speed);
                        }

                        @Override
                        public void onError(Progress progress) {
                            listener.onError();
                            OkHttp.removeUrlFromDownloadQueue(url);
                        }

                        @Override
                        public void onFinish(File file, Progress progress) {
                            // 下载完成，自动启动安装
                            ComponentUtils.installApk(DownloadApkService.this, file, true);
                            // 通知监听器完成下载
                            listener.onFinish();
                            OkHttp.removeUrlFromDownloadQueue(url);
                        }

                        @Override
                        public void onRemove(Progress progress) {
                            listener.onRemove();
                            OkHttp.removeUrlFromDownloadQueue(url);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            listener.onError();
            OkHttp.removeUrlFromDownloadQueue(url);
        }
    }
}
