package com.ess.anime.wallpaper.website.parser;

import android.text.Html;

import com.ess.anime.wallpaper.bean.CommentBean;
import com.ess.anime.wallpaper.bean.ImageBean;
import com.ess.anime.wallpaper.bean.PoolListBean;
import com.ess.anime.wallpaper.bean.ThumbBean;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.utils.FileUtils;
import com.ess.anime.wallpaper.utils.TimeFormat;
import com.ess.anime.wallpaper.website.WebsiteConfig;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class ZerochanParser extends HtmlParser {

    public ZerochanParser(WebsiteConfig websiteConfig) {
        super(websiteConfig);
    }

    @Override
    public List<ThumbBean> getThumbList(Document doc) {
        List<ThumbBean> thumbList = new ArrayList<>();
        Elements elements = doc.getElementsByTag("li");
        for (Element e : elements) {
            try {
                Element img = e.getElementsByTag("img").first();
                if (img != null) {
                    String id = e.getElementsByTag("a").first().attr("href").replaceAll("[^0-9]", "");
                    String thumbUrl = img.attr("src");
                    String title = img.attr("title");
                    String realSize = title.substring(0, title.indexOf(" ")).trim().replace("x", " x ");
                    String style = img.attr("style");
                    int thumbWidth = Integer.parseInt(style.substring(0, style.indexOf(";")).replaceAll("[^0-9]", ""));
                    int thumbHeight = Integer.parseInt(style.substring(style.indexOf(";")).replaceAll("[^0-9]", ""));
                    String linkToShow = mWebsiteConfig.getBaseUrl() + id;
                    thumbList.add(new ThumbBean(id, thumbWidth, thumbHeight, thumbUrl, realSize, linkToShow));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return thumbList;
    }

    @Override
    public String getImageDetailJson(Document doc) {
        ImageBean.ImageJsonBuilder builder = new ImageBean.ImageJsonBuilder();
        try {
            Element scriptJson = doc.getElementsByAttributeValue("type", "application/ld+json").first();
            JsonObject json = new JsonParser().parse(scriptJson.data()).getAsJsonObject();

            // 解析时间字符串，格式：2023-01-22T03:42:27+00:00
            // 注意PostBean.createdTime单位为second
            String createdTime = json.get("datePublished").getAsString();
            long mills = TimeFormat.timeToMillsWithZone(createdTime, "yyyy-MM-dd'T'HH:mm:ss", TimeZone.getTimeZone("GMT-1:00"));
            createdTime = String.valueOf(mills / 1000);
            builder.createdTime(createdTime);

            // 解析收藏数
            Element favorite = doc.getElementsContainingOwnText("favorites.").first();
            if (favorite != null) {
                builder.score(favorite.ownText().replaceAll("[^0-9]", ""));
            }

            // 解析图片信息
            Element user = doc.getElementsByClass("user").first();
            String author = user != null ? user.text() : "";
            String fileSize = String.valueOf(FileUtils.parseFileSize(json.get("contentSize").getAsString()));
            String fileUrl = json.get("contentUrl").getAsString();
            String sampleUrl = json.get("thumbnail").getAsString();
            builder.creatorId(author)
                    .author(author)
                    .fileSize(fileSize)
                    .fileUrl(fileUrl)
                    .sampleUrl(sampleUrl)
                    .sampleFileSize("-1") // zerochan无法获得sample尺寸图片大小，又需要提供下载，因此用-1代替
                    .jpegUrl(fileUrl)
                    .jpegFileSize(fileSize)
                    .rating(Constants.RATING_S);

            Elements scripts = doc.getElementsByTag("script");
            for (Element scriptData : scripts) {
                String text = scriptData.data();
                if (text.contains("<![CDATA[")) {
                    String[] items = text.split(";");
                    for (String item : items) {
                        item = item.trim();
                        if (item.startsWith("id =")) {
                            builder.id(item.replaceAll("[^0-9]", ""));
                        } else if (item.startsWith("thumbX =")) {
                            builder.sampleWidth(item.replaceAll("[^0-9]", ""));
                        } else if (item.startsWith("thumbY =")) {
                            builder.sampleHeight(item.replaceAll("[^0-9]", ""));
                        } else if (item.startsWith("x =")) {
                            String width = item.replaceAll("[^0-9]", "");
                            builder.width(width).jpegWidth(width);
                        } else if (item.startsWith("y =")) {
                            String height = item.replaceAll("[^0-9]", "");
                            builder.height(height).jpegHeight(height);
                        }
                    }
                }
            }

            // 解析来源
            builder.source("");
            Elements elements = doc.getElementsByTag("h2");
            for (Element h2 : elements) {
                if (h2.text().trim().equalsIgnoreCase("Source")) {
                    Element source = h2.nextElementSibling();
                    if (source != null) {
                        builder.source(source.text().trim());
                    }
                }
            }

            // 防止为null的参数
            builder.md5("").parentId("");

            // 解析tags
            Element preview = doc.getElementsByAttributeValueStarting("alt", "Tags:").first();
            builder.tags(preview.attr("alt").replace("Tags:", "").trim().replaceAll(",", ""));

            Element ul = doc.getElementById("tags");
            if (ul != null) {
                for (Element li : ul.getElementsByTag("li")) {
                    Element img = li.getElementsByTag("img").first();
                    String type = img != null ? img.attr("alt") : "";
                    String tag = li.text().trim();
                    switch (type) {
                        case "Series":
                        case "Game":
                        case "Visual Novel":
                        case "OVA":
                        case "Artbook":
                        case "Movie":
                            builder.addCopyrightTags(tag);
                            break;
                        case "Character":
                            builder.addCharacterTags(tag);
                            break;
                        case "Mangaka":
                            builder.addArtistTags(tag);
                            break;
                        case "Studio":
                        case "Character Group":
                            builder.addCircleTags(tag);
                            break;
                        case "Theme":
                        case "VTuber":
                            builder.addStyleTags(tag);
                            break;
                        default:
                            builder.addGeneralTags(tag);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return builder.build();
    }

    @Override
    public List<CommentBean> getCommentList(Document doc) {
        List<CommentBean> commentList = new ArrayList<>();
        Element post = doc.getElementById("posts");
        if (post != null) {
            Elements elements = post.getElementsByTag("li");
            for (Element e : elements) {
                try {
                    Element person = e.getElementsByAttributeValueContaining("href", "comments").first();
                    String author = person.text().trim();
                    String id = "#" + author;
                    Element img = e.getElementsByAttributeValue("alt", "avatar").first();
                    String avatar = img != null ? img.attr("src") : "";
                    String date = e.getElementsByTag("span").first().text().trim();
                    CharSequence quote = "";
                    CharSequence comment = Html.fromHtml(e.getElementsByTag("p").last().html());
                    commentList.add(new CommentBean(id, author, date, avatar, quote, comment));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return commentList;
    }

    @Override
    public List<PoolListBean> getPoolListList(Document doc) {
        return new ArrayList<>();
    }
}
