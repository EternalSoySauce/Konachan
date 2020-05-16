package com.ess.anime.wallpaper.pixiv.login;

public interface IPixivLoginCallback {
    void onLoginSuccess();

    void onLoginError();

    void onConnectPixivFailed();
}
