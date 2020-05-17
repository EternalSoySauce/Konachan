package com.ess.anime.wallpaper.http;


import android.app.Application;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.ess.anime.wallpaper.listener.BaseDownloadProgressListener;
import com.lzy.okgo.OkGo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class OkHttp {

    public final static String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_2) AppleWebKit / 537.36(KHTML, like Gecko) Chrome  47.0.2526.106 Safari / 537.36";

    public interface OkHttpCallback {
        void onFailure();

        void onSuccessful(String body);
    }

    // 需要避免重复访问的url保存在这里
    private static List<String> sUrlInQueueList = new ArrayList<>();

    // 需要避免进度监听器重复添加的url保存在这里
    private static HashMap<String, BaseDownloadProgressListener> sUrlInListenerMap = new HashMap<>();

    // 添加需要避免重复访问的url
    public static void addUrlToDownloadQueue(String url) {
        sUrlInQueueList.add(url);
    }

    // url访问成功后即可从队列中移除，以便下次可以再次访问
    public static void removeUrlFromDownloadQueue(String url) {
        sUrlInQueueList.remove(url);
    }

    // 判断当前url是否正在访问中
    public static boolean isUrlInDownloadQueue(String url) {
        return sUrlInQueueList.contains(url);
    }

    // 添加需要避免进度监听器重复添加的url
    public static void addUrlToProgressListener(String url, BaseDownloadProgressListener listener) {
        sUrlInListenerMap.put(url, listener);
    }

    // 判断当前url是否已经添加到进度监听器中
    public static boolean isUrlInProgressListener(String url) {
        return sUrlInListenerMap.containsKey(url);
    }

    // 获取url所对应的进度监听器
    public static BaseDownloadProgressListener getProgressListener(String url) {
        return sUrlInListenerMap.get(url);
    }

    private static RequestQueue sRequestQueue;

    // 初始化全局配置
    public static void initHttpConfig(Application application) {
        // 同步请求和下载文件用OkGo
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.readTimeout(15, TimeUnit.SECONDS);
        builder.writeTimeout(15, TimeUnit.SECONDS);
        builder.connectTimeout(15, TimeUnit.SECONDS);
        OkGo.getInstance().init(application)
                .setOkHttpClient(builder.build())
                .setRetryCount(0);

        // 异步请求用Volley
        sRequestQueue = Volley.newRequestQueue(application);
    }

    // 异步网络请求
    public static void connect(String url, Object tag, OkHttpCallback callback) {
        connect(url, tag, callback, Request.Priority.NORMAL);
    }

    // 异步网络请求带优先级
    public static void connect(String url, Object tag, OkHttpCallback callback, Request.Priority priority) {
        connect(url, tag, null, callback, priority);
    }

    // 异步网络请求带Header和优先级
    public static void connect(String url, Object tag, Map<String, String> headerMap, OkHttpCallback callback, Request.Priority priority) {
        PriorityStringRequest request = new PriorityStringRequest(
                convertSchemeToHttps(url),
                callback::onSuccessful,
                error -> {
                    if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                        // 404按成功处理，UI显示无搜索结果而不是访问失败
                        callback.onSuccessful(new String(error.networkResponse.data));
                    } else {
                        callback.onFailure();
                    }
                });
        request.setTag(tag);
        request.setPriority(priority);
        if (headerMap != null) {
            request.addHeaders(headerMap);
        }
        sRequestQueue.add(request);
    }

    // Get同步网络请求
    // 这里用OkGo进行同步请求，用Volley的话cancel同步请求无效
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
        sRequestQueue.cancelAll(tag);
        OkGo.getInstance().cancelTag(tag);
    }

}
