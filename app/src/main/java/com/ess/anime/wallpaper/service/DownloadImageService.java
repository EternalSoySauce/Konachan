package com.ess.anime.wallpaper.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.ess.anime.wallpaper.bean.DownloadBean;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.http.OkHttp;
import com.ess.anime.wallpaper.listener.DownloadImageProgressListener;
import com.ess.anime.wallpaper.utils.BitmapUtils;
import com.ess.anime.wallpaper.utils.FileUtils;
import com.yanzhenjie.kalle.Kalle;
import com.yanzhenjie.kalle.download.SimpleCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DownloadImageService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final List<Runnable> mThreadList = new ArrayList<>();

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                downloadBitmap(intent);
                synchronized (mThreadList) {
                    mThreadList.remove(this);
                    checkToStopService();
                }
            }
        };

        synchronized (mThreadList) {
            mThreadList.add(runnable);
            new Thread(runnable).start();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void downloadBitmap(Intent intent) {
        if (intent == null) {
            // 下载过程中若关闭app会导致intent为null
            // 此时终止下载并清除所有notification
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();
            return;
        }

        DownloadBean downloadBean = intent.getParcelableExtra(Constants.DOWNLOAD_BEAN);
        String url = downloadBean.downloadUrl;
        String savePath = downloadBean.savePath;

        // 绑定下载进度监听器
        DownloadImageProgressListener listener;
        if (!OkHttp.isUrlInProgressListener(url)) {
            listener = new DownloadImageProgressListener(this, downloadBean, intent);
            OkHttp.addUrlToProgressListener(url, listener);
        } else {
            listener = (DownloadImageProgressListener) OkHttp.getProgressListener(url);
            listener.prepareNotification();
        }

        // 临时下载文件
        File tempFolder = new File(Constants.IMAGE_TEMP);
        String tempName = savePath.substring(savePath.lastIndexOf("/") + 1, savePath.lastIndexOf("."));
        File tempFile = new File(tempFolder, tempName);
        if (!tempFolder.exists() && !tempFolder.mkdirs()) {
            OkHttp.removeUrlFromDownloadQueue(url);
            return;
        }

        // 下载
        Kalle.Download.get(url)
                .directory(tempFolder.getAbsolutePath())
                .fileName(tempName)
                .onProgress(listener::onProgress)
                .perform(new SimpleCallback() {
                    @Override
                    public void onFinish(String path) {
                        super.onFinish(path);
                        // 下载成功，保存为图片
                        File folder = new File(Constants.IMAGE_DIR);
                        if (folder.exists() || folder.mkdirs()) {
                            File file = new File(savePath);
                            FileUtils.copyFile(tempFile, file);
                            // 添加图片到媒体库（刷新相册）
                            BitmapUtils.insertToMediaStore(DownloadImageService.this, file);
                            // 通知监听器完成下载 （由于lolibooru监听不到下载进度，所以在这里进行弥补）
                            listener.onFinish();
                        }
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
                        tempFile.delete();
                    }
                });
    }

    private void checkToStopService() {
        if (mThreadList.isEmpty()) {
            stopSelf();
        }
    }
}
