package com.ess.anime.wallpaper.bean;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;

public class ApkBean implements Parcelable {

    public int versionCode;

    public String versionName;

    public String apkName;

    public String apkUrl;

    public long apkSize;

    public String updatedContentEn;

    public String updatedContentZh;

    public String localFilePath;

    public static ApkBean getApkDetailFromJson(Context context, String json) {
        ApkBean apkBean = new Gson().fromJson(json, ApkBean.class);
        apkBean.localFilePath = context.getExternalFilesDir(null) + "/" + apkBean.apkName;
        return apkBean;
    }

    protected ApkBean(Parcel in) {
        versionCode = in.readInt();
        versionName = in.readString();
        apkName = in.readString();
        apkUrl = in.readString();
        apkSize = in.readLong();
        updatedContentEn = in.readString();
        updatedContentZh = in.readString();
        localFilePath = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(versionCode);
        dest.writeString(versionName);
        dest.writeString(apkName);
        dest.writeString(apkUrl);
        dest.writeLong(apkSize);
        dest.writeString(updatedContentEn);
        dest.writeString(updatedContentZh);
        dest.writeString(localFilePath);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ApkBean> CREATOR = new Creator<ApkBean>() {
        @Override
        public ApkBean createFromParcel(Parcel in) {
            return new ApkBean(in);
        }

        @Override
        public ApkBean[] newArray(int size) {
            return new ApkBean[size];
        }
    };

}
