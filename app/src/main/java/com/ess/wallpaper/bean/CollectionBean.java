package com.ess.wallpaper.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class CollectionBean implements Parcelable {

    public String url;
    public String width;
    public String height;
    public boolean isChecked;

    public CollectionBean(String url, String width, String height) {
        this(url, width, height, false);
    }

    public CollectionBean(String url, String width, String height, boolean isChecked) {
        this.url = url;
        this.width = width;
        this.height = height;
        this.isChecked = isChecked;
    }

    protected CollectionBean(Parcel in) {
        url = in.readString();
        width = in.readString();
        height = in.readString();
        isChecked = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeString(width);
        dest.writeString(height);
        dest.writeByte((byte) (isChecked ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CollectionBean> CREATOR = new Creator<CollectionBean>() {
        @Override
        public CollectionBean createFromParcel(Parcel in) {
            return new CollectionBean(in);
        }

        @Override
        public CollectionBean[] newArray(int size) {
            return new CollectionBean[size];
        }
    };
}
