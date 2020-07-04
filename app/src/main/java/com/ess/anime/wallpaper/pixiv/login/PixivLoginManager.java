package com.ess.anime.wallpaper.pixiv.login;

import com.android.volley.Request;
import com.ess.anime.wallpaper.http.OkHttp;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.LinkedHashMap;
import java.util.Map;

public class PixivLoginManager {

    private static class PixivLoginHolder {
        private final static PixivLoginManager instance = new PixivLoginManager();
    }

    public static PixivLoginManager getInstance() {
        return PixivLoginHolder.instance;
    }

    private PixivLoginManager() {
    }

    /*******************************************************************/

    private final Object mLock = new Object();
    private boolean mIsLoggingIn;

    public void login(String account, String password, IPixivLoginCallback callback) {
        synchronized (mLock) {
            if (!mIsLoggingIn) {
                mIsLoggingIn = true;
                getPostKey(account, password, callback);
            }
        }
    }

    /**
     * 登录帐号密码时需要附带PostKey
     */
    private synchronized void getPostKey(String account, String password, IPixivLoginCallback callback) {
        String url = "https://accounts.pixiv.net/login?lang=zh&source=pc&view_type=page&ref=wwwtop_accounts_index";
        OkHttp.connect(url, this, new OkHttp.OkHttpCallback() {
            @Override
            public void onFailure() {
                if (mIsLoggingIn && callback != null) {
                    callback.onConnectPixivFailed();
                }
            }

            @Override
            public void onSuccessful(String body) {
                if (!mIsLoggingIn) {
                    return;
                }

                try {
                    Document document = Jsoup.parse(body);
                    String config = document.getElementById("init-config").val();
                    JsonObject jsonObject = new JsonParser().parse(config).getAsJsonObject();
                    String postKey = jsonObject.get("pixivAccount.postKey").getAsString();
                    login(account, password, postKey, callback);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (callback != null) {
                        callback.onLoginError();
                    }
                }
            }
        }, Request.Priority.IMMEDIATE);
    }

    /**
     * 登录保存Cookie
     */
    private synchronized void login(String account, String password, String postKey, IPixivLoginCallback callback) {
        String originUrl = "https://accounts.pixiv.net";
        String loginUrl = "https://accounts.pixiv.net/api/login?lang=zh";
        String refererUrl = "https://accounts.pixiv.net/login?return_to=https%3A%2F%2Fwww.pixiv.net%2F&lang=zh&source=pc&view_type=page";

        Map<String, String> headerMap = new LinkedHashMap<>();
        headerMap.put("Origin", originUrl);
        headerMap.put("Referer", refererUrl);
        headerMap.put("User-Agent", OkHttp.USER_AGENT);

        Map<String, String> bodyMap = new LinkedHashMap<>();
        bodyMap.put("pixiv_id", account);
        bodyMap.put("password", password);
        bodyMap.put("post_key", postKey);
        bodyMap.put("return_to", "https://www.pixiv.net/");

//        OkHttp.post(loginUrl, this, headerMap, bodyMap, new OkHttp.OkHttpCallback() {
//            @Override
//            public void onFailure() {
//                if (mIsLoggingIn && callback != null) {
//                    callback.onConnectPixivFailed();
//                }
//            }
//
//            @Override
//            public void onSuccessful(String body) {
//                if (mIsLoggingIn && callback != null) {
//                    callback.onLoginSuccess();
//                }
//            }
//        }, Request.Priority.IMMEDIATE);
    }

    public void cancelLogin() {
        synchronized (mLock) {
            mIsLoggingIn = false;
            OkHttp.cancel(this);
        }
    }

    public boolean isLoggingIn() {
        synchronized (mLock) {
            return mIsLoggingIn;
        }
    }

}
