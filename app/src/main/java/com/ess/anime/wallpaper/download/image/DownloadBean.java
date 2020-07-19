package com.ess.anime.wallpaper.download.image;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class DownloadBean implements Parcelable {

    public int type;

    public String downloadUrl;

    public long downloadSize;

    public String downloadTitle;

    public String thumbUrl;

    public String savePath;

    public boolean fileExists;

    public String description;

    public DownloadBean(int type, String downloadUrl, long downloadSize, String downloadTitle
            , String thumbUrl, String savePath, boolean fileExists, String description) {
        this.type = type;
        this.downloadUrl = downloadUrl;
        this.downloadSize = downloadSize;
        this.downloadTitle = downloadTitle;
        this.thumbUrl = thumbUrl;
        this.savePath = savePath;
        this.fileExists = fileExists;
        this.description = description;
    }

    protected DownloadBean(Parcel in) {
        type = in.readInt();
        downloadUrl = in.readString();
        downloadSize = in.readLong();
        downloadTitle = in.readString();
        thumbUrl = in.readString();
        savePath = in.readString();
        fileExists = in.readByte() != 0;
        description = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeString(downloadUrl);
        dest.writeLong(downloadSize);
        dest.writeString(downloadTitle);
        dest.writeString(thumbUrl);
        dest.writeString(savePath);
        dest.writeByte((byte) (fileExists ? 1 : 0));
        dest.writeString(description);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DownloadBean> CREATOR = new Creator<DownloadBean>() {
        @Override
        public DownloadBean createFromParcel(Parcel in) {
            return new DownloadBean(in);
        }

        @Override
        public DownloadBean[] newArray(int size) {
            return new DownloadBean[size];
        }
    };

    @Override
    public String toString() {
        return TextUtils.isEmpty(description) ? super.toString() : description;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DownloadBean) {
            DownloadBean downloadBean = (DownloadBean) obj;
            return this.type == downloadBean.type && !(this.downloadUrl == null || downloadBean.downloadUrl == null) && this.downloadUrl.equals(downloadBean.downloadUrl);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return (downloadUrl != null ? downloadUrl.hashCode() : 0) + type;
    }
}
