package com.ess.anime.wallpaper.global;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initData();
    }

    private void initData() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Constants.sAllowPlaySound = preferences.getBoolean(Constants.ALLOW_PLAY_SOUND, true);
    }

}
