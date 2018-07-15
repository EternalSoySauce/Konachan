package com.lss.anime.wallpaper.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class PoolPostBean implements Parcelable {

    public String id;  //（未知用处）

    @SerializedName(value = "poolId", alternate = "pool_id")
    public String poolId;  //图集id

    @SerializedName(value = "postId", alternate = "post_id")
    public String postId;  //图片id

    public boolean active;  //（未知用处，猜测为是否公开）

    public String sequence;  //图片在图集中的位置序号（值为String格式，有可能为NaN）

    @SerializedName(value = "nextPostId", alternate = "next_post_id")
    public String nextPostId;  //下张图片id

    @SerializedName(value = "prevPostId", alternate = "prev_post_id")
    public String prevPostId;  //上张图片id

    protected PoolPostBean(Parcel in) {
        id = in.readString();
        poolId = in.readString();
        postId = in.readString();
        active = in.readByte() != 0;
        sequence = in.readString();
        nextPostId = in.readString();
        prevPostId = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(poolId);
        dest.writeString(postId);
        dest.writeByte((byte) (active ? 1 : 0));
        dest.writeString(sequence);
        dest.writeString(nextPostId);
        dest.writeString(prevPostId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PoolPostBean> CREATOR = new Creator<PoolPostBean>() {
        @Override
        public PoolPostBean createFromParcel(Parcel in) {
            return new PoolPostBean(in);
        }

        @Override
        public PoolPostBean[] newArray(int size) {
            return new PoolPostBean[size];
        }
    };
}
