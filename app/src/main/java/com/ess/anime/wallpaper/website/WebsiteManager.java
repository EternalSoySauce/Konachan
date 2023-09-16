package com.ess.anime.wallpaper.website;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.ess.anime.wallpaper.MyApp;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.http.OkHttp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
