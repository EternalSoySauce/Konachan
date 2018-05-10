package com.ess.anime.wallpaper.listener;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.bean.DownloadBean;
import com.ess.anime.wallpaper.other.GlideApp;
import com.ess.anime.wallpaper.other.MyGlideModule;
import com.ess.anime.wallpaper.service.DownloadImageService;
import com.ess.anime.wallpaper.ui.activity.CollectionActivity;
import com.ess.anime.wallpaper.utils.FileUtils;
import com.ess.anime.wallpaper.utils.UIUtils;

import me.jessyan.progressmanager.ProgressListener;
import me.jessyan.progressmanager.body.ProgressInfo;

public class DownloadImageProgressListener implements ProgressListener, RequestListener<Bitmap> {

    private Context mContext;
    private DownloadBean mDownloadBean;
    private String mBitmapAvailable;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mNotifyBuilder;
    private int mNotifyId;
    private PendingIntent mReloadPendingIntent;
    private PendingIntent mJumpPendingIntent;

    public DownloadImageProgressListener(Context context, DownloadBean downloadBean, Intent intent) {
        mContext = context;
        mDownloadBean = downloadBean;
        prepareNotification();
        createReloadPendingIntent(intent);
        createJumpPendingIntent();
        loadThumbnail();
    }

    public void prepareNotification() {
        if (mNotifyBuilder == null) {
            mBitmapAvailable = FileUtils.computeFileSize(mDownloadBean.downloadSize);
            mNotifyBuilder = new NotificationCompat.Builder(mContext);
            mNotifyId = (int) System.currentTimeMillis();
            mNotifyManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

            String title = mDownloadBean.downloadTitle;
            String ticker = mContext.getString(R.string.download_started, title);
            mNotifyBuilder.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_notification_download_large));
            mNotifyBuilder.setTicker(ticker);
            mNotifyBuilder.setContentTitle(title);
            mNotifyBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        }
        mNotifyBuilder.setSmallIcon(R.drawable.ic_notification_download);
        mNotifyBuilder.setContentText("0B / " + mBitmapAvailable);
        mNotifyBuilder.setProgress(100, 0, false);
        mNotifyBuilder.setOngoing(true);
        mNotifyBuilder.setContentIntent(null);
        mNotifyManager.notify(mNotifyId, mNotifyBuilder.build());
    }

    private void loadThumbnail() {
        int size = UIUtils.dp2px(mContext, 64);
        GlideApp.with(mContext)
                .asBitmap()
                .load(MyGlideModule.makeGlideUrl(mDownloadBean.thumbUrl))
                .listener(this)
                .override(size)
                .centerCrop()
                .submit();
    }

    @Override
    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
        loadThumbnail();
        return true;
    }

    @Override
    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
        mNotifyBuilder.setLargeIcon(resource);
        mNotifyManager.notify(mNotifyId, mNotifyBuilder.build());
        return true;
    }

    @Override
    public void onProgress(ProgressInfo progressInfo) {
        int progress = progressInfo.getPercent();
        mNotifyBuilder.setProgress(100, progress, false);
        String content = FileUtils.computeFileSize(progressInfo.getCurrentbytes())
                + " / " + mBitmapAvailable;
        mNotifyBuilder.setContentText(content);
        mNotifyManager.notify(mNotifyId, mNotifyBuilder.build());

        // 由于lolibooru监听不到下载进度，所以将下载完成操作移动到service中执行通知
//        if (progressInfo.isFinish()) {
//            // 下载完成
//            performFinish();
//        }
    }

    @Override
    public void onError(long id, Exception e) {
        mNotifyBuilder.setSmallIcon(R.drawable.ic_notification_download_failed);
        mNotifyBuilder.setContentText(mContext.getString(R.string.download_failed));
        mNotifyBuilder.setOngoing(false);
        mNotifyBuilder.setContentIntent(mReloadPendingIntent);
        mNotifyManager.notify(mNotifyId, mNotifyBuilder.build());
    }

    private void createReloadPendingIntent(Intent intent) {
        Intent reloadIntent = new Intent(mContext, DownloadImageService.class);
        reloadIntent.putExtras(intent);
        mReloadPendingIntent = PendingIntent.getService(mContext, mNotifyId,
                reloadIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void createJumpPendingIntent() {
        Intent jumpIntent = new Intent(mContext, CollectionActivity.class);
        mJumpPendingIntent = PendingIntent.getActivity(mContext, mNotifyId,
                jumpIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void performFinish() {
        String finish = mContext.getString(R.string.download_finished, mBitmapAvailable);
        mNotifyBuilder.setProgress(100, 100, false);
        mNotifyBuilder.setSmallIcon(R.drawable.ic_notification_download_succeed);
        mNotifyBuilder.setContentText(finish);
        mNotifyBuilder.setOngoing(false);
        mNotifyBuilder.setContentIntent(mJumpPendingIntent);
        mNotifyManager.notify(mNotifyId, mNotifyBuilder.build());
    }

}
