package com.ess.anime.wallpaper.website.parser;

import com.ess.anime.wallpaper.bean.CommentBean;
import com.ess.anime.wallpaper.bean.ImageBean;
import com.ess.anime.wallpaper.bean.PoolListBean;
import com.ess.anime.wallpaper.bean.PostBean;
import com.ess.anime.wallpaper.bean.ThumbBean;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.utils.TimeFormat;
import com.ess.anime.wallpaper.website.WebsiteConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;

public class WallhavenParser extends HtmlParser {

    public WallhavenParser(WebsiteConfig websiteConfig) {
        super(websiteConfig);
    }

    @Override
    public List<ThumbBean> getThumbList(Document doc) {
        List<ThumbBean> thumbList = new ArrayList<>();
        try {
            String json = doc.text();
            JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
            JsonArray items = jsonObject.getAsJsonArray("data");
            for (int i = 0; i < items.size(); i++) {
                try {
                    JsonObject item = items.get(i).getAsJsonObject();
                    String id = item.get("id").getAsString();
                    String thumbUrl = item.getAsJsonObject("thumbs").get("original").getAsString();
                    int realWidth = item.get("dimension_x").getAsInt();
                    int realHeight = item.get("dimension_y").getAsInt();
                    String realSize = realWidth + " x " + realHeight;
                    int thumbWidth, thumbHeight;
                    if (realWidth >= realHeight) {
                        thumbWidth = 720;
                        thumbHeight = (int) (realHeight / 1f / realWidth * thumbWidth);
                    } else {
                        thumbHeight = 720;
                        thumbWidth = (int) (realWidth / 1f / realHeight * thumbHeight);
                    }
                    String linkToShow = mWebsiteConfig.getPostDetailUrl(id);
                    ThumbBean thumbBean = new ThumbBean(id, thumbWidth, thumbHeight, thumbUrl, realSize, linkToShow);
                    thumbBean.needPreloadImageDetail = false; // Wallhaven API 限制每分钟最多访问45次，故不进行图片详情预加载
                    thumbBean.tempPost = parseTempPost(item);
                    thumbList.add(thumbBean);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return thumbList;
    }

    private PostBean parseTempPost(JsonObject item) {
        try {
            PostBean postBean = new PostBean();
            postBean.fileUrl = item.get("path").getAsString();
            return postBean;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public String getImageDetailJson(Document doc) {
        ImageBean.ImageJsonBuilder builder = new ImageBean.ImageJsonBuilder();
        try {
            String json = doc.text();
            JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
            JsonObject item = jsonObject.getAsJsonObject("data");

            // 解析时间字符串，格式：2023-09-15 11:45:52
            // 注意PostBean.createdTime单位为second
            String createdTime = item.get("created_at").getAsString();
            long mills = TimeFormat.timeToMills(createdTime, "yyyy-MM-dd HH:mm:ss");
            createdTime = String.valueOf(mills / 1000);
            builder.createdTime(createdTime);

            // 解析上传者信息
            JsonObject uploader = item.getAsJsonObject("uploader");
            String author = uploader.get("username").getAsString();
            builder.creatorId(author).author(author);

            // 解析收藏数
            int favorites = item.get("favorites").getAsInt();
            builder.score(String.valueOf(favorites));

            // 解析图片信息
            String id = item.get("id").getAsString();
            String source = item.get("source").getAsString();
            String thumbUrl = item.getAsJsonObject("thumbs").get("original").getAsString();
            String fileUrl = item.get("path").getAsString();
            String fileWidth = String.valueOf(item.get("dimension_x").getAsInt());
            String fileHeight = String.valueOf(item.get("dimension_y").getAsInt());
            String fileSize = String.valueOf(item.get("file_size").getAsLong());
            builder.id(id)
                    .source(source)
                    .fileUrl(fileUrl)
                    .fileSize(fileSize)
                    .previewUrl(thumbUrl)
                    .sampleUrl(fileUrl)
                    .sampleWidth(fileWidth)
                    .sampleHeight(fileHeight)
                    .sampleFileSize(fileSize)
                    .jpegUrl(fileUrl)
                    .jpegWidth(fileWidth)
                    .jpegHeight(fileHeight)
                    .jpegFileSize(fileSize)
                    .width(fileWidth)
                    .height(fileHeight);

            // 解析安全等级
            String purity = item.get("purity").getAsString();
            String rating;
            switch (purity) {
                default:
                case "sfw":
                    rating = Constants.RATING_S;
                    break;
                case "sketchy":
                    rating = Constants.RATING_Q;
                    break;
                case "nsfw":
                    rating = Constants.RATING_E;
                    break;
            }
            builder.rating(rating);

            // 防止为null的参数
            builder.md5("").parentId("");

            // 解析tags
            StringBuilder tags = new StringBuilder();
            JsonArray tagArray = item.getAsJsonArray("tags");
            for (int i = 0; i < tagArray.size(); i++) {
                try {
                    JsonObject tagItem = tagArray.get(i).getAsJsonObject();
                    String tagName = tagItem.get("name").getAsString();
                    int categoryId = tagItem.get("category_id").getAsInt();
                    tags.append(tagName).append(" ");
                    switch (categoryId) {
                        case 22:  // Anime & Manga » Series
                        case 23:  // Anime & Manga » Visual Novels
                        case 28:  // Entertainment » Comic Books & Graphic Novels
                        case 30:  // Entertainment » Games
                        case 32:  // Entertainment » Movies
                        case 33:  // Entertainment » Music
                        case 34:  // Entertainment » Sports
                        case 64:  // Entertainment » Television
                            builder.addCopyrightTags(tagName);
                            break;
                        case 20:  // Anime & Manga » Characters
                        case 48:  // People » Celebrities
                        case 49:  // People » Fictional Characters
                        case 50:  // People » Models
                        case 51:  // People » Other Figures
                        case 52:  // People » Pornstars
                        case 65:  // People » Musicians
                        case 66:  // People » Photographers
                            builder.addCharacterTags(tagName);
                            break;
                        case 47:  // People » Artists
                            builder.addArtistTags(tagName);
                            break;
                        case 24:  // Art & Design » Architecture
                        case 25:  // Art & Design » Digital
                        case 26:  // Art & Design » Photography
                        case 27:  // Art & Design » Traditional
                        case 31:  // Entertainment » Literature
                            builder.addStyleTags(tagName);
                            break;
                        default:
                            builder.addGeneralTags(tagName);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            builder.tags(tags.toString().trim());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return builder.build();
    }

    @Override
    public List<CommentBean> getCommentList(Document doc) {
        return new ArrayList<>();
    }

    @Override
    public List<PoolListBean> getPoolListList(Document doc) {
        return new ArrayList<>();
    }
}
