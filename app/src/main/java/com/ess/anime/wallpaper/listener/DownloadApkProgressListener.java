package com.ess.anime.wallpaper.listener;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.bean.ApkBean;
import com.ess.anime.wallpaper.service.DownloadApkService;
import com.ess.anime.wallpaper.utils.ComponentUtils;

import java.io.File;

public class DownloadApkProgressListener extends BaseDownloadProgressListener<ApkBean> {

    private ApkBean mApkBean;

    public DownloadApkProgressListener(Context context, ApkBean apkBean, Intent intent) {
        super(context, apkBean, intent);
    }

    @Override
    void setData(ApkBean data) {
        mApkBean = data;
    }

    @Override
    long getTotalFileSize() {
        return mApkBean.apkSize;
    }

    @Override
    String getNotifyTitle() {
        return mContext.getString(R.string.app_name);
    }

    @Override
    boolean autoPerformFinish() {
        return true;
    }

    @Override
    Class<?> getClassToReload() {
        return DownloadApkService.class;
    }

    @Override
    void createOperatePendingIntent() {
        File apkFile = new File(mApkBean.localFilePath);
        if (apkFile.exists()) {
            Intent installIntent = ComponentUtils.installApk(mContext, apkFile, false);
            if (installIntent != null) {
                mOperateIntent = PendingIntent.getActivity(mContext, mNotifyId,
                        installIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            }
        }
    }

}
