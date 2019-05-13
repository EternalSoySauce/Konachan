package com.ess.anime.wallpaper;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.preference.PreferenceManager;

import com.ess.anime.wallpaper.global.Constants;

import androidx.multidex.MultiDex;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().build());
        initData();
    }

    private void initData() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Constants.sAllowPlaySound = preferences.getBoolean(Constants.ALLOW_PLAY_SOUND, true);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}
