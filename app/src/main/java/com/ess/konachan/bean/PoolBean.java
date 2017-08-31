package com.ess.konachan.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class PoolBean implements Parcelable {

    public String id;  //图集id

    public String name;  //图集名称

    @SerializedName(value = "createdTime", alternate = "created_at")
    public String createdTime;  //创建时间（格式：2016-12-05T12:01:05.115Z）

    @SerializedName(value = "updatedTime", alternate = "updated_at")
    public String updatedTime;  //最后更新时间（格式：2016-12-05T12:03:05.979Z）

    @SerializedName(value = "userID", alternate = "user_id")
    public String userID;  //用户id

    @SerializedName(value = "isPublic", alternate = "is_public")
    public boolean isPublic;  //是否公开

    @SerializedName(value = "postCount", alternate = "post_count")
    public int postCount;  //图集中的图片数量

    public String description;  //图集简介

    protected PoolBean(Parcel in) {
        id = in.readString();
        name = in.readString();
        createdTime = in.readString();
        updatedTime = in.readString();
        userID = in.readString();
        isPublic = in.readByte() != 0;
        postCount = in.readInt();
        description = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(createdTime);
        dest.writeString(updatedTime);
        dest.writeString(userID);
        dest.writeByte((byte) (isPublic ? 1 : 0));
        dest.writeInt(postCount);
        dest.writeString(description);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PoolBean> CREATOR = new Creator<PoolBean>() {
        @Override
        public PoolBean createFromParcel(Parcel in) {
            return new PoolBean(in);
        }

        @Override
        public PoolBean[] newArray(int size) {
            return new PoolBean[size];
        }
    };
}
