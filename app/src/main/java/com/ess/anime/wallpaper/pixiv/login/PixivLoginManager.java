package com.ess.anime.wallpaper.pixiv.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.ess.anime.wallpaper.MyApp;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.ui.activity.web.PixivLoginActivity;

import java.util.ArrayList;
import java.util.List;

public class PixivLoginManager {

    private static class PixivLoginHolder {
        private final static PixivLoginManager instance = new PixivLoginManager();
    }

    public static PixivLoginManager getInstance() {
        return PixivLoginHolder.instance;
    }

    private PixivLoginManager() {
    }

    /***************************  登录接口  ***************************/

    public void login(Context context) {
        Intent intent = new Intent(context, PixivLoginActivity.class);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    public boolean isLogin() {
        return !TextUtils.isEmpty(getCookie());
    }

    public String getCookie() {
        return getPreferences().getString(Constants.PIXIV_LOGIN_COOKIE, null);
    }

    public void setCookie(String cookie) {
        getPreferences().edit().putString(Constants.PIXIV_LOGIN_COOKIE, cookie).apply();
        setCookieExpired(false);
    }

    public void setCookieExpired(boolean expired) {
        getPreferences().edit().putBoolean(Constants.PIXIV_LOGIN_COOKIE_EXPIRED, expired).apply();
        notifyLoginStateChanged();
    }

    public boolean isCookieExpired() {
        return getPreferences().getBoolean(Constants.PIXIV_LOGIN_COOKIE_EXPIRED, false);
    }

    private SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());
    }

    /***************************  监听器  ***************************/

    private List<IPixivLoginListener> mLoginListener = new ArrayList<>();

    public synchronized void registerLoginListener(IPixivLoginListener listener) {
        if (!mLoginListener.contains(listener)) {
            mLoginListener.add(listener);
        }
    }

    public synchronized void unregisterLoginListener(IPixivLoginListener listener) {
        mLoginListener.remove(listener);
    }

    private synchronized void notifyLoginStateChanged() {
        for (IPixivLoginListener listener : new ArrayList<>(mLoginListener)) {
            listener.onLoginStateChanged();
        }
    }
}
