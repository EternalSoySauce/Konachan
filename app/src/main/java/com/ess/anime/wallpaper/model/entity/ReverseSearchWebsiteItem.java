package com.ess.anime.wallpaper.model.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class ReverseSearchWebsiteItem implements Parcelable {

    public int iconRes;

    public int websiteNameRes;

    public int websiteDescRes;

    public int websiteHelpRes;

    public String websiteUrl;

    public ReverseSearchWebsiteItem(int iconRes, int websiteNameRes, int websiteDescRes, int websiteHelpRes, String websiteUrl) {
        this.iconRes = iconRes;
        this.websiteNameRes = websiteNameRes;
        this.websiteDescRes = websiteDescRes;
        this.websiteHelpRes = websiteHelpRes;
        this.websiteUrl = websiteUrl;
    }

    protected ReverseSearchWebsiteItem(Parcel in) {
        iconRes = in.readInt();
        websiteNameRes = in.readInt();
        websiteDescRes = in.readInt();
        websiteHelpRes = in.readInt();
        websiteUrl = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(iconRes);
        dest.writeInt(websiteNameRes);
        dest.writeInt(websiteDescRes);
        dest.writeInt(websiteHelpRes);
        dest.writeString(websiteUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ReverseSearchWebsiteItem> CREATOR = new Creator<ReverseSearchWebsiteItem>() {
        @Override
        public ReverseSearchWebsiteItem createFromParcel(Parcel in) {
            return new ReverseSearchWebsiteItem(in);
        }

        @Override
        public ReverseSearchWebsiteItem[] newArray(int size) {
            return new ReverseSearchWebsiteItem[size];
        }
    };

}
