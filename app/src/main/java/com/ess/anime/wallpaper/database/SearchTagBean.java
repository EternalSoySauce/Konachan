package com.ess.anime.wallpaper.database;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class SearchTagBean {

    @Id
    @Unique
    @Property(nameInDb = "SEARCH_TAG")
    String tag;
    @Property(nameInDb = "SEARCH_MODE")
    int mode;
    @Property(nameInDb = "UPDATE_TIME")
    long updateTime;

    @Generated(hash = 264909405)
    public SearchTagBean(String tag, int mode, long updateTime) {
        this.tag = tag;
        this.mode = mode;
        this.updateTime = updateTime;
    }

    @Generated(hash = 1089009375)
    public SearchTagBean() {
    }

    public String getTag() {
        return this.tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getMode() {
        return this.mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public long getUpdateTime() {
        return this.updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

}
