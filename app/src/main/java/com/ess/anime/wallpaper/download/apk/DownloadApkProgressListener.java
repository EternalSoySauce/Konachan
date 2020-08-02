package com.ess.anime.wallpaper.download.apk;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.download.BaseDownloadProgressListener;
import com.ess.anime.wallpaper.utils.SystemUtils;

import java.io.File;

public class DownloadApkProgressListener extends BaseDownloadProgressListener<ApkBean> {

    private ApkBean mApkBean;

    public DownloadApkProgressListener(Context context, ApkBean apkBean, Intent intent) {
        super(context, apkBean, intent);
    }

    @Override
    protected void setData(ApkBean data) {
        mApkBean = data;
    }

    @Override
    protected PendingIntent prepareContentIntent() {
        return null;
    }

    @Override
    protected long getTotalFileSize() {
        return mApkBean.apkSize;
    }

    @Override
    protected String getNotifyTitle() {
        return mContext.getString(R.string.app_name);
    }

    @Override
    protected Class<?> getClassToReload() {
        return DownloadApkService.class;
    }

    @Override
    protected void createOperatePendingIntent() {
        File apkFile = new File(mApkBean.localFilePath);
        if (apkFile.exists()) {
            Intent installIntent = SystemUtils.installApk(mContext, apkFile, false);
            if (installIntent != null) {
                mOperateIntent = PendingIntent.getActivity(mContext, mNotifyId,
                        installIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            }
        }
    }

}
