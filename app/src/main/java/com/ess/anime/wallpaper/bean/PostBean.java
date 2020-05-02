package com.ess.anime.wallpaper.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

public class PostBean implements Parcelable {

    public String id;  //K站图片id

    public String tags;  //图片标签

    @SerializedName(value = "createdTime", alternate = "created_at")
    public long createdTime;  //上传时间（格式：mills，单位：second）

    @SerializedName(value = "creatorId", alternate = "creator_id")
    public String creatorId;  //上传者id

    public String author;  //上传者用户名

    public String change;  //（未知用处）

    public String source;  //图片源址

    public int score;  //图片评分

    public String md5;  //md5加密码

    @SerializedName(value = "fileSize", alternate = "file_size")
    public long fileSize;  //大图页面显示的尺寸，图片文件大小（作为原图备用值）

    @SerializedName(value = "fileUrl", alternate = "file_url")
    public String fileUrl;  //大图页面显示的尺寸，图片地址（作为原图备用值）

    @SerializedName(value = "isShownInIndex", alternate = "is_shown_in_index")
    public boolean isShownInIndex;  //（未知用处）

    @SerializedName(value = "previewUrl", alternate = "preview_url")
    public String previewUrl;  //thumb尺寸，图片地址

    @SerializedName(value = "previewWidth", alternate = "preview_width")
    public int previewWidth;  //thumb尺寸，图片比例宽度

    @SerializedName(value = "previewHeight", alternate = "preview_height")
    public int previewHeight;  //thumb尺寸，图片比例高度

    @SerializedName(value = "actualPreviewWidth", alternate = "actual_preview_width")
    public int actualPreviewWidth;  //thumb尺寸，图片实际宽度

    @SerializedName(value = "actualPreviewHeight", alternate = "actual_preview_height")
    public int actualPreviewHeight;  //thumb尺寸，图片实际高度

    @SerializedName(value = "sampleUrl", alternate = "sample_url")
    public String sampleUrl;  //sample尺寸，图片地址

    @SerializedName(value = "sampleWidth", alternate = "sample_width")
    public int sampleWidth;  //sample尺寸，图片实际宽度

    @SerializedName(value = "sampleHeight", alternate = "sample_height")
    public int sampleHeight;  //sample尺寸，图片实际高度

    @SerializedName(value = "sampleFileSize", alternate = "sample_file_size")
    public long sampleFileSize;  //sample尺寸，图片文件大小

    @SerializedName(value = "jpegUrl", alternate = "jpeg_url")
    public String jpegUrl;  //real尺寸，图片地址

    @SerializedName(value = "jpegWidth", alternate = "jpeg_width")
    public int jpegWidth;  //real尺寸，图片实际宽度

    @SerializedName(value = "jpegHeight", alternate = "jpeg_height")
    public int jpegHeight;  //real尺寸，图片实际高度

    @SerializedName(value = "jpegFileSize", alternate = "jpeg_file_size")
    public long jpegFileSize;  //real尺寸，图片文件大小（若此处值为0，则使用上面的fileSize）

    public String rating;  //安全等级：s（safe_mode），e（R18），q（questionable）

    @SerializedName(value = "hasChildren", alternate = "has_children")
    public boolean hasChildren;  //是否有相关子图片

    @SerializedName(value = "parentId", alternate = "parent_id")
    public String parentId;  //相关父图片id（内容相同，只有背景或部分装饰不同的那种图组）

    public String status;  //（未知用处）

    public int width;  //图片宽度（使用jpegWidth）

    public int height;  //图片高度（使用jpegHeight）

    @SerializedName(value = "isHeld", alternate = "is_held")
    public boolean isHeld;  //（未知用处）

    @SerializedName(value = "framesPendingString", alternate = "frames_pending_string")
    public String framesPendingString;  //（未知用处）

    @SerializedName(value = "framesPending", alternate = "frames_pending")
    public Object[] framesPending;  //（未知用处）（数组格式，类型不明）

