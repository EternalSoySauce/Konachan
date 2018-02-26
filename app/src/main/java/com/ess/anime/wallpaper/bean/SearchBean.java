package com.ess.anime.wallpaper.bean;

import com.ess.anime.wallpaper.R;

import java.util.ArrayList;

public class SearchBean {

    public int colorId;  // 标签颜色resId

    public ArrayList<String> tagList = new ArrayList<>();  // 包含tag和其所有别称

    public SearchBean(String type) {
        // 0~6（暂未发现有2）分别对应五中标签类型，即对应相应颜色
        switch (type) {
            case "0":
            case "2": // 暂未发现有2
            case "6": // 6不知道为啥也是白色
                colorId = R.color.color_general;
                break;
            case "1":
                colorId = R.color.color_artist;
                break;
            case "3":
                colorId = R.color.color_copyright;
                break;
            case "4":
                colorId = R.color.color_character;
                break;
            case "5":
                colorId = R.color.color_circle;
                break;
        }
    }
}
