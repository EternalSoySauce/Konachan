package com.ess.anime.wallpaper.utils;

import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.qmuiteam.qmui.util.QMUIDeviceHelper;
import com.yanzhenjie.permission.AndPermission;

import java.io.File;
import java.io.IOException;

public class WallpaperUtil {

    // todo 壁纸机型兼容性，bitmap oom
    public final static int FLAG_DESKTOP = WallpaperManager.FLAG_SYSTEM;
    public final static int FLAG_LOCKSCREEN = WallpaperManager.FLAG_LOCK;
    public final static int FLAG_BOTH = WallpaperManager.FLAG_SYSTEM | WallpaperManager.FLAG_LOCK;

    /**
     * 设置壁纸
     *
     * @param context 上下文
     * @param file    图片文件
     * @param which   原生系统壁纸模式 FLAG_SYSTEM | FLAG_LOCK
     * @return 是否成功
     */
    public static boolean setWallpaper(Context context, File file, int which) {
        if (context == null || file == null) {
            return false;
        }
        Uri uriPath = AndPermission.getFileUri(context, file);

        Intent intent;
        if (QMUIDeviceHelper.isHuawei()) {
            try {
                ComponentName componentName =
                        new ComponentName("com.android.gallery3d", "com.android.gallery3d.app.Wallpaper");
                intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(uriPath, "image/*");
                intent.putExtra("mimeType", "image/*");
                intent.setComponent(componentName);
                context.startActivity(intent);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return setNativeSystemWallpaper(context, file, which);
            }
        } else if (QMUIDeviceHelper.isMIUI()) {
            try {
                ComponentName componentName = new ComponentName("com.android.thememanager",
                        "com.android.thememanager.activity.WallpaperDetailActivity");
                intent = new Intent("miui.intent.action.START_WALLPAPER_DETAIL");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(uriPath, "image/*");
                intent.putExtra("mimeType", "image/*");
                intent.setComponent(componentName);
                context.startActivity(intent);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return setNativeSystemWallpaper(context, file, which);
            }
        } else {
            try {
                intent = WallpaperManager.getInstance(context).getCropAndSetWallpaperIntent(uriPath);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.getApplicationContext().startActivity(intent);
                return true;
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return setNativeSystemWallpaper(context, file, which);
            }
        }
    }

    private static boolean setNativeSystemWallpaper(Context context, File file, int which) {
        try {
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
            int result = wallpaperManager.setBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()), null, true, which);
            return result != 0;
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return false;
    }

}
