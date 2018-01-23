package com.ess.wallpaper.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class ImageBean implements Parcelable {

    public PostBean[] posts;  //图片信息

    public PoolBean[] pools;  //所属图集信息

    @SerializedName(value = "poolPosts", alternate = "pool_posts")
    public PoolPostBean[] poolPosts;  //在图集中该图片的信息

    @SerializedName(value = "tagArray", alternate = "tags")
    private JsonObject tagArray;  //此处json格式过于不规范，接收后转换为TagBean

    public transient TagBean tags;  //标签详情（不序列化）

    public VoteBean votes;  //（未知用处，bean暂时未设定）

    private ImageBean() {
    }

    public static ImageBean getImageDetailFromJson(String json) {
        Gson gson = new Gson();
        ImageBean imageBean = gson.fromJson(json, ImageBean.class);
        imageBean.tags = new TagBean(imageBean.tagArray);
        return imageBean;
    }

    protected ImageBean(Parcel in) {
        posts = in.createTypedArray(PostBean.CREATOR);
        pools = in.createTypedArray(PoolBean.CREATOR);
        poolPosts = in.createTypedArray(PoolPostBean.CREATOR);
        tags = in.readParcelable(TagBean.class.getClassLoader());
        votes = in.readParcelable(VoteBean.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedArray(posts, flags);
        dest.writeTypedArray(pools, flags);
        dest.writeTypedArray(poolPosts, flags);
        dest.writeParcelable(tags, flags);
        dest.writeParcelable(votes, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ImageBean> CREATOR = new Creator<ImageBean>() {
        @Override
        public ImageBean createFromParcel(Parcel in) {
            return new ImageBean(in);
        }

        @Override
        public ImageBean[] newArray(int size) {
            return new ImageBean[size];
        }
    };
}
