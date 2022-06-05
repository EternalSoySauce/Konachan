package com.ess.anime.wallpaper.model.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class ReverseSearchWebsiteItem implements Parcelable {

    public String websiteName;

    public String websiteDesc;

    public String websiteHelp;

    public String websiteUrl;

    public ReverseSearchWebsiteItem(String websiteName, String websiteDesc, String websiteHelp, String websiteUrl) {
        this.websiteName = websiteName;
        this.websiteDesc = websiteDesc;
        this.websiteHelp = websiteHelp;
        this.websiteUrl = websiteUrl;
    }

    protected ReverseSearchWebsiteItem(Parcel in) {
        websiteName = in.readString();
        websiteDesc = in.readString();
        websiteHelp = in.readString();
        websiteUrl = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(websiteName);
        dest.writeString(websiteDesc);
        dest.writeString(websiteHelp);
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
