package com.ess.anime.wallpaper.bean;

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

    public static class ImageJsonBuilder {

        private PostBean postBean = new PostBean();

        public ImageJsonBuilder id(String id) {
            postBean.id = id;
            return this;
        }

        public ImageJsonBuilder tags(String tags) {
            postBean.tags = tags;
            return this;
        }

        public ImageJsonBuilder createdTime(String createdTime) {
            try {
                postBean.createdTime = Long.parseLong(createdTime);
            } catch (NumberFormatException ignore) {
                postBean.createdTime = 0;
            }
            return this;
        }

        public ImageJsonBuilder creatorId(String creatorId) {
            postBean.creatorId = creatorId;
            return this;
        }

        public ImageJsonBuilder author(String author) {
            postBean.author = author;
            return this;
        }

        public ImageJsonBuilder change(String change) {
            postBean.change = change;
            return this;
        }

        public ImageJsonBuilder source(String source) {
            postBean.source = source;
            return this;
        }

        public ImageJsonBuilder score(String score) {
            try {
                postBean.score = Integer.parseInt(score);
            } catch (NumberFormatException ignore) {
                postBean.score = 0;
            }
            return this;
        }

        public ImageJsonBuilder md5(String md5) {
            postBean.md5 = md5;
            return this;
        }

        public ImageJsonBuilder fileSize(String fileSize) {
            try {
                postBean.fileSize = Long.parseLong(fileSize);
            } catch (NumberFormatException ignore) {
                postBean.fileSize = 0;
            }
            return this;
        }

        public ImageJsonBuilder fileUrl(String fileUrl) {
            postBean.fileUrl = fileUrl;
            return this;
        }

        public ImageJsonBuilder isShownInIndex(String isShownInIndex) {
            postBean.isShownInIndex = Boolean.parseBoolean(isShownInIndex);
            return this;
        }

        public ImageJsonBuilder previewUrl(String previewUrl) {
            postBean.previewUrl = previewUrl;
            return this;
        }

        public ImageJsonBuilder previewWidth(String previewWidth) {
            try {
                postBean.previewWidth = Integer.parseInt(previewWidth);
            } catch (NumberFormatException ignore) {
                postBean.previewWidth = 0;
            }
            return this;
        }

        public ImageJsonBuilder previewHeight(String previewHeight) {
            try {
                postBean.previewHeight = Integer.parseInt(previewHeight);
            } catch (NumberFormatException ignore) {
                postBean.previewHeight = 0;
            }
            return this;
        }

        public ImageJsonBuilder actualPreviewWidth(String actualPreviewWidth) {
            try {
                postBean.actualPreviewWidth = Integer.parseInt(actualPreviewWidth);
            } catch (NumberFormatException ignore) {
                postBean.actualPreviewWidth = 0;
            }
            return this;
        }

        public ImageJsonBuilder actualPreviewHeight(String actualPreviewHeight) {
            try {
                postBean.actualPreviewHeight = Integer.parseInt(actualPreviewHeight);
            } catch (NumberFormatException ignore) {
                postBean.actualPreviewHeight = 0;
            }
            return this;
        }

        public ImageJsonBuilder sampleUrl(String sampleUrl) {
            postBean.sampleUrl = sampleUrl;
            return this;
        }

        public ImageJsonBuilder sampleWidth(String sampleWidth) {
            try {
                postBean.sampleWidth = Integer.parseInt(sampleWidth);
            } catch (NumberFormatException ignore) {
                postBean.sampleWidth = 0;
            }
            return this;
        }

        public ImageJsonBuilder sampleHeight(String sampleHeight) {
            try {
                postBean.sampleHeight = Integer.parseInt(sampleHeight);
            } catch (NumberFormatException ignore) {
                postBean.sampleHeight = 0;
            }
            return this;
        }

        public ImageJsonBuilder sampleFileSize(String sampleFileSize) {
            try {
                postBean.sampleFileSize = Long.parseLong(sampleFileSize);
            } catch (NumberFormatException ignore) {
                postBean.sampleFileSize = 0;
            }
            return this;
        }

        public ImageJsonBuilder jpegUrl(String jpegUrl) {
            postBean.jpegUrl = jpegUrl;
            return this;
        }

        public ImageJsonBuilder jpegWidth(String jpegWidth) {
            try {
                postBean.jpegWidth = Integer.parseInt(jpegWidth);
            } catch (NumberFormatException ignore) {
                postBean.jpegWidth = 0;
            }
            return this;
        }

        public ImageJsonBuilder jpegHeight(String jpegHeight) {
            try {
                postBean.jpegHeight = Integer.parseInt(jpegHeight);
            } catch (NumberFormatException ignore) {
                postBean.jpegHeight = 0;
            }
            return this;
        }

        public ImageJsonBuilder jpegFileSize(String jpegFileSize) {
            try {
                postBean.jpegFileSize = Long.parseLong(jpegFileSize);
            } catch (NumberFormatException ignore) {
                postBean.jpegFileSize = 0;
            }
            return this;
        }

        public ImageJsonBuilder rating(String rating) {
            postBean.rating = rating;
            return this;
        }

        public ImageJsonBuilder hasChildren(String hasChildren) {
            postBean.hasChildren = Boolean.parseBoolean(hasChildren);
            return this;
        }

        public ImageJsonBuilder parentId(String parentId) {
            postBean.parentId = parentId;
            return this;
        }

        public ImageJsonBuilder status(String status) {
            postBean.status = status;
            return this;
        }

        public ImageJsonBuilder width(String width) {
            try {
                postBean.width = Integer.parseInt(width);
            } catch (NumberFormatException ignore) {
                postBean.width = 0;
            }
            return this;
        }

        public ImageJsonBuilder height(String height) {
            try {
                postBean.height = Integer.parseInt(height);
            } catch (NumberFormatException ignore) {
                postBean.height = 0;
            }
            return this;
        }

        public ImageJsonBuilder isHeld(String isHeld) {
            postBean.isHeld = Boolean.parseBoolean(isHeld);
            return this;
        }

        public ImageJsonBuilder framesPendingString(String framesPendingString) {
            postBean.framesPendingString = framesPendingString;
            return this;
        }

        public ImageJsonBuilder framesPending(String framesPending) {
            postBean.framesPending = new Object[]{framesPending};
            return this;
        }

        public ImageJsonBuilder framesString(String framesString) {
            postBean.framesString = framesString;
            return this;
        }

        public ImageJsonBuilder frames(String frames) {
            postBean.frames = new Object[]{frames};
            return this;
        }

        public ImageJsonBuilder flagDetail(String flagDetail) {
            postBean.flagDetail = flagDetail;
            return this;
        }

        public String build() {
            return "{\"posts\":[{" +
                    "\"id\":" + postBean.id + "," +
                    "\"tags\":\"" + postBean.tags + "\"," +
                    "\"createdTime\":" + postBean.createdTime + "," +
                    "\"creator_id\":" + postBean.creatorId + "," +
                    "\"author\":\"" + postBean.author + "\"," +
                    "\"change\":\"" + postBean.change + "\"," +
                    "\"source\":\"" + postBean.source + "\"," +
                    "\"score\":" + postBean.score + "," +
                    "\"md5\":\"" + postBean.md5 + "\"," +
                    "\"fileSize\":" + postBean.fileSize + "," +
                    "\"file_url\":\"" + postBean.fileUrl + "\"," +
                    "\"isShownInIndex\":" + postBean.isShownInIndex + "," +
                    "\"preview_url\":\"" + postBean.previewUrl + "\"," +
                    "\"previewWidth\":" + postBean.previewWidth + "," +
                    "\"previewHeight\":" + postBean.previewHeight + "," +
                    "\"actualPreviewWidth\":" + postBean.actualPreviewWidth + "," +
                    "\"actualPreviewHeight\":" + postBean.actualPreviewHeight + "," +
                    "\"sample_url\":\"" + postBean.sampleUrl + "\"," +
                    "\"sample_width\":" + postBean.sampleWidth + "," +
                    "\"sample_height\":" + postBean.sampleHeight + "," +
                    "\"sampleFileSize\":" + postBean.sampleFileSize + "," +
                    "\"jpeg_url\":\"" + postBean.jpegUrl + "\"," +
                    "\"jpeg_width\":" + postBean.jpegWidth + "," +
                    "\"jpeg_height\":" + postBean.jpegHeight + "," +
                    "\"jpegFileSize\":" + postBean.jpegFileSize + "," +
                    "\"rating\":\"" + postBean.rating + "\"," +
                    "\"has_children\":" + postBean.hasChildren + "," +
                    "\"parent_id\":\"" + postBean.parentId + "\"," +
                    "\"status\":\"" + postBean.status + "\"," +
                    "\"width\":" + postBean.width + "," +
                    "\"height\":" + postBean.height + "," +
                    "\"isHeld\":" + postBean.isHeld + "," +
                    "\"framesPendingString\":\"" + postBean.framesPendingString + "\"," +
                    "\"framesPending\":[" +/* postBean.framesPending[0] +*/ "]," +
                    "\"framesString\":\"" + postBean.framesString + "\"," +
                    "\"frames\":[" + /*postBean.frames[0] +*/ "]," +
                    "\"flag_detail\":\"" + postBean.flagDetail + "\"" +
                    "}]," +
                    "\"pools\":[]," +
                    "\"pool_posts\":[]," +
                    "\"tags\":{}," +
                    "\"votes\":{}}";
        }
    }
}
