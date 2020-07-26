package com.ess.anime.wallpaper.download.image;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.ess.anime.wallpaper.download.BaseDownloadProgressListener;
import com.ess.anime.wallpaper.glide.GlideApp;
import com.ess.anime.wallpaper.glide.MyGlideModule;
import com.ess.anime.wallpaper.ui.activity.CollectionActivity;
import com.ess.anime.wallpaper.utils.UIUtils;

import androidx.annotation.Nullable;

public class DownloadImageProgressListener extends BaseDownloadProgressListener<DownloadBean> implements RequestListener<Bitmap> {

    private DownloadBean mDownloadBean;
    private boolean mNeedToReloadThumbnail;

    public DownloadImageProgressListener(Context context, DownloadBean downloadBean, Intent intent) {
        super(context, downloadBean, intent);
        loadThumbnail();
    }

    @Override
    protected void setData(DownloadBean data) {
        mDownloadBean = data;
    }

    @Override
    public void prepareNotification() {
        createOperatePendingIntent();
        super.prepareNotification();
    }

    @Override
    protected PendingIntent prepareContentIntent() {
        return mOperateIntent;
    }

    @Override
    protected long getTotalFileSize() {
        return mDownloadBean.downloadSize;
    }

    @Override
    protected String getNotifyTitle() {
        return mDownloadBean.downloadTitle;
    }

    private void loadThumbnail() {
        mNeedToReloadThumbnail = false;
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
        mNeedToReloadThumbnail = true;
        return true;
    }

    @Override
    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
        mNeedToReloadThumbnail = false;
        mNotifyBuilder.setLargeIcon(resource);
        mNotifyManager.notify(mNotifyId, mNotifyBuilder.build());
        return true;
    }

    @Override
    public void onProgress(int progress, long currentSize, long totalSize, long speed) {
        super.onProgress(progress, currentSize, totalSize, speed);
        if (mNeedToReloadThumbnail) {
            loadThumbnail();
        }
    }

    @Override
    protected Class<?> getClassToReload() {
        return DownloadImageService.class;
    }

    @Override
    protected void createOperatePendingIntent() {
        if (mOperateIntent == null) {
            Intent jumpIntent = new Intent(mContext, CollectionActivity.class);
            mOperateIntent = PendingIntent.getActivity(mContext, mNotifyId,
                    jumpIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }

}
