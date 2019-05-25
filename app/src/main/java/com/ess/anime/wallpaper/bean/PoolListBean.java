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
        if (obj instanceof PoolListBean) {
            PoolListBean poolListBean = (PoolListBean) obj;
            return !(this.linkToShow == null || poolListBean.linkToShow == null)
                    && this.linkToShow.equals(poolListBean.linkToShow);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return linkToShow.hashCode();
    }
}
