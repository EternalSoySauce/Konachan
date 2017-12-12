package com.ess.kanime.bean;

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
            return !(this.thumbUrl == null || poolListBean.thumbUrl == null) && this.thumbUrl.equals(poolListBean.thumbUrl);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return thumbUrl.hashCode();
    }
}
