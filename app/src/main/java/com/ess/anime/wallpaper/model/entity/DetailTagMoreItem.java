package com.ess.anime.wallpaper.model.entity;

public class DetailTagMoreItem {

    private String title;

    private int iconRes;

    private Runnable clickCallback;

    public DetailTagMoreItem(String title, int iconRes, Runnable clickCallback) {
        this.title = title;
        this.iconRes = iconRes;
        this.clickCallback = clickCallback;
    }

    public String getTitle() {
        return title;
    }

    public int getIconRes() {
        return iconRes;
    }

    public Runnable getClickCallback() {
        return clickCallback;
    }

}
