package com.ess.anime.wallpaper.http;


import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.listener.BaseDownloadProgressListener;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

public class OkHttp {

    public interface OkHttpCallback {
        void onFailure();

        void onSuccessful(String body);
    }

    // 需要避免重复访问的url保存在这里
    private static List<String> mUrlInQueueList = new ArrayList<>();

    // 需要避免进度监听器重复添加的url保存在这里
    private static HashMap<String, BaseDownloadProgressListener> mUrlInListenerMap = new HashMap<>();

    // 添加需要避免重复访问的url
    public static void addUrlToDownloadQueue(String url) {
        mUrlInQueueList.add(url);
    }

    // url访问成功后即可从队列中移除，以便下次可以再次访问
    public static void removeUrlFromDownloadQueue(String url) {
        mUrlInQueueList.remove(url);
    }

    // 判断当前url是否正在访问中
    public static boolean isUrlInDownloadQueue(String url) {
        return mUrlInQueueList.contains(url);
    }

    // 添加需要避免进度监听器重复添加的url
    public static void addUrlToProgressListener(String url, BaseDownloadProgressListener listener) {
        mUrlInListenerMap.put(url, listener);
    }

    // 判断当前url是否已经添加到进度监听器中
    public static boolean isUrlInProgressListener(String url) {
        return mUrlInListenerMap.containsKey(url);
    }

    // 获取url所对应的进度监听器
    public static BaseDownloadProgressListener getProgressListener(String url) {
        return mUrlInListenerMap.get(url);
    }

    // 初始化全局配置
    public static void initHttpConfig(Application application) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.readTimeout(15, TimeUnit.SECONDS);
        builder.writeTimeout(15, TimeUnit.SECONDS);
        builder.connectTimeout(15, TimeUnit.SECONDS);
        builder.dispatcher(new Dispatcher(new PriorityExecutorService()));
        OkGo.getInstance().init(application)
                .setOkHttpClient(builder.build())
                .setRetryCount(0);
    }

    // 异步网络请求
    public static void connect(String url, Object tag, OkHttpCallback callback) {
        OkGo.<String>get(convertSchemeToHttps(url))
                .tag(tag)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if (response.isSuccessful() && response.code() >= 200 && response.code() < 300) {
                            callback.onSuccessful(response.body());
                        } else {
                            callback.onFailure();
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        callback.onFailure();
                    }
                });
    }

    // 同步网络请求
    public static okhttp3.Response execute(String url, Object tag) throws Exception {
        return OkGo.<String>get(convertSchemeToHttps(url))
                .tag(tag)
                .execute();
    }

    // 检测将http协议转换为https协议
    public static String convertSchemeToHttps(String url) {
        return url.replace("http://", "https://");
    }

    // 取消请求
    public static void cancel(Object tag) {
        OkGo.getInstance().cancelTag(tag);
    }

    // 通过tags搜索图片
    public static String getPostUrl(Context context, int page, List<String> tagList) {
        if (tagList == null) {
            tagList = new ArrayList<>();
        }

        StringBuilder tags = new StringBuilder();
        for (String tag : tagList) {
            tags.append(tag).append("+");
        }

        String baseUrl = getBaseUrl(context);
        switch (baseUrl) {
            case Constants.BASE_URL_GELBOORU:
                return baseUrl + "index.php?page=dapi&s=post&q=index&pid=" + (page - 1) + "&tags=" + tags + "&limit=42";
            default:
                return baseUrl + "post?page=" + page + "&tags=" + tags;
        }
    }

    // 搜索图集
    public static String getPoolUrl(Context context, int page, String name) {
        name = name == null ? "" : name;
        String baseUrl = getBaseUrl(context);
        switch (baseUrl) {
            case Constants.BASE_URL_DANBOORU:
                return baseUrl + "pools/gallery?commit=Search&limit=20&page=" + page + "&search[name_matches]=" + name + "&search[order]=&utf8=%E2%9C%93";
            case Constants.BASE_URL_SANKAKU:
                return baseUrl + "pool/index?page=" + page + "&query=" + name;
            case Constants.BASE_URL_GELBOORU:   // gelbooru无法搜索
                return baseUrl + "index.php?page=pool&s=list&pid=" + (page - 1) * 25;
            default:
                return baseUrl + "pool?commit=Search&page=" + page + "&query=" + name;
        }
    }

    // 搜索图集中的图片
    public static String getPoolPostUrl(Context context, String linkToShow, int page) {
        String baseUrl = getBaseUrl(context);
        switch (baseUrl) {
            case Constants.BASE_URL_GELBOORU:
                return linkToShow;
            default:
                return linkToShow + "?page=" + page;
        }
    }

    // 当前网站源的网址
    public static String getBaseUrl(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(Constants.BASE_URL, Constants.BASE_URL_KONACHAN_S);
    }
}
