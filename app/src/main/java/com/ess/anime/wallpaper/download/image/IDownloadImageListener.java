package com.ess.anime.wallpaper.download.image;

public interface IDownloadImageListener {
    void onDataAdded(DownloadBean downloadBean);

    void onDataRemoved(DownloadBean downloadBean);

    void onDataChanged(DownloadBean downloadBean);
}
