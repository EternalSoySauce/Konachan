package com.ess.anime.wallpaper.global;

import com.ess.anime.wallpaper.bean.ThumbBean;

import java.util.ArrayList;

public class ImageDataHolder {
    private static final Object lock = new Object();
    private static ArrayList<ThumbBean> thumbList = new ArrayList<>();
    private static int index;

    public static ArrayList<ThumbBean> getThumbList() {
        synchronized (lock) {
            return thumbList;
        }
    }

    public static void setThumbList(ArrayList<ThumbBean> thumbList, int index) {
        synchronized (lock) {
            clear();
            ImageDataHolder.thumbList.addAll(thumbList);
            ImageDataHolder.index = index;
        }
    }

    public static void clear() {
        synchronized (lock) {
            thumbList.clear();
        }
    }

    public static ThumbBean previousThumb() {
        synchronized (lock) {
            try {
                return thumbList.get(--index);
            } catch (IndexOutOfBoundsException ignore) {
                index++;
                return null;
            }
        }
    }

    public static ThumbBean nextThumb() {
        synchronized (lock) {
            try {
                return thumbList.get(++index);
            } catch (IndexOutOfBoundsException ignore) {
                index--;
                return null;
            }
        }
    }

}
