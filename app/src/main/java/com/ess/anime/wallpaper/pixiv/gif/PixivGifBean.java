package com.ess.anime.wallpaper.pixiv.gif;

import com.ess.anime.wallpaper.MyApp;
import com.ess.anime.wallpaper.global.Constants;

import java.io.File;

import nl.bravobit.ffmpeg.FFtask;

public class PixivGifBean {

    public String id;

    public String thumbUrl;

    public String zipUrl;

    public float fps;

    public FFtask gifTask;

    private String gifSavedPath;

    public PixivGifBean(String id) {
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


    /********************* 进度状态 *********************/
    public enum PixivDlState {
        CONNECT_PIXIV, DOWNLOAD_ZIP, EXTRACT_ZIP, MAKE_GIF, FINISH, CANCEL, NOT_GIF, ARTWORK_NOT_EXIST, NEED_LOGIN, LOGIN_EXPIRED
    }

    public PixivDlState state = PixivDlState.CONNECT_PIXIV;
    public float progress;
    public boolean isError;


    /***************************************************/
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PixivGifBean) {
            PixivGifBean pixivGifBean = (PixivGifBean) obj;
            return !(this.id == null || pixivGifBean.id == null) && this.id.equals(pixivGifBean.id);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }
}
