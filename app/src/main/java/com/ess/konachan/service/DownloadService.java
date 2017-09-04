package com.ess.konachan.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.ess.konachan.bean.ImageBean;
import com.ess.konachan.bean.ThumbBean;
import com.ess.konachan.global.Constants;
import com.ess.konachan.http.MyProgressListener;
import com.ess.konachan.http.OkHttp;
import com.ess.konachan.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import me.jessyan.progressmanager.ProgressManager;
import okhttp3.Response;

public class DownloadService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final ArrayList<Runnable> mThreadList = new ArrayList<>();

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
        String url = intent.getStringExtra(Constants.JPEG_URL);
        String bitmapPath = intent.getStringExtra(Constants.BITMAP_PATH);
        ThumbBean thumbBean = intent.getParcelableExtra(Constants.THUMB_BEAN);
        ImageBean imageBean = intent.getParcelableExtra(Constants.IMAGE_BEAN);

        // 绑定下载进度监听器
        MyProgressListener listener;
        if (!OkHttp.getInstance().isUrlInProgressListener(url)) {
            listener = new MyProgressListener(this, imageBean, intent);
            listener.setNotifyThumb(thumbBean.thumbUrl);
            ProgressManager.getInstance().addResponseListener(url, listener);
            OkHttp.getInstance().addUrlToProgressListener(url, listener);
        } else {
            listener = OkHttp.getInstance().getProgressListener(url);
            listener.prepareNotification();
        }

        try {
            // 下载
            Response response = OkHttp.getInstance().execute(url);
            // 保存为图片
            File folder = new File(Constants.IMAGE_DIR);
            if (folder.exists() || folder.mkdirs()) {
                InputStream inputStream = response.body().byteStream();
                File file = new File(bitmapPath);
                FileUtils.streamToFile(inputStream, file);
            }
        } catch (IOException e) {
            e.printStackTrace();
            ProgressManager.getInstance().notifyOnErorr(url, e);
        } finally {
            OkHttp.getInstance().removeUrlFromDownloadQueue(url);
        }
    }

    private void checkToStopService() {
        if (mThreadList.isEmpty()) {
            stopSelf();
        }
    }
}
