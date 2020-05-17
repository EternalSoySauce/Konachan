package com.ess.anime.wallpaper.pixiv.login;

import android.os.Handler;
import android.os.Looper;

import com.ess.anime.wallpaper.http.OkHttp;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lzy.okgo.OkGo;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;

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
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService mThreadExecutor = Executors.newSingleThreadExecutor();

    private boolean mIsLoggingIn;
    private String mCookie;

    public void login(String account, String password, IPixivLoginCallback callback) {
        synchronized (mLock) {
            if (!mIsLoggingIn) {
                mIsLoggingIn = true;
                mThreadExecutor.execute(() -> {
                    // 获取PostKey
                    String postKey = getPostKey();
                    if (!mIsLoggingIn) {
                        return;
                    }

                    if (postKey == null) {
                        synchronized (mLock) {
                            if (callback != null) {
                                mHandler.post(callback::onConnectPixivFailed);
                            }
                            mIsLoggingIn = false;
                        }
                    } else if (postKey.isEmpty()) {
                        synchronized (mLock) {
                            if (callback != null) {
                                mHandler.post(callback::onLoginError);
                            }
                            mIsLoggingIn = false;
                        }
                    } else {
                        // 登录获取Cookie
                        String cookie = login(account, password, postKey);
                        if (!mIsLoggingIn) {
                            return;
                        }

                        if (cookie == null) {
                            synchronized (mLock) {
                                if (callback != null) {
                                    mHandler.post(callback::onConnectPixivFailed);
                                }
                                mIsLoggingIn = false;
                            }
                        } else if (cookie.isEmpty()) {
                            synchronized (mLock) {
                                if (callback != null) {
                                    mHandler.post(callback::onLoginError);
                                }
                                mIsLoggingIn = false;
                            }
                        } else {
                            synchronized (mLock) {
                                mCookie = cookie;
                                if (callback != null) {
                                    mHandler.post(callback::onLoginSuccess);
                                }
                                mIsLoggingIn = false;
                            }
                        }
                    }
                });
            }
        }
    }

    /**
     * 登录帐号密码时需要附带PostKey
     *
     * @return null为网络访问异常，空为解析失败，其他为获取成功
     */
    private synchronized String getPostKey() {
        String url = "https://accounts.pixiv.net/login?lang=zh&source=pc&view_type=page&ref=wwwtop_accounts_index";
        try (Response response = OkHttp.execute(url, this)) {
            if (response.isSuccessful()) {
                try {
                    String html = response.body().string();
                    Document document = Jsoup.parse(html);
                    String config = document.getElementById("init-config").val();
                    JsonObject jsonObject = new JsonParser().parse(config).getAsJsonObject();
                    return jsonObject.get("pixivAccount.postKey").getAsString();
                } catch (Exception e2) {
                    e2.printStackTrace();
                    return "";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 登录获取Cookie
     *
     * @return null为网络访问异常，空为解析失败，其他为获取成功
     */
    private synchronized String login(String account, String password, String postKey) {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("pixiv_id", account)
                .addFormDataPart("password", password)
                .addFormDataPart("post_key", postKey)
                .addFormDataPart("return_to", "https://www.pixiv.net/")
                .build();

        String originUrl = "https://accounts.pixiv.net";
        String loginUrl = "https://accounts.pixiv.net/api/login?lang=zh";
        String refererUrl = "https://accounts.pixiv.net/login?return_to=https%3A%2F%2Fwww.pixiv.net%2F&lang=zh&source=pc&view_type=page";
        try (Response response = OkGo.<String>post(loginUrl)
                .tag(this)
                .headers("accept", "application/json")
                .headers("content-type", "aapplication/x-www-form-urlencoded")
                .headers("Origin", originUrl)
                .headers("Referer", refererUrl)
                .headers("User-Agent", OkHttp.USER_AGENT)
                .upRequestBody(requestBody)
                .execute()) {
            if (response.isSuccessful()) {
                try {
                    return response.header("set-cookie", "");
                } catch (Exception e2) {
                    e2.printStackTrace();
                    return "";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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


    public void checkLoginStatus(IPixivCheckLoginStatusCallback callback) {
        synchronized (mLock) {
            String url = "https://www.pixiv.net/";
        }
    }

    public String getCookie() {
        synchronized (mLock) {
            return mCookie;
        }
    }
}
