package com.ess.anime.wallpaper.download.image;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class DownloadBean implements Parcelable {

    public int type;

    @Id
    @Unique
    public String downloadUrl;

    public long downloadSize;

    public String downloadTitle;

    public String thumbUrl;

    public String savePath;

    public boolean fileExists;

    public String description;

    public long addedTime;

    public DownloadBean(int type, String downloadUrl, long downloadSize, String downloadTitle
            , String thumbUrl, String savePath, boolean fileExists, String description) {
        this(type, downloadUrl, downloadSize, downloadTitle, thumbUrl, savePath, fileExists, description, System.currentTimeMillis());
    }

    @Generated(hash = 1292789351)
    public DownloadBean(int type, String downloadUrl, long downloadSize, String downloadTitle
            , String thumbUrl, String savePath, boolean fileExists, String description, long addedTime) {
        this.type = type;
        this.downloadUrl = downloadUrl;
        this.downloadSize = downloadSize;
        this.downloadTitle = downloadTitle;
        this.thumbUrl = thumbUrl;
        this.savePath = savePath;
        this.fileExists = fileExists;
        this.description = description;
        this.addedTime = addedTime;
    }

    @Generated(hash = 2040406903)
    public DownloadBean() {
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
        addedTime = in.readLong();
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
        dest.writeLong(addedTime);
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

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDownloadUrl() {
        return this.downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public long getDownloadSize() {
        return this.downloadSize;
    }

    public void setDownloadSize(long downloadSize) {
        this.downloadSize = downloadSize;
    }

    public String getDownloadTitle() {
        return this.downloadTitle;
    }

    public void setDownloadTitle(String downloadTitle) {
        this.downloadTitle = downloadTitle;
    }

    public String getThumbUrl() {
        return this.thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    public String getSavePath() {
        return this.savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public boolean getFileExists() {
        return this.fileExists;
    }

    public void setFileExists(boolean fileExists) {
        this.fileExists = fileExists;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getAddedTime() {
        return this.addedTime;
    }

    public void setAddedTime(long addedTime) {
        this.addedTime = addedTime;
    }
}
