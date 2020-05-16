package com.ess.anime.wallpaper.pixiv.login;

public interface IPixivCheckLoginStatusCallback {
    void onCookieValid();

    void onCookieExpired();

    void onConnectPixivFailed();
}
