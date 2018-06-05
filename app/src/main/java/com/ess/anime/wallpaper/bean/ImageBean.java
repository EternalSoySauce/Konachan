package com.ess.anime.wallpaper.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

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
        try {
            Gson gson = new Gson();
            ImageBean imageBean = gson.fromJson(json, ImageBean.class);
            imageBean.tags = new TagBean(imageBean.tagArray);
            return imageBean;
        } catch (Exception e) {
            e.printStackTrace();
            return new ImageBean();
        }
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
        private PoolBean poolBean = new PoolBean();
        private TagBean tagBean = new TagBean();

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
                if (score.contains(".")) {
                    postBean.score = Math.round(Float.parseFloat(score));
                } else {
                    postBean.score = Integer.parseInt(score);
                }
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

        public ImageJsonBuilder poolId(String id) {
            poolBean.id = id;
            return this;
        }

        public ImageJsonBuilder poolName(String name) {
            poolBean.name = name;
            return this;
        }

        public ImageJsonBuilder poolCreatedTime(String createdTime) {
            poolBean.createdTime = createdTime;
            return this;
        }

        public ImageJsonBuilder poolUpdatedTime(String updatedTime) {
            poolBean.updatedTime = updatedTime;
            return this;
        }

        public ImageJsonBuilder poolUserID(String userID) {
            poolBean.userID = userID;
            return this;
        }

        public ImageJsonBuilder poolIsPublic(String isPublic) {
            poolBean.isPublic = Boolean.parseBoolean(isPublic);
            return this;
        }

        public ImageJsonBuilder poolPostCount(String postCount) {
            try {
                poolBean.postCount = Integer.parseInt(postCount);
            } catch (NumberFormatException ignore) {
                poolBean.postCount = 0;
            }
            return this;
        }

        public ImageJsonBuilder poolDescription(String description) {
            poolBean.description = description;
            return this;
        }

        public ImageJsonBuilder addCopyrightTags(String... copyrightTags) {
            tagBean.copyright.addAll(Arrays.asList(copyrightTags));
            return this;
        }

        public ImageJsonBuilder addCharacterTags(String... characterTags) {
            tagBean.character.addAll(Arrays.asList(characterTags));
            return this;
        }

        public ImageJsonBuilder addArtistTags(String... artistTags) {
            tagBean.artist.addAll(Arrays.asList(artistTags));
            return this;
        }

        public ImageJsonBuilder addCircleTags(String... circleTags) {
            tagBean.circle.addAll(Arrays.asList(circleTags));
            return this;
        }

        public ImageJsonBuilder addStyleTags(String... styleTags) {
            tagBean.style.addAll(Arrays.asList(styleTags));
            return this;
        }

        public ImageJsonBuilder addGeneralTags(String... generalTags) {
            tagBean.general.addAll(Arrays.asList(generalTags));
            return this;
        }

        public String build() {
            StringBuilder json = new StringBuilder()
                    .append("{\"posts\":[{")
                    .append("\"id\":").append(postBean.id).append(",")
                    .append("\"tags\":\"").append(postBean.tags).append("\",")
                    .append("\"createdTime\":").append(postBean.createdTime).append(",")
                    .append("\"creator_id\":\"").append(postBean.creatorId).append("\",")
                    .append("\"author\":\"").append(postBean.author).append("\",")
                    .append("\"change\":\"").append(postBean.change).append("\",")
                    .append("\"source\":\"").append(postBean.source).append("\",")
                    .append("\"score\":").append(postBean.score).append(",")
                    .append("\"md5\":\"").append(postBean.md5).append("\",")
                    .append("\"fileSize\":").append(postBean.fileSize).append(",")
                    .append("\"file_url\":\"").append(postBean.fileUrl).append("\",")
                    .append("\"isShownInIndex\":").append(postBean.isShownInIndex).append(",")
                    .append("\"preview_url\":\"").append(postBean.previewUrl).append("\",")
                    .append("\"previewWidth\":").append(postBean.previewWidth).append(",")
                    .append("\"previewHeight\":").append(postBean.previewHeight).append(",")
                    .append("\"actualPreviewWidth\":").append(postBean.actualPreviewWidth).append(",")
                    .append("\"actualPreviewHeight\":").append(postBean.actualPreviewHeight).append(",")
                    .append("\"sample_url\":\"").append(postBean.sampleUrl).append("\",")
                    .append("\"sample_width\":").append(postBean.sampleWidth).append(",")
                    .append("\"sample_height\":").append(postBean.sampleHeight).append(",")
                    .append("\"sampleFileSize\":").append(postBean.sampleFileSize).append(",")
                    .append("\"jpeg_url\":\"").append(postBean.jpegUrl).append("\",")
                    .append("\"jpeg_width\":").append(postBean.jpegWidth).append(",")
                    .append("\"jpeg_height\":").append(postBean.jpegHeight).append(",")
                    .append("\"jpegFileSize\":").append(postBean.jpegFileSize).append(",")
                    .append("\"rating\":\"").append(postBean.rating).append("\",")
                    .append("\"has_children\":").append(postBean.hasChildren).append(",")
                    .append("\"parent_id\":\"").append(postBean.parentId).append("\",")
                    .append("\"status\":\"").append(postBean.status).append("\",")
                    .append("\"width\":").append(postBean.width).append(",")
                    .append("\"height\":").append(postBean.height).append(",")
                    .append("\"isHeld\":").append(postBean.isHeld).append(",")
                    .append("\"framesPendingString\":\"").append(postBean.framesPendingString).append("\",")
                    .append("\"framesPending\":[").append(/* postBean.framesPending[0] ).append(*/ "],")
                    .append("\"framesString\":\"").append(postBean.framesString).append("\",")
                    .append("\"frames\":[").append( /*postBean.frames[0] ).append(*/ "],")
                    .append("\"flag_detail\":\"").append(postBean.flagDetail).append("\"")
                    .append("}],")
                    .append("\"pools\":[");
            if (!TextUtils.isEmpty(poolBean.id)) {
                json.append("{\"id\":").append(poolBean.id).append(",")
                        .append("\"name\":\"").append(poolBean.name).append("\",")
                        .append("\"created_at\":\"").append(poolBean.createdTime).append("\",")
                        .append("\"updated_at\":\"").append(poolBean.updatedTime).append("\",")
                        .append("\"user_id\":").append(poolBean.userID).append(",")
                        .append("\"is_public\":").append(poolBean.isPublic).append(",")
                        .append("\"post_count\":").append(poolBean.postCount).append(",")
                        .append("\"description\":\"").append(poolBean.description).append("\"}");
            }
            json.append("],")
                    .append("\"pool_posts\":[],")
                    .append("\"tags\":{");
            for (String copyright : tagBean.copyright) {
                json.append("\"").append(copyright).append("\":\"copyright\",");
            }
            for (String character : tagBean.character) {
                json.append("\"").append(character).append("\":\"character\",");
            }
            for (String artist : tagBean.artist) {
                json.append("\"").append(artist).append("\":\"artist\",");
            }
            for (String circle : tagBean.circle) {
                json.append("\"").append(circle).append("\":\"circle\",");
            }
            for (String style : tagBean.style) {
                json.append("\"").append(style).append("\":\"style\",");
            }
            for (String general : tagBean.general) {
                json.append("\"").append(general).append("\":\"general\",");
            }
            if (json.charAt(json.length() - 1) == ',') {
                json.deleteCharAt(json.length() - 1);
            }
            json.append("},").append("\"votes\":{}}");
            return json.toString().replace("\\", "\\\\");
        }
    }
}
