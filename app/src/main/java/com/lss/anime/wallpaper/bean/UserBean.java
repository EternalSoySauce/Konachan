package com.lss.anime.wallpaper.bean;

import android.content.Context;
import android.text.TextUtils;

import com.lss.anime.wallpaper.utils.ComponentUtils;
import com.lss.anime.wallpaper.utils.TimeFormat;

import java.util.UUID;

public class UserBean {

    public String id;

    public String date;

    public UserBean(Context context) {
        id = ComponentUtils.getAndroidId(context);
        if (TextUtils.isEmpty(id)) {
            id = UUID.randomUUID().toString();
        }
        date = TimeFormat.dateFormat(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss");
    }
}
