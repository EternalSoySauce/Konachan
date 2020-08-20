package com.ess.anime.wallpaper.download.image.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.ui.activity.DownloadImageManagerActivity;

public class MyNotification {

    private static final String NOTIFY_CHANNEL_ID = "notification";
    private static final String NOTIFY_CHANNEL_NAME = "notification";
    private static final int NOTIFY_ID = 123;

    private Service mService;

    public synchronized void show(Service service) {
        mService = service;
        createNotifyChannel();
        showNotification();
    }

    private void createNotifyChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFY_CHANNEL_ID,
                    NOTIFY_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            channel.setImportance(NotificationManager.IMPORTANCE_NONE);
            NotificationManager notifyManager = (NotificationManager) mService.getSystemService(Context.NOTIFICATION_SERVICE);
            notifyManager.createNotificationChannel(channel);
        }
    }

    private void showNotification() {
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(mService, NOTIFY_CHANNEL_ID);
        } else {
            builder = new Notification.Builder(mService);
        }

        Intent intent = new Intent(mService, DownloadImageManagerActivity.class);
        PendingIntent pd = PendingIntent.getActivity(mService, 1000, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(mService.getString(R.string.foreground_notify_title))
                .setContentText(mService.getString(R.string.foreground_notify_msg))
                .setContentIntent(pd);

        mService.startForeground(NOTIFY_ID, builder.build());
    }

    public void stop() {
        mService.stopForeground(true);
    }

}
