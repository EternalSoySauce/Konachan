package com.ess.anime.wallpaper.model.manager;

import com.ess.anime.wallpaper.http.HandlerFuture;
import com.ess.anime.wallpaper.http.OkHttp;
import com.ess.anime.wallpaper.utils.FileUtils;
import com.ess.anime.wallpaper.website.WebsiteConfig;

import org.jsoup.Jsoup;

import java.io.File;
import java.util.List;

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

    public void startTask(String search, ISearchAutoCompleteCallback callback) {
        stopTask();
        WebsiteConfig websiteConfig = WebsiteManager.getInstance().getWebsiteConfig();
        if (websiteConfig.hasTagJson()) {
            startLocalTask(search, callback);
        } else {
            startNetworkTask(search, callback);
        }
    }

    private void startLocalTask(String search, ISearchAutoCompleteCallback callback) {
        File file = new File(WebsiteManager.getInstance().getWebsiteConfig().getSearchAutoCompletePath());
        if (file.exists() && file.isFile()) {
            String json = FileUtils.fileToString(file);
            json = json == null ? "" : json;
            HandlerFuture.ofWork(json)
                    .applyThen(body -> {
                        return WebsiteManager.getInstance()
                                .getWebsiteConfig()
                                .getHtmlParser()
                                .getImageDetailJson(Jsoup.parse(body));
                    })
                    .runOn(HandlerFuture.IO.UI)
                    .applyThen(promptList -> {

                    });
        }
    }

    private void startNetworkTask(String search, ISearchAutoCompleteCallback callback) {

    }

    public void stopTask() {
        if (mHandlerFuture != null) {
            mHandlerFuture.exits();
        }
        OkHttp.cancel(TAG);
    }

    public interface ISearchAutoCompleteCallback {
        void onSearchAutoComplete(List<String> promptList);
    }

}