    @SerializedName(value = "framesString", alternate = "frames_string")
    public String framesString;  //（未知用处）

    public Object[] frames;  //（未知用处）（数组格式，类型不明）

    @SerializedName(value = "flagDetail", alternate = "flag_detail")
    public String flagDetail;  //（未知用处）（只有一部分有这个key）

    public PostBean() {
    }

    protected PostBean(Parcel in) {
        id = in.readString();
        tags = in.readString();
        createdTime = in.readLong();
        creatorId = in.readString();
        author = in.readString();
        change = in.readString();
        source = in.readString();
        score = in.readInt();
        md5 = in.readString();
        fileSize = in.readLong();
        fileUrl = in.readString();
        isShownInIndex = in.readByte() != 0;
        previewUrl = in.readString();
        previewWidth = in.readInt();
        previewHeight = in.readInt();
        actualPreviewWidth = in.readInt();
        actualPreviewHeight = in.readInt();
        sampleUrl = in.readString();
        sampleWidth = in.readInt();
        sampleHeight = in.readInt();
        sampleFileSize = in.readLong();
        jpegUrl = in.readString();
        jpegWidth = in.readInt();
        jpegHeight = in.readInt();
        jpegFileSize = in.readLong();
        rating = in.readString();
        hasChildren = in.readByte() != 0;
        parentId = in.readString();
        status = in.readString();
        width = in.readInt();
        height = in.readInt();
        isHeld = in.readByte() != 0;
        framesPendingString = in.readString();
        framesString = in.readString();
        flagDetail = in.readString();
    }

    /**
     * 用newPost中的非null和0的数据替换掉原有数据
     *
     * @param newPost
     */
    public void replaceDataIfNotNull(PostBean newPost) {
        if (newPost == null) {
            return;
        }

        if (!TextUtils.isEmpty(newPost.id)) {
            id = newPost.id;
        }
        if (!TextUtils.isEmpty(newPost.tags)) {
            tags = newPost.tags;
        }
        if (newPost.createdTime != 0) {
            createdTime = newPost.createdTime;
        }
        if (!TextUtils.isEmpty(newPost.creatorId)) {
            creatorId = newPost.creatorId;
        }
        if (!TextUtils.isEmpty(newPost.author)) {
            author = newPost.author;
        }
        if (!TextUtils.isEmpty(newPost.change)) {
            change = newPost.change;
        }
        if (!TextUtils.isEmpty(newPost.source)) {
            source = newPost.source;
        }
        if (newPost.score != 0) {
            score = newPost.score;
        }
        if (!TextUtils.isEmpty(newPost.md5)) {
            md5 = newPost.md5;
        }
        if (newPost.fileSize != 0) {
            fileSize = newPost.fileSize;
        }
        if (!TextUtils.isEmpty(newPost.fileUrl)) {
            fileUrl = newPost.fileUrl;
        }
        if (newPost.isShownInIndex) {
            isShownInIndex = newPost.isShownInIndex;
        }
        if (!TextUtils.isEmpty(newPost.previewUrl)) {
            previewUrl = newPost.previewUrl;
        }
        if (newPost.previewWidth != 0) {
            previewWidth = newPost.previewWidth;
        }
        if (newPost.previewHeight != 0) {
            previewHeight = newPost.previewHeight;
        }
        if (newPost.actualPreviewWidth != 0) {
            actualPreviewWidth = newPost.actualPreviewWidth;
        }
        if (newPost.actualPreviewHeight != 0) {
            actualPreviewHeight = newPost.actualPreviewHeight;
        }
        if (!TextUtils.isEmpty(newPost.sampleUrl)) {
            sampleUrl = newPost.sampleUrl;
        }
        if (newPost.sampleWidth != 0) {
            sampleWidth = newPost.sampleWidth;
        }
        if (newPost.sampleHeight != 0) {
            sampleHeight = newPost.sampleHeight;
        }
        if (newPost.sampleFileSize != 0) {
            sampleFileSize = newPost.sampleFileSize;
        }
        if (!TextUtils.isEmpty(newPost.jpegUrl)) {
            jpegUrl = newPost.jpegUrl;
        }
        if (newPost.jpegWidth != 0) {
            jpegWidth = newPost.jpegWidth;
        }
        if (newPost.jpegHeight != 0) {
            jpegHeight = newPost.jpegHeight;
        }
        if (newPost.jpegFileSize != 0) {
            jpegFileSize = newPost.jpegFileSize;
        }
        if (!TextUtils.isEmpty(newPost.rating)) {
            rating = newPost.rating;
        }
        if (newPost.hasChildren) {
            hasChildren = newPost.hasChildren;
        }
        if (!TextUtils.isEmpty(newPost.parentId)) {
            parentId = newPost.parentId;
        }
        if (!TextUtils.isEmpty(newPost.status)) {
            status = newPost.status;
        }
        if (newPost.width != 0) {
            width = newPost.width;
        }
        if (newPost.height != 0) {
            height = newPost.height;
        }
        if (newPost.isHeld) {
            isHeld = newPost.isHeld;
        }
        if (!TextUtils.isEmpty(newPost.framesPendingString)) {
            framesPendingString = newPost.framesPendingString;
        }
        if (!TextUtils.isEmpty(newPost.framesString)) {
            framesString = newPost.framesString;
        }
        if (!TextUtils.isEmpty(newPost.flagDetail)) {
            flagDetail = newPost.flagDetail;
        }
    }

