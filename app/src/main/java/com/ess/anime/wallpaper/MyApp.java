package com.ess.anime.wallpaper;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.preference.PreferenceManager;

import com.ess.anime.wallpaper.database.GreenDaoUtils;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.http.OkHttp;
import com.ess.anime.wallpaper.website.WebsiteManager;
import com.tencent.bugly.crashreport.CrashReport;
import com.unity3d.services.core.properties.ClientProperties;

import androidx.multidex.MultiDex;

public class MyApp extends Application {

    private final static String BUGLY_APP_ID = "82daf92318";

    private static MyApp sApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
        CrashReport.initCrashReport(this, BUGLY_APP_ID, false);
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().build());
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();
        GreenDaoUtils.initGreenDao(this);
        OkHttp.initHttpConfig(this);
        ClientProperties.setApplicationContext(this);
        WebsiteManager.getInstance().updateWebsiteConfig();
        initData();
    }

    private void initData() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Constants.sAllowPlaySound = preferences.getBoolean(Constants.ALLOW_PLAY_SOUND, true);
    }

    public static MyApp getInstance() {
        return sApplication;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}
