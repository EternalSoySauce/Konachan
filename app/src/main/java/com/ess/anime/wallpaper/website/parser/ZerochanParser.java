package com.ess.anime.wallpaper.website.parser;

import android.text.Html;
import android.text.TextUtils;

import com.ess.anime.wallpaper.bean.CommentBean;
import com.ess.anime.wallpaper.bean.ImageBean;
import com.ess.anime.wallpaper.bean.PoolListBean;
import com.ess.anime.wallpaper.bean.ThumbBean;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.utils.FileUtils;
import com.ess.anime.wallpaper.utils.TimeFormat;
import com.ess.anime.wallpaper.website.WebsiteConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ZerochanParser extends HtmlParser {

    public ZerochanParser(WebsiteConfig websiteConfig) {
        super(websiteConfig);
    }

    @Override
    public List<ThumbBean> getThumbList(Document doc) {
        List<ThumbBean> thumbList = parseThumbListByGeneralXml(doc);
        if (thumbList.isEmpty()) {
            thumbList = parseThumbListByPopularDailyJson(doc);
        }
        return thumbList;
    }

    private List<ThumbBean> parseThumbListByGeneralXml(Document doc) {
        List<ThumbBean> thumbList = new ArrayList<>();
        try {
            Elements items = doc.getElementsByTag("item");
            for (Element item : items) {
                try {
                    String link = item.getElementsByTag("link").first().text();
                    if (TextUtils.isEmpty(link)) {
                        // Jsoup 解析后把 <link></link> 变成了 <link>，导致无法正确获取 Element，最新1.16.1版本仍未修复
                        String[] lines = item.html().split("\n");
                        for (String line : lines) {
                            if (line.startsWith("<link>")) {
                                link = line.replace("<link>", "").replace("</link>", "").trim();
                                break;
                            }
                        }
                    }
                    String id = new URL(link).getPath().replaceAll("[^0-9]", "");
                    Element content = item.getElementsByTag("media:content").first();
                    int realWidth = Integer.parseInt(content.attr("width"));
                    int realHeight = Integer.parseInt(content.attr("height"));
                    String realSize = realWidth + " x " + realHeight;
                    String thumbUrl = content.attr("url");
                    String expression = content.attr("expression");
                    if (TextUtils.isEmpty(thumbUrl) || !TextUtils.equals(expression, "sample")) {
                        thumbUrl = item.getElementsByTag("media:thumbnail").attr("url");
                    }
                    int thumbWidth, thumbHeight;
                    if (realWidth >= realHeight) {
                        thumbWidth = 600;
                        thumbHeight = (int) (realHeight / 1f / realWidth * thumbWidth);
                    } else {
                        thumbHeight = 600;
                        thumbWidth = (int) (realWidth / 1f / realHeight * thumbHeight);
                    }
                    String linkToShow = mWebsiteConfig.getPostDetailUrl(id);
                    thumbList.add(new ThumbBean(id, thumbWidth, thumbHeight, thumbUrl, realSize, linkToShow));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return thumbList;
    }

    // 存在tag有带双引号的，但没加转义字符，Json解析报错，弃用
    // 例如：https://www.zerochan.net/4021426?p=1&l=50&s=id&json
    private List<ThumbBean> parseThumbListByGeneralJson(Document doc) {
        List<ThumbBean> thumbList = new ArrayList<>();
        try {
            String json = doc.text();
            String endPoint = "</div>";
            if (json.contains(endPoint)) {
                json = json.replace(json.substring(1, json.lastIndexOf(endPoint) + endPoint.length()), "");
            }
//            json = json.replaceAll("\\\\", "");

            JsonArray items = new JsonArray();
            JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
            if (jsonObject.has("items")) {
                items.addAll(jsonObject.getAsJsonArray("items"));
            } else {
                items.add(jsonObject);
            }
            for (int i = 0; i < items.size(); i++) {
                try {
                    JsonObject item = items.get(i).getAsJsonObject();
                    String id = item.get("id").getAsString();
                    String thumbUrl = "";
                    if (item.has("thumbnail")) {
                        thumbUrl = item.get("thumbnail").getAsString();
                    } else if (item.has("medium")) {
                        thumbUrl = item.get("medium").getAsString();
                    } else if (item.has("large")) {
                        thumbUrl = item.get("large").getAsString();
                    } else if (item.has("full")) {
                        thumbUrl = item.get("full").getAsString();
                    }
                    int realWidth = item.get("width").getAsInt();
                    int realHeight = item.get("height").getAsInt();
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
                    thumbList.add(new ThumbBean(id, thumbWidth, thumbHeight, thumbUrl, realSize, linkToShow));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return thumbList;
    }

    private List<ThumbBean> parseThumbListByPopularDailyJson(Document doc) {
        List<ThumbBean> thumbList = new ArrayList<>();
        try {
            Elements elements = doc.getElementsByClass(" ");
            for (Element element : elements) {
                if (!TextUtils.equals("li", element.tag().getName())) {
                    continue;
                }
                String id = element.getElementsByClass("fav").attr("data-id");
                Element img = element.getElementsByTag("img").first();
                String thumbUrl = img.attr("src");  // 未登录时网站代码
                if (TextUtils.isEmpty(thumbUrl)) {
                    thumbUrl = img.attr("data-src");  // 登录后网站代码
                }
                int thumbWidth = Integer.parseInt(img.attr("width"));
                int thumbHeight = Integer.parseInt(img.attr("height"));
                String realSize = img.attr("title");
                Pattern pattern = Pattern.compile("(\\d+)x(\\d+)");
                Matcher matcher = pattern.matcher(realSize);
                if (matcher.find()) {
                    int realWidth = Integer.parseInt(matcher.group(1));
                    int realHeight = Integer.parseInt(matcher.group(2));
                    realSize = realWidth + " x " + realHeight;
                }
                String linkToShow = mWebsiteConfig.getPostDetailUrl(id);
                thumbList.add(new ThumbBean(id, thumbWidth, thumbHeight, thumbUrl, realSize, linkToShow));
            }
        } catch (Exception e) {
            e.printStackTrace();
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
            JsonObject interactionStatistic = json.getAsJsonObject("interactionStatistic");
            if (interactionStatistic != null) {
                String favorite = interactionStatistic.get("userInteractionCount").getAsString();
                builder.score(favorite);
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
                if (text.contains("thumbX = ") && text.contains("thumbY = ")) {
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
            Element source = doc.getElementById("source-url");
            if (source != null) {
                builder.source(source.text().trim());
            }

            // 防止为null的参数
            builder.md5("").parentId("");

            // 解析tags
            Element preview = doc.getElementsByAttributeValueStarting("alt", "Tags:").first();
            builder.tags(preview.attr("alt").replace("Tags:", "").trim().replaceAll(",", ""));

            Element ul = doc.getElementById("tags");
            if (ul != null) {
                for (Element li : ul.getElementsByTag("li")) {
                    Element img = li.getElementsByTag("s").first();
                    String type = img != null ? img.className() : "";
                    String tag = li.text().trim();
                    switch (type) {
                        case "medium series":
                        case "medium game":
                        case "medium visual novel":
                        case "medium ova":
                        case "medium artbook":
                        case "medium movie":
                            builder.addCopyrightTags(tag);
                            break;
                        case "medium character":
                            builder.addCharacterTags(tag);
                            break;
                        case "medium mangaka":
                            builder.addArtistTags(tag);
                            break;
                        case "medium studio":
                        case "medium character Group":
                            builder.addCircleTags(tag);
                            break;
                        case "medium vtuber":
                            builder.addStyleTags(tag);
                            break;
                        default:
                            builder.addGeneralTags(tag);
                    }
                    if (TextUtils.equals(type, "medium unsafe-rating")) {
                        builder.rating(Constants.RATING_E);
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
            Elements elements = post.getElementsByAttributeValueStarting("id", "post-");
            for (Element e : elements) {
                try {
                    String id = "#" + e.attr("data-id");
                    String author = e.attr("data-author");
                    Element img = e.getElementsByTag("img").first();
                    String avatar = img != null ? img.attr("src") : "";
                    String date = "Posted at " + e.getElementsByTag("span").first().attr("title").trim();
                    String matchQuote;
                    String matchComment;
                    Element bb = e.getElementsByClass("bb").first();
                    Element blockquote = bb.getElementsByTag("blockquote").first();
                    if (blockquote != null) {
                        Element cite = blockquote.getElementsByTag("cite").first();
                        String quoteAuthor = cite.text();
                        cite.remove();
                        matchQuote = quoteAuthor + " said:<br>" + blockquote.html();
                        blockquote.remove();
                        Element br = bb.select("br").last();
                        if (br != null) {
                            br.remove();
                        }
                        matchComment = bb.html();
                    } else {
                        matchQuote = "";
                        matchComment = bb.html();
                    }
                    CharSequence quote = Html.fromHtml(matchQuote.trim());
                    CharSequence comment = Html.fromHtml(matchComment.trim());
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