    /**
     * 获取fileSize最小的图片url，以便快速加载预览图
     *
     * @return
     */
    public String getMinSizeImageUrl() {
        String url = fileUrl;
        if (sampleFileSize != 0 && !TextUtils.equals(sampleUrl, fileUrl) && sampleFileSize <= fileSize) {
            url = sampleUrl;
            if (jpegFileSize != 0 && !TextUtils.equals(jpegUrl, sampleUrl) && jpegFileSize < sampleFileSize) {
                url = jpegUrl;
            }
        } else if (jpegFileSize != 0 && !TextUtils.equals(jpegUrl, fileUrl) && jpegFileSize < fileSize) {
            url = jpegUrl;
        }
        return url;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(tags);
        dest.writeLong(createdTime);
        dest.writeString(creatorId);
        dest.writeString(author);
        dest.writeString(change);
        dest.writeString(source);
        dest.writeInt(score);
        dest.writeString(md5);
        dest.writeLong(fileSize);
        dest.writeString(fileUrl);
        dest.writeByte((byte) (isShownInIndex ? 1 : 0));
        dest.writeString(previewUrl);
        dest.writeInt(previewWidth);
        dest.writeInt(previewHeight);
        dest.writeInt(actualPreviewWidth);
        dest.writeInt(actualPreviewHeight);
        dest.writeString(sampleUrl);
        dest.writeInt(sampleWidth);
        dest.writeInt(sampleHeight);
        dest.writeLong(sampleFileSize);
        dest.writeString(jpegUrl);
        dest.writeInt(jpegWidth);
        dest.writeInt(jpegHeight);
        dest.writeLong(jpegFileSize);
        dest.writeString(rating);
        dest.writeByte((byte) (hasChildren ? 1 : 0));
        dest.writeString(parentId);
        dest.writeString(status);
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeByte((byte) (isHeld ? 1 : 0));
        dest.writeString(framesPendingString);
        dest.writeString(framesString);
        dest.writeString(flagDetail);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PostBean> CREATOR = new Creator<PostBean>() {
        @Override
        public PostBean createFromParcel(Parcel in) {
            return new PostBean(in);
        }

        @Override
        public PostBean[] newArray(int size) {
            return new PostBean[size];
        }
    };
}
