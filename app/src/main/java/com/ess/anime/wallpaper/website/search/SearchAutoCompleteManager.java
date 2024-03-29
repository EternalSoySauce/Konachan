package com.ess.anime.wallpaper.website.search;

import com.android.volley.Request;
import com.ess.anime.wallpaper.http.HandlerFuture;
import com.ess.anime.wallpaper.http.OkHttp;
import com.ess.anime.wallpaper.website.WebsiteConfig;
import com.ess.anime.wallpaper.website.WebsiteManager;

import java.util.List;
import java.util.Map;

import androidx.arch.core.util.Function;

public class SearchAutoCompleteManager {

    public final static String TAG = SearchAutoCompleteManager.class.getName();

    private static class SearchAutoCompleteHolder {
        private final static SearchAutoCompleteManager instance = new SearchAutoCompleteManager();
    }

    public static SearchAutoCompleteManager getInstance() {
        return SearchAutoCompleteHolder.instance;
    }

    private SearchAutoCompleteManager() {
    }

    /*******************************************************************/

    private HandlerFuture mHandlerFuture;

    public synchronized void startTask(String search, ISearchAutoCompleteCallback callback) {
        WebsiteConfig websiteConfig = WebsiteManager.getInstance().getWebsiteConfig();
        if (websiteConfig.hasTagJson()) {
            startLocalTask(websiteConfig, search, callback);
        } else if (websiteConfig.isSupportSearchAutoCompleteFromNetwork()) {
            startNetworkTask(websiteConfig, search, callback);
        }
    }

    private void startLocalTask(WebsiteConfig websiteConfig, String search, ISearchAutoCompleteCallback callback) {
        mHandlerFuture = HandlerFuture.ofWork(search)
                .applyThen((Function<String, List>) websiteConfig::parseSearchAutoCompleteListFromTagJson)
                .runOn(HandlerFuture.IO.UI)
                .applyThen(callback::onSearchAutoComplete);
    }

    private void startNetworkTask(WebsiteConfig websiteConfig, String search, ISearchAutoCompleteCallback callback) {
        String url = websiteConfig.getSearchAutoCompleteUrl(search);
        Map<String, String> headerMap = WebsiteManager.getInstance().getRequestHeaders();
        OkHttp.connect(url, TAG, headerMap, new OkHttp.OkHttpCallback() {
            @Override
            public void onFailure(int errorCode, String errorMessage) {
                startTask(search, callback);
            }

            @Override
            public void onSuccessful(String body) {
                callback.onSearchAutoComplete(websiteConfig.parseSearchAutoCompleteListFromNetwork(body, search));
            }
        }, Request.Priority.IMMEDIATE);
    }

    public synchronized void stopTask() {
        if (mHandlerFuture != null) {
            mHandlerFuture.exits();
        }
        OkHttp.cancel(TAG);
    }

    public interface ISearchAutoCompleteCallback {
        void onSearchAutoComplete(List<String> promptList);
    }

}
