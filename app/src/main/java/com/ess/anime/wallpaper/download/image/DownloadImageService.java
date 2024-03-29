package com.ess.anime.wallpaper.download.image;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import com.ess.anime.wallpaper.download.image.notification.MyNotification;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.http.HandlerFuture;
import com.ess.anime.wallpaper.http.OkHttp;
import com.ess.anime.wallpaper.utils.BitmapUtils;
import com.ess.anime.wallpaper.utils.FileUtils;
import com.lzy.okgo.model.Progress;
import com.lzy.okserver.download.DownloadListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DownloadImageService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final List<Runnable> mThreadList = new ArrayList<>();
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());
    private MyNotification mNotify;

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotify = new MyNotification();
            mNotify.show(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mNotify != null) {
            mNotify.stop();
            mNotify = null;
        }
    }

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
        try {
            mMainHandler.post(() -> DownloadImageManager.getInstance().addOrUpdate(downloadBean));
            OkHttp.startDownloadFile(OkHttp.convertSchemeToHttps(url), tempFolder.getAbsolutePath(), tempName, null,
                    new DownloadListener(url) {
                        @Override
                        public void onStart(Progress progress) {
                            DownloadImageManager.getInstance().addOrUpdate(downloadBean);
                        }

                        @Override
                        public void onProgress(Progress progress) {
                            listener.onProgress((int) (progress.fraction * 100), progress.currentSize, progress.totalSize, progress.speed);
                            DownloadImageManager.getInstance().addOrUpdate(downloadBean);
                        }

                        @Override
                        public void onError(Progress progress) {
                            listener.onError();
                            DownloadImageManager.getInstance().addOrUpdate(downloadBean);
                            OkHttp.removeUrlFromDownloadQueue(url);
                        }

                        @Override
                        public void onFinish(File file, Progress progress) {
                            HandlerFuture.ofWork(tempFile)
                                    .applyThen(tempFile -> {
                                        // 下载成功，保存为图片
                                        File saveFile = new File(savePath);
                                        boolean success = FileUtils.moveFile(tempFile, saveFile);
                                        return success ? saveFile : null;
                                    })
                                    .runOn(HandlerFuture.IO.UI)
                                    .applyThen(saveFile -> {
                                        // 添加图片到媒体库（刷新相册）
                                        if (saveFile != null) {
                                            BitmapUtils.insertToMediaStore(DownloadImageService.this, saveFile);
                                        }
                                        // 通知监听器完成下载 （由于lolibooru监听不到下载进度，所以在这里进行弥补）
                                        listener.onFinish();
                                        DownloadImageManager.getInstance().addOrUpdate(downloadBean);
                                        OkHttp.removeUrlFromDownloadQueue(url);
                                    });
                        }

                        @Override
                        public void onRemove(Progress progress) {
                            listener.onRemove();
                            DownloadImageManager.getInstance().remove(downloadBean);
                            OkHttp.removeUrlFromDownloadQueue(url);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            listener.onError();
            mMainHandler.post(() -> DownloadImageManager.getInstance().addOrUpdate(downloadBean));
            OkHttp.removeUrlFromDownloadQueue(url);
        }
    }

    private void checkToStopService() {
        if (mThreadList.isEmpty()) {
            stopSelf();
        }
    }
}
