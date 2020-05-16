package com.ess.anime.wallpaper.pixiv.gif;

public interface IPixivDlListener {
    void onDataAdded(PixivGifBean pixivGifBean);

    void onDataRemoved(PixivGifBean pixivGifBean);

    void onDataChanged(PixivGifBean pixivGifBean);
}
