package com.ess.anime.wallpaper.pixiv;

import com.ess.anime.wallpaper.MyApp;
import com.ess.anime.wallpaper.global.Constants;

import java.io.File;

import nl.bravobit.ffmpeg.FFtask;

public class PixivBean {

    public String id;

    public String zipUrl;

    public float fps;

    public FFtask gifTask;

    private String gifSavedPath;

    public PixivBean(String id) {
        this.id = id;
    }

    // 获取fps和zipUrl的网址
    public String getJsonUrl() {
        return "https://www.pixiv.net/ajax/illust/" + id + "/ugoira_meta?lang=zh";
    }

    // 下载P站资源时需要传入Referer请求头
    public String getRefererUrl() {
        return "https://www.pixiv.net/artworks/" + id;
    }

    // zip本地缓存路径
    public String getZipCacheDirPath() {
        return MyApp.getInstance().getCacheDir() + File.separator + id;
    }

    // zip本地存储名
    public String getZipFileName() {
        return id + ".zip";
    }

    // 生成Gif的文件路径
    public String getGifSavedPath() {
        if (gifSavedPath == null) {
            gifSavedPath = Constants.IMAGE_DIR + File.separator + "Pixiv_" + id + "_" + System.currentTimeMillis() + ".gif";
        }
        return gifSavedPath;
    }
}
