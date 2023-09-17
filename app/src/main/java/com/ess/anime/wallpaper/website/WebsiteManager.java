package com.ess.anime.wallpaper.website;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.ess.anime.wallpaper.MyApp;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.http.OkHttp;
import com.ess.anime.wallpaper.utils.FileUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebsiteManager {

    public final static String TAG = WebsiteManager.class.getName();

    private static class WebsiteHolder {
        private final static WebsiteManager instance = new WebsiteManager();
    }

    public static WebsiteManager getInstance() {
        return WebsiteHolder.instance;
    }

    private WebsiteManager() {
    }

    /********************** Basic **********************/

    private WebsiteConfig mWebsiteConfig;

    public WebsiteConfig getWebsiteConfig() {
        synchronized (WebsiteManager.class) {
            return mWebsiteConfig;
        }
    }

    // 切换网站源
    public void changeWebsite(String baseUrl) {
        synchronized (WebsiteManager.class) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());
            sp.edit().putString(Constants.BASE_URL, baseUrl).apply();
            updateWebsiteConfig();
            OkHttp.cancel(TAG);
            updateCurrentTagJson();
        }
    }

    public void updateWebsiteConfig() {
        synchronized (WebsiteManager.class) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());
            String baseUrl = sp.getString(Constants.BASE_URL, WebsiteConfig.BASE_URL_KONACHAN_S);
            if (!Arrays.asList(WebsiteConfig.BASE_URLS).contains(baseUrl)) {
                baseUrl = WebsiteConfig.BASE_URL_KONACHAN_S;
            }

            WebsiteConfig websiteConfig;
            switch (baseUrl) {
                default:
                case WebsiteConfig.BASE_URL_KONACHAN_S:
                    websiteConfig = new KonachanSConfig();
                    break;
                case WebsiteConfig.BASE_URL_KONACHAN_E:
                    websiteConfig = new KonachanEConfig();
                    break;
                case WebsiteConfig.BASE_URL_YANDE:
                    websiteConfig = new YandeConfig();
                    break;
                case WebsiteConfig.BASE_URL_DANBOORU:
                    websiteConfig = new DanbooruConfig();
                    break;
                case WebsiteConfig.BASE_URL_SAFEBOORU:
                    websiteConfig = new SafebooruConfig();
                    break;
                case WebsiteConfig.BASE_URL_GELBOORU:
                    websiteConfig = new GelbooruConfig();
                    break;
                case WebsiteConfig.BASE_URL_LOLIBOORU:
                    websiteConfig = new LolibooruConfig();
                    break;
                case WebsiteConfig.BASE_URL_SANKAKU:
                    websiteConfig = new SankakuConfig();
                    break;
                case WebsiteConfig.BASE_URL_ZEROCHAN:
                    websiteConfig = new ZerochanConfig();
                    break;
                case WebsiteConfig.BASE_URL_WALLHAVEN:
                    websiteConfig = new WallhavenConfig();
                    break;
            }

            if (mWebsiteConfig != websiteConfig) {
                mWebsiteConfig = websiteConfig;
                for (OnWebsiteChangeListener listener : websiteChangeListenerList) {
                    listener.onWebsiteChanged(baseUrl);
                }
            }
        }
    }

    // 下载更新当前网站源的搜索下拉提示
    public void updateCurrentTagJson() {
        synchronized (WebsiteManager.class) {
            if (mWebsiteConfig.hasTagJson()) {
                String url = mWebsiteConfig.getTagJsonUrl();
                OkHttp.connect(url, TAG, new OkHttp.OkHttpCallback() {
                    @Override
                    public void onFailure(int errorCode, String errorMessage) {
                        updateCurrentTagJson();
                    }

                    @Override
                    public void onSuccessful(String json) {
                        mWebsiteConfig.saveTagJson(url, json);
                    }
                });
            }
        }
    }

    /********************** Request Headers **********************/

    private final static String SERVER_HEADER_FILE_URL = "https://opentext.oss-cn-shenzhen.aliyuncs.com/apk/website_request_headers";
    private final static String LOCAL_HEADER_FILE_NAME = "website_request_headers";

    private String mRequestHeadersJson;

    public void loadNewRequestHeadersJsonFromServer() {
        OkHttp.connect(SERVER_HEADER_FILE_URL, SERVER_HEADER_FILE_URL, new OkHttp.OkHttpCallback() {
            @Override
            public void onFailure(int errorCode, String errorMessage) {
                loadNewRequestHeadersJsonFromServer();
            }

            @Override
            public void onSuccessful(String json) {
                saveRequestHeadersJson(json);
            }
        });
    }

    private void saveRequestHeadersJson(String json) {
        synchronized (WebsiteManager.class) {
            String dir = MyApp.getInstance().getFilesDir().getPath();
            File file = new File(dir, LOCAL_HEADER_FILE_NAME);
            FileUtils.stringToFile(json, file);
            mRequestHeadersJson = json;
        }
    }

    private String getRequestHeadersJson() {
        synchronized (WebsiteManager.class) {
            if (TextUtils.isEmpty(mRequestHeadersJson)) {
                String dir = MyApp.getInstance().getFilesDir().getPath();
                File file = new File(dir, LOCAL_HEADER_FILE_NAME);
                if (file.exists() && file.isFile()) {
                    mRequestHeadersJson = FileUtils.fileToString(file);
                }
            }
            return mRequestHeadersJson == null ? "" : mRequestHeadersJson;
        }
    }

    // 需要填充的Header信息，如AccessToken
    public Map<String, String> getRequestHeaders() {
        Map<String, String> headerMap = new HashMap<>();
        try {
            String json = getRequestHeadersJson();
            if (!TextUtils.isEmpty(json)) {
                String websiteName = mWebsiteConfig.getWebsiteName();
                JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
                JsonObject website = jsonObject.getAsJsonObject(websiteName);
                if (website != null) {
                    for (String key : website.keySet()) {
                        headerMap.put(key, website.get(key).getAsString());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return headerMap;
    }

    /********************** Observer **********************/

    private List<OnWebsiteChangeListener> websiteChangeListenerList = new ArrayList<>();

    public void registerWebsiteChangeListener(OnWebsiteChangeListener listener) {
        if (!websiteChangeListenerList.contains(listener)) {
            websiteChangeListenerList.add(listener);
        }
    }

    public void unregisterWebsiteChangeListener(OnWebsiteChangeListener listener) {
        websiteChangeListenerList.remove(listener);
    }

    public interface OnWebsiteChangeListener {
        void onWebsiteChanged(String baseUrl);
    }
}
