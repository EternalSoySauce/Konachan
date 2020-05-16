package com.ess.anime.wallpaper.pixiv.login;

import android.util.Log;

import com.android.volley.Request;
import com.ess.anime.wallpaper.http.OkHttp;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

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

    private String mCookie;

    public void getPostKey() {
        String url = "https://accounts.pixiv.net/login?lang=zh&source=pc&view_type=page&ref=wwwtop_accounts_index";
        OkHttp.connect(url, this, new OkHttp.OkHttpCallback() {
            @Override
            public void onFailure() {
                Log.i("Rrr", "onFailure");
            }

            @Override
            public void onSuccessful(String html) {
                Log.i("Rrr", "onSuccessful " + html);
                Document document = Jsoup.parse(html);
                String config = document.getElementById("init-config").val();
                JsonObject jsonObject = new JsonParser().parse(config).getAsJsonObject();
                if (jsonObject.has("pixivAccount.postKey")) {
                    String postKey = jsonObject.get("pixivAccount.postKey").getAsString();
                    Log.i("rrr", postKey);
                }
            }
        }, Request.Priority.IMMEDIATE);
    }

    public void login(String account, String password) {
//        OkHttp.connect(url, this, new OkHttp.OkHttpCallback() {
//            @Override
//            public void onFailure() {
//
//            }
//
//            @Override
//            public void onSuccessful(String body) {
//
//            }
//        }, Request.Priority.IMMEDIATE);
    }

    public void checkLoginStatus(IPixivCheckLoginStatusCallback callback) {
        String url = "https://www.pixiv.net/";

    }

    public String getCookie() {
        return mCookie;
    }
}
