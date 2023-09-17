package com.ess.anime.wallpaper.website.parser;

import android.text.Html;
import android.text.TextUtils;

import com.ess.anime.wallpaper.bean.CommentBean;
import com.ess.anime.wallpaper.bean.ImageBean;
import com.ess.anime.wallpaper.bean.PoolListBean;
import com.ess.anime.wallpaper.bean.ThumbBean;
import com.ess.anime.wallpaper.website.WebsiteConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SankakuParser extends HtmlParser {

    public SankakuParser(WebsiteConfig websiteConfig) {
        super(websiteConfig);
    }

    @Override
    public List<ThumbBean> getThumbList(Document doc) {
        List<ThumbBean> thumbList = new ArrayList<>();
        try {
            String json = doc.text();
            JsonArray items = new JsonParser().parse(json).getAsJsonArray();
            for (int i = 0; i < items.size(); i++) {
                try {
                    JsonObject item = items.get(i).getAsJsonObject();
                    boolean needSignUp = item.get("redirect_to_signup").getAsBoolean();
                    if (needSignUp) {
                        // 忽略需要登录才能显示的图片
                        continue;
                    }
                    String id = item.get("id").getAsString();
                    int thumbWidth = item.get("preview_width").getAsInt() * 2;
                    int thumbHeight = item.get("preview_height").getAsInt() * 2;
                    String thumbUrl = item.get("preview_url").getAsString();
                    if (thumbUrl.contains("download-preview.png")) {
                        // 封面为这张图片就是flash，不解析，无意义
                        continue;
                    }
                    int realWidth = item.get("width").getAsInt();
                    int realHeight = item.get("height").getAsInt();
                    String realSize = realWidth + " x " + realHeight;
                    String linkToShow = mWebsiteConfig.getBaseUrl() + "posts/" + id;
                    ThumbBean thumbBean = new ThumbBean(id, thumbWidth, thumbHeight, thumbUrl, realSize, linkToShow);
                    thumbBean.imageBean = parseImageBean(item);
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

    private ImageBean parseImageBean(JsonObject item) {
        try {
            Document doc = Jsoup.parse(item.toString());
            String json = getImageDetailJson(doc);
            return ImageBean.getImageDetailFromJson(json);
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
            JsonObject item = new JsonParser().parse(json).getAsJsonObject();

            // 解析图片信息
            builder.id(item.get("id").getAsString())
                    .createdTime(item.getAsJsonObject("created_at").get("s").getAsString())
                    .creatorId(item.getAsJsonObject("author").get("id").getAsString())
                    .author(item.getAsJsonObject("author").get("name").getAsString())
                    .source(!item.get("source").isJsonNull() ? item.get("source").getAsString() : "")
                    .score(item.get("total_score").getAsString())
                    .md5(item.get("md5").getAsString())
                    .fileUrl(item.get("file_url").getAsString())
                    .width(item.get("width").getAsString())
                    .height(item.get("height").getAsString())
                    .fileSize(item.get("file_size").getAsString())
                    .previewUrl(item.get("preview_url").getAsString())
                    .previewWidth(item.get("preview_width").getAsString())
                    .previewHeight(item.get("preview_height").getAsString())
                    .sampleUrl(item.get("sample_url").getAsString())
                    .sampleWidth(item.get("sample_width").getAsString())
                    .sampleHeight(item.get("sample_height").getAsString())
                    .sampleFileSize("-1")
                    .jpegUrl(item.get("file_url").getAsString())
                    .jpegWidth(item.get("width").getAsString())
                    .jpegHeight(item.get("height").getAsString())
                    .jpegFileSize(item.get("file_size").getAsString())
                    .rating(item.get("rating").getAsString())
                    .hasChildren(item.get("has_children").getAsString())
                    .parentId(!item.get("parent_id").isJsonNull() ? item.get("parent_id").getAsString() : "");

            // 解析tags
            StringBuilder tags = new StringBuilder();
            JsonArray tagArray = item.getAsJsonArray("tags");
            for (int i = 0; i < tagArray.size(); i++) {
                try {
                    JsonObject tagItem = tagArray.get(i).getAsJsonObject();
                    String tagName = tagItem.get("tagName").getAsString();
                    tags.append(tagName).append(" ");
                    int tagType = tagItem.get("type").getAsInt();
                    switch (tagType) {
                        default:
                        case 0:  // tag-type-general
                        case 9:  // tag-type-meta
                            builder.addGeneralTags(tagName);
                            break;
                        case 1:  // tag-type-artist
                            builder.addArtistTags(tagName);
                            break;
                        case 2:  // tag-type-studio
                            builder.addCircleTags(tagName);
                            break;
                        case 3:  // tag-type-copyright
                            builder.addCopyrightTags(tagName);
                            break;
                        case 4:  // tag-type-character
                            builder.addCharacterTags(tagName);
                            break;
                        case 5:  // tag-type-genre
                        case 8:  // tag-type-medium
                            builder.addStyleTags(tagName);
                            break;
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
        List<CommentBean> commentList = new ArrayList<>();
        Elements elements = doc.getElementsByClass("comment");
        for (Element e : elements) {
            try {
                Element a = e.getElementsByClass("avatar-medium").first();
                String href = a.attr("href");
                String id = "#c" + href.substring(href.lastIndexOf("/") + 1);
                Element img = a.getElementsByTag("img").first();
                String author = img.attr("title");
                String headUrl = img.attr("src");
                if (!headUrl.startsWith("http")) {
                    headUrl = "https:" + headUrl;
                }
                Element span = e.getElementsByClass("date").first();
                String date = span.attr("title").replace("  ", " ");
                Elements body = e.getElementsByClass("body");
                body.select("p").append("<br/><br/>");
                body.select("p").unwrap();
                Elements blockquote = body.select("blockquote");
                if (!blockquote.isEmpty()) {
                    blockquote.select("br").last().remove();
                    blockquote.select("br").last().remove();
                }
                CharSequence quote = Html.fromHtml(blockquote.html());
                blockquote.remove();
                body.select("br").last().remove();
                body.select("br").last().remove();
                CharSequence comment = Html.fromHtml(body.html());
                commentList.add(new CommentBean(id, author, date, headUrl, quote, comment));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return commentList;
    }

    @Override
    public List<PoolListBean> getPoolListList(Document doc) {
        List<PoolListBean> poolList = new ArrayList<>();
        try {
            String json = doc.text();
            JsonArray items = new JsonParser().parse(json).getAsJsonArray();
            for (int i = 0; i < items.size(); i++) {
                try {
                    JsonObject item = items.get(i).getAsJsonObject();
                    boolean needSignUp = item.get("redirect_to_signup").getAsBoolean();
                    if (needSignUp) {
                        // 忽略需要登录才能显示的图片
                        continue;
                    }
                    boolean isDeleted = item.get("is_deleted").getAsBoolean();
                    if (isDeleted) {
                        continue;
                    }
                    String id = item.get("id").getAsString();
                    PoolListBean poolListBean = new PoolListBean();
                    poolListBean.id = id;
                    poolListBean.name = item.get("name").getAsString();
                    poolListBean.creator = !item.get("author").isJsonNull() ? item.getAsJsonObject("author").get("name").getAsString() : "";
                    poolListBean.postCount = item.get("post_count").getAsString();
                    poolListBean.createTime = item.get("created_at").getAsString();
                    poolListBean.updateTime = item.get("updated_at").getAsString();
                    poolListBean.linkToShow = mWebsiteConfig.getBaseUrl() + "pools/" + id;

                    String thumbUrl = "";
                    if (TextUtils.isEmpty(thumbUrl)) {
                        String sampleUrlKey = "sample_url";
                        if (item.has(sampleUrlKey) && !item.get(sampleUrlKey).isJsonNull()) {
                            thumbUrl = item.get(sampleUrlKey).getAsString();
                        }
                    }
                    if (TextUtils.isEmpty(thumbUrl) || new URL(thumbUrl).getPath().endsWith(".webp")) {
                        String previewUrlKey = "preview_url";
                        if (item.has(previewUrlKey) && !item.get(previewUrlKey).isJsonNull()) {
                            thumbUrl = item.get(previewUrlKey).getAsString();
                        }
                    }
                    if (TextUtils.isEmpty(thumbUrl) || new URL(thumbUrl).getPath().endsWith(".webp")) {
                        String fileUrlKey = "file_url";
                        if (item.has(fileUrlKey) && !item.get(fileUrlKey).isJsonNull()) {
                            thumbUrl = item.get(fileUrlKey).getAsString();
                        }
                    }
                    poolListBean.thumbUrl = thumbUrl;
                    if (Integer.parseInt(poolListBean.postCount) > 0) {
                        poolList.add(poolListBean);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return poolList;
    }

    @Override
    public List<ThumbBean> getThumbListOfPool(Document doc) {
        try {
            String json = doc.text();
            JsonObject item = new JsonParser().parse(json).getAsJsonObject();
            JsonElement posts = item.get("posts");
            Document doc2 = Jsoup.parse(posts.toString());
            return getThumbList(doc2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}
