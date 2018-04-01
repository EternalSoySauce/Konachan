package com.ess.anime.wallpaper.listener;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.app.NotificationCompat;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.bean.ApkBean;
import com.ess.anime.wallpaper.service.DownloadApkService;
import com.ess.anime.wallpaper.utils.ComponentUtils;
import com.ess.anime.wallpaper.utils.FileUtils;

import java.io.File;

import me.jessyan.progressmanager.ProgressListener;
import me.jessyan.progressmanager.body.ProgressInfo;

public class DownloadApkProgressListener implements ProgressListener {

    private Context mContext;
    private ApkBean mApkBean;
    private String mFileAvailable;

    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mNotifyBuilder;
    private int mNotifyId;
    private PendingIntent mReloadIntent;
    private PendingIntent mInstallIntent;

    public DownloadApkProgressListener(Context context, ApkBean apkBean, Intent intent) {
        mContext = context;
        mApkBean = apkBean;
        prepareNotification();
        createReloadPendingIntent(intent);
    }

    public void prepareNotification() {
        // TODO 适配8.0
        if (mNotifyBuilder == null) {
            mFileAvailable = FileUtils.computeFileSize(mApkBean.apkSize);
            mNotifyBuilder = new NotificationCompat.Builder(mContext);
            mNotifyId = (int) System.currentTimeMillis();
            mNotifyManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

            String title = mContext.getString(R.string.app_name);
            String ticker = title + mContext.getString(R.string.download_started);
            mNotifyBuilder.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher));
            mNotifyBuilder.setTicker(ticker);
            mNotifyBuilder.setContentTitle(title);
            mNotifyBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        }
        mNotifyBuilder.setSmallIcon(R.drawable.ic_notification_download);
        mNotifyBuilder.setContentText("0B / " + mFileAvailable);
        mNotifyBuilder.setProgress(100, 0, false);
        mNotifyBuilder.setOngoing(true);
        mNotifyBuilder.setContentIntent(null);
        mNotifyManager.notify(mNotifyId, mNotifyBuilder.build());
    }

    @Override
    public void onProgress(ProgressInfo progressInfo) {
        int progress = progressInfo.getPercent();
        mNotifyBuilder.setProgress(100, progress, false);
        String content = FileUtils.computeFileSize(progressInfo.getCurrentbytes())
                + " / " + mFileAvailable;
        mNotifyBuilder.setContentText(content);
        mNotifyManager.notify(mNotifyId, mNotifyBuilder.build());

        if (progressInfo.isFinish()) {
            // 下载完成
            createInstallPendingIntent();
            performFinish();
        }
    }

    @Override
    public void onError(long id, Exception e) {
        mNotifyBuilder.setSmallIcon(R.drawable.ic_notification_download_failed);
        mNotifyBuilder.setContentText(mContext.getString(R.string.download_failed));
        mNotifyBuilder.setOngoing(false);
        mNotifyBuilder.setContentIntent(mReloadIntent);
        mNotifyManager.notify(mNotifyId, mNotifyBuilder.build());
    }

    private void createReloadPendingIntent(Intent intent) {
        Intent reloadIntent = new Intent(mContext, DownloadApkService.class);
        reloadIntent.putExtras(intent);
        mReloadIntent = PendingIntent.getService(mContext, mNotifyId,
                reloadIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void createInstallPendingIntent() {
        File apkFile = new File(mApkBean.localFilePath);
        if (apkFile.exists()) {
            Intent installIntent = ComponentUtils.installApk(mContext, apkFile, false);
            if (installIntent != null) {
                mInstallIntent = PendingIntent.getActivity(mContext, mNotifyId,
                        installIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            }
        }
    }

    public void performFinish() {
        String finish = mFileAvailable + " / " + mContext.getString(R.string.download_finished);
        mNotifyBuilder.setProgress(100, 100, false);
        mNotifyBuilder.setSmallIcon(R.drawable.ic_notification_download_succeed);
        mNotifyBuilder.setContentText(finish);
        mNotifyBuilder.setOngoing(false);
        mNotifyBuilder.setContentIntent(mInstallIntent);
        mNotifyManager.notify(mNotifyId, mNotifyBuilder.build());
    }

}
