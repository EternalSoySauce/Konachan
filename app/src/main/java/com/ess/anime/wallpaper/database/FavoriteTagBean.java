package com.ess.anime.wallpaper.database;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Unique;

@Entity
public class FavoriteTagBean {

    @Id
    @Unique
    @Property(nameInDb = "TAG")
    String tag;
    @Property(nameInDb = "ANNOTATION")
    String annotation;
    @Property(nameInDb = "IS_FAVORITE")
    boolean isFavorite;
    @Property(nameInDb = "FAVORITE_TIME")
    long favoriteTime;

    @Generated(hash = 277234787)
    public FavoriteTagBean(String tag, String annotation, boolean isFavorite,
                           long favoriteTime) {
        this.tag = tag;
        this.annotation = annotation;
        this.isFavorite = isFavorite;
        this.favoriteTime = favoriteTime;
    }

    @Generated(hash = 729334237)
    public FavoriteTagBean() {
    }

    public String getTag() {
        return this.tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getAnnotation() {
        return this.annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public boolean getIsFavorite() {
        return this.isFavorite;
    }

    public void setIsFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    public long getFavoriteTime() {
        return this.favoriteTime;
    }

    public void setFavoriteTime(long favoriteTime) {
        this.favoriteTime = favoriteTime;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FavoriteTagBean) {
            FavoriteTagBean tagBean = (FavoriteTagBean) obj;
            return !(this.tag == null || tagBean.tag == null) && this.tag.equals(tagBean.tag);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return tag.hashCode();
    }

}
