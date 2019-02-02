package com.ess.anime.wallpaper.model.holder;

import com.ess.anime.wallpaper.bean.CollectionBean;
import com.ess.anime.wallpaper.bean.ThumbBean;

import java.util.ArrayList;
import java.util.List;

public class ImageDataHolder {

    private static final Object lock = new Object();

    /*********************************** For Post ***********************************/
    private static List<ThumbBean> thumbList = new ArrayList<>();
    private static int index;

    public static List<ThumbBean> getThumbList() {
        synchronized (lock) {
            return thumbList;
        }
    }

    public static void setThumbList(List<ThumbBean> thumbList, int index) {
        synchronized (lock) {
            clearThumbList();
            ImageDataHolder.thumbList.addAll(thumbList);
            ImageDataHolder.index = index;
        }
    }

    public static void clearThumbList() {
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

    /*********************************** For Collection ***********************************/
    private static List<CollectionBean> collectionList = new ArrayList<>();
    private static int currentItem;

    public static List<CollectionBean> getCollectionList() {
        synchronized (lock) {
            return collectionList;
        }
    }

    public static int getCollectionCurrentItem() {
        synchronized (lock) {
            return currentItem;
        }
    }

    public static void setCollectionList(List<CollectionBean> collectionList) {
        synchronized (lock) {
            clearCollectionList();
            ImageDataHolder.collectionList.addAll(collectionList);
        }
    }

    public static void setCollectionCurrentItem(int currentItem) {
        synchronized (lock) {
            ImageDataHolder.currentItem = currentItem;
        }
    }

    public static void clearCollectionList() {
        synchronized (lock) {
            collectionList.clear();
        }
    }
}
