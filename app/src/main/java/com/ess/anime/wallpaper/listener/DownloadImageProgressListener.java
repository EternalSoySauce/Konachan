package com.ess.anime.wallpaper.listener;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import androidx.annotation.Nullable;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.ess.anime.wallpaper.bean.DownloadBean;
import com.ess.anime.wallpaper.glide.GlideApp;
import com.ess.anime.wallpaper.glide.MyGlideModule;
import com.ess.anime.wallpaper.service.DownloadImageService;
import com.ess.anime.wallpaper.ui.activity.CollectionActivity;
import com.ess.anime.wallpaper.utils.UIUtils;

import me.jessyan.progressmanager.body.ProgressInfo;

public class DownloadImageProgressListener extends BaseDownloadProgressListener<DownloadBean> implements RequestListener<Bitmap> {

    private DownloadBean mDownloadBean;
    private boolean mNeedToReloadThumbnail;

    public DownloadImageProgressListener(Context context, DownloadBean downloadBean, Intent intent) {
        super(context, downloadBean, intent);
        loadThumbnail();
    }

    @Override
    void setData(DownloadBean data) {
        mDownloadBean = data;
    }

    @Override
    long getTotalFileSize() {
        return mDownloadBean.downloadSize;
    }

    @Override
    String getNotifyTitle() {
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
    public void onProgress(ProgressInfo progressInfo) {
        super.onProgress(progressInfo);
        if (mNeedToReloadThumbnail) {
            loadThumbnail();
        }
    }

    @Override
    boolean autoPerformFinish() {
        // 由于lolibooru监听不到下载进度，所以将下载完成操作移动到service中执行通知
        return false;
    }

    @Override
    Class<?> getClassToReload() {
        return DownloadImageService.class;
    }

    @Override
    void createOperatePendingIntent() {
        Intent jumpIntent = new Intent(mContext, CollectionActivity.class);
        mOperateIntent = PendingIntent.getActivity(mContext, mNotifyId,
                jumpIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

}
