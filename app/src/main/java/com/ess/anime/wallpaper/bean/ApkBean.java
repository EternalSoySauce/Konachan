package com.ess.anime.wallpaper.bean;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.UUID;

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

    private static String sID = null;
    private static final String INSTALLATION = "INSTALLATION";

    public synchronized static String id(Context context) {
        if (sID == null) {
            File installation = new File(context.getFilesDir(), INSTALLATION);
            try {
                if (!installation.exists()) writeInstallationFile(installation);
                sID = readInstallationFile(installation);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return sID;
    }

    private static String readInstallationFile(File installation) throws IOException {
        RandomAccessFile f = new RandomAccessFile(installation, "r");
        byte[] bytes = new byte[(int) f.length()];
        f.readFully(bytes);
        f.close();
        return new String(bytes);
    }

    private static void writeInstallationFile(File installation) throws IOException {
        FileOutputStream out = new FileOutputStream(installation);
        String id = UUID.randomUUID().toString();
        out.write(id.getBytes());
        out.close();
    }
}
