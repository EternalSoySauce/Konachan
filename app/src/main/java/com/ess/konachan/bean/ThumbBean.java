package com.ess.konachan.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class ThumbBean implements Parcelable{

    public String thumbUrl;
    public String realSize;
    public String linkToShow;
    public ImageBean imageBean;

    public ThumbBean(String thumbUrl, String realSize, String linkToShow) {
        this.thumbUrl = thumbUrl;
        this.realSize = realSize;
        this.linkToShow = linkToShow;
    }

    protected ThumbBean(Parcel in) {
        thumbUrl = in.readString();
        realSize = in.readString();
        linkToShow = in.readString();
        imageBean = in.readParcelable(ImageBean.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(thumbUrl);
        dest.writeString(realSize);
        dest.writeString(linkToShow);
        dest.writeParcelable(imageBean, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ThumbBean> CREATOR = new Creator<ThumbBean>() {
        @Override
        public ThumbBean createFromParcel(Parcel in) {
            return new ThumbBean(in);
        }

        @Override
        public ThumbBean[] newArray(int size) {
            return new ThumbBean[size];
        }
    };
}
