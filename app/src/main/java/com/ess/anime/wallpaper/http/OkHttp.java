package com.ess.anime.wallpaper.http;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.ess.anime.wallpaper.global.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import me.jessyan.progressmanager.ProgressListener;
import me.jessyan.progressmanager.ProgressManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttp {

    private static class OkHttpHolder {
        private static final OkHttp instance = new OkHttp();
    }

    public static OkHttp getInstance() {
        return OkHttpHolder.instance;
    }

    private final static int TIME_OUT_SECONDS = 15;
    private final static String CANCEL_EXCEPTION = "Canceled";    // 由于cancel()操作抛出的异常需要特殊处理（或排除）
    private final static String SOCKET_CLOSED = "Socket closed";   // 同上
    private final static String RESET_STREAM = "stream was reset: CANCEL";  // 同上

    private OkHttpClient mHttpClient;

    // 需要避免重复访问的url保存在这里
    private ArrayList<String> mUrlInQueueList = new ArrayList<>();

    // 需要避免进度监听器重复添加的url保存在这里
    private HashMap<String, ProgressListener> mUrlInListenerMap = new HashMap<>();

    private OkHttp() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(TIME_OUT_SECONDS, TimeUnit.SECONDS);
        builder.readTimeout(TIME_OUT_SECONDS, TimeUnit.SECONDS);
        builder.writeTimeout(TIME_OUT_SECONDS, TimeUnit.SECONDS);
        // 绑定下载进度监听器
        mHttpClient = ProgressManager.getInstance().with(builder).build();
    }

    // 异步网络请求
    public Call connect(String url, Callback callback) {
        Request request = new Request.Builder().url(url).build();
        Call call = mHttpClient.newCall(request);
        call.enqueue(callback);
        return call;
    }

    // 同步网络请求
    public Response execute(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        Call call = mHttpClient.newCall(request);
        return call.execute();
    }

    // 添加需要避免重复访问的url
    public void addUrlToDownloadQueue(String url) {
        mUrlInQueueList.add(url);
    }

    // url访问成功后即可从队列中移除，以便下次可以再次访问
    public void removeUrlFromDownloadQueue(String url) {
        mUrlInQueueList.remove(url);
    }

    // 判断当前url是否正在访问中
    public boolean isUrlInDownloadQueue(String url) {
        return mUrlInQueueList.contains(url);
    }

    // 添加需要避免进度监听器重复添加的url
    public void addUrlToProgressListener(String url, ProgressListener listener) {
        mUrlInListenerMap.put(url, listener);
    }

    // 判断当前url是否已经添加到进度监听器中
    public boolean isUrlInProgressListener(String url) {
        return mUrlInListenerMap.containsKey(url);
    }

    // 获取url所对应的进度监听器
    public ProgressListener getProgressListener(String url) {
        return mUrlInListenerMap.get(url);
    }

    public void cancelAll() {
        mHttpClient.dispatcher().cancelAll();
    }

    /**
     * 判断是网络异常还是手动cancel()产生的异常
     *
     * @param e 异常Exception
     * @return 是否为网络问题
     */
    public static boolean isNetworkProblem(Exception e) {
        return e == null || (!e.getMessage().equals(CANCEL_EXCEPTION)
                && !e.getMessage().equals(SOCKET_CLOSED)
                && !e.getMessage().equals(RESET_STREAM));
    }

    // 通过tags搜索图片
    public static String getPostUrl(Context context, int page, ArrayList<String> tagList) {
        if (tagList == null) {
            return null;
        }

        StringBuilder tags = new StringBuilder();
        for (String tag : tagList) {
            tags.append(tag).append("+");
        }
        return getBaseUrl(context) + "post?page=" + page + "&tags=" + tags;
    }

    // 搜索图集
    public static String getPoolUrl(Context context, int page, String name) {
        name = name == null ? "" : name;
        return getBaseUrl(context) + "pool?page=" + page + "&query=" + name;
    }

    public static String getBaseUrl(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(Constants.BASE_URL, Constants.BASE_URL_KONACHAN_S);
    }
}
