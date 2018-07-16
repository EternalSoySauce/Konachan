package com.ess.anime.wallpaper.bean;

public class PoolListBean {

    public String id;

    public String name;

    public String creator;

    public String postCount;

    public String createTime;

    public String updateTime;

    public String linkToShow;

    public String thumbUrl;

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof PoolListBean) {
            PoolListBean poolListBean = (PoolListBean) obj;
            return !(this.id == null || poolListBean.id == null) && this.id.equals(poolListBean.id);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return thumbUrl.hashCode();
    }
}
