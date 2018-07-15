package com.lss.anime.wallpaper.http;

import android.content.Context;

import com.danikula.videocache.HttpProxyCacheServer;
import com.danikula.videocache.file.FileNameGenerator;
import com.lss.anime.wallpaper.utils.FileUtils;

public class VideoCache {

    private static class VideoCacheHolder {
        private static final VideoCache instance = new VideoCache();
    }

    private HttpProxyCacheServer mProxy;  // 用于网络视频缓存

    private VideoCache() {
    }

    public static VideoCache getInstance(Context context) {
        return VideoCacheHolder.instance.createHttpProxyCacheServer(context);
    }

    private VideoCache createHttpProxyCacheServer(Context context) {
        if (mProxy == null) {
            mProxy = new HttpProxyCacheServer.Builder(context.getApplicationContext())
                    .maxCacheSize(1024 * 1024 * 1024)
                    .fileNameGenerator(new MyFileNameGenerator())
                    .build();
        }
        return this;
    }

    public String getCacheUrl(String url) {
        return mProxy.getProxyUrl(url);
    }

    static class MyFileNameGenerator implements FileNameGenerator {

        @Override
        public String generate(String url) {
            String extension = FileUtils.getFileExtensionWithDot(url);
            url = url.substring(0, url.lastIndexOf(extension) + extension.length())
                    .replaceAll(".com|.net", "");
            return FileUtils.encodeMD5String(url);
        }

    }
}
