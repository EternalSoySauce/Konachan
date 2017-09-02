package com.ess.konachan.http;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.ess.konachan.R;
import com.ess.konachan.bean.ImageBean;
import com.ess.konachan.bean.PostBean;
import com.ess.konachan.service.DownloadService;
import com.ess.konachan.utils.FileUtils;
import com.ess.konachan.utils.UIUtils;

import me.jessyan.progressmanager.ProgressListener;
import me.jessyan.progressmanager.body.ProgressInfo;

public class MyProgressListener implements ProgressListener {

    private Context mContext;
    private ImageBean mImageBean;

    private String mBitmapAvailable;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mNotifyBuilder;
    private int mNotifyId;
    private PendingIntent mPendingIntent;

    public MyProgressListener(Context context, ImageBean imageBean, Intent intent) {
        mContext = context;
        mImageBean = imageBean;
        prepareNotification();
        createReloadPendingIntent(intent);
    }

    public void prepareNotification() {
        if (mNotifyBuilder == null) {
            mBitmapAvailable = getBitmapAvailable(mImageBean);
            mNotifyBuilder = new NotificationCompat.Builder(mContext);
            mNotifyId = (int) System.currentTimeMillis();
            mNotifyManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

            String title = mContext.getString(R.string.image_id_symbol) + mImageBean.posts[0].id;
            String ticker = title + mContext.getString(R.string.download_started);
            mNotifyBuilder.setSmallIcon(R.mipmap.ic_launcher);
            mNotifyBuilder.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher));
            mNotifyBuilder.setTicker(ticker);
            mNotifyBuilder.setContentTitle(title);
            mNotifyBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        }
        mNotifyBuilder.setContentText("0B / " + mBitmapAvailable);
        mNotifyBuilder.setProgress(100, 0, false);
        mNotifyBuilder.setOngoing(true);
        mNotifyBuilder.setContentIntent(null);
        mNotifyManager.notify(mNotifyId, mNotifyBuilder.build());
    }

    private String getBitmapAvailable(ImageBean imageBean) {
        String available;
        PostBean postBean = imageBean.posts[0];
        if (postBean.jpegFileSize != 0) {
            available = FileUtils.computeFileSize(postBean.jpegFileSize);
        } else {
            available = FileUtils.computeFileSize(postBean.fileSize);
        }
        return available;
    }

    public void setNotifyThumb(final String thumbUrl) {
        int size = UIUtils.dp2px(mContext, 64);
        RequestOptions option = new RequestOptions().override(size).centerCrop();
        Glide.with(mContext).asBitmap().load(thumbUrl).listener(new RequestListener<Bitmap>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                Glide.with(mContext).asBitmap().load(thumbUrl).listener(this);
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                mNotifyBuilder.setLargeIcon(resource);
                mNotifyManager.notify(mNotifyId, mNotifyBuilder.build());
                return false;
            }
        }).apply(option).submit();
    }

    @Override
    public void onProgress(ProgressInfo progressInfo) {
        int progress = progressInfo.getPercent();
        mNotifyBuilder.setProgress(100, progress, false);
        String content = FileUtils.computeFileSize(progressInfo.getCurrentbytes())
                + " / " + mBitmapAvailable;
        mNotifyBuilder.setContentText(content);
        mNotifyManager.notify(mNotifyId, mNotifyBuilder.build());

        if (progressInfo.isFinish()) {
            // 下载完成
            String finish = mBitmapAvailable + " / " + mContext.getString(R.string.download_finished);
            mNotifyBuilder.setContentText(finish);
            mNotifyBuilder.setOngoing(false);
            mNotifyManager.notify(mNotifyId, mNotifyBuilder.build());
        }
    }

    @Override
    public void onError(long id, Exception e) {
        mNotifyBuilder.setContentText(mContext.getString(R.string.download_failed));
        mNotifyBuilder.setOngoing(false);
        mNotifyBuilder.setContentIntent(mPendingIntent);
        mNotifyManager.notify(mNotifyId, mNotifyBuilder.build());
    }

    private void createReloadPendingIntent(Intent intent) {
        Intent reloadIntent = new Intent(mContext, DownloadService.class);
        reloadIntent.putExtras(intent);
        mPendingIntent = PendingIntent.getService(mContext, mNotifyId,
                reloadIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
