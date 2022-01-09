package com.ess.anime.wallpaper.website.parser;

import android.text.Html;
import android.text.TextUtils;

import com.ess.anime.wallpaper.bean.CommentBean;
import com.ess.anime.wallpaper.bean.ImageBean;
import com.ess.anime.wallpaper.bean.PoolListBean;
import com.ess.anime.wallpaper.bean.ThumbBean;
import com.ess.anime.wallpaper.utils.FileUtils;
import com.ess.anime.wallpaper.utils.TimeFormat;
import com.ess.anime.wallpaper.website.WebsiteConfig;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

public class DanbooruParser extends HtmlParser {

    public DanbooruParser(WebsiteConfig websiteConfig) {
        super(websiteConfig);
    }

    @Override
    public List<ThumbBean> getThumbList(Document doc) {
        List<ThumbBean> thumbList = new ArrayList<>();
        Elements elements = doc.getElementsByTag("post");
        for (Element e : elements) {
            try {
                String id = e.getElementsByTag("id").first().text();
                Element img = e.getElementsByTag("preview-file-url").first();
                String thumbUrl = img.text().replace("preview", "360x360");
                if (!thumbUrl.startsWith("http")) {
                    thumbUrl = mWebsiteConfig.getBaseUrl() + thumbUrl;
                }
                int realWidth  = Integer.parseInt(e.getElementsByTag("image-width").first().text());
                int realHeight = Integer.parseInt(e.getElementsByTag("image-height").first().text());
                String realSize = realWidth + " x " + realHeight;
                int thumbWidth, thumbHeight;
                if (realWidth >= realHeight) {
                    thumbWidth = 360;
                    thumbHeight = (int) (realHeight / 1f / realWidth * thumbWidth);
                } else {
                    thumbHeight = 360;
                    thumbWidth = (int) (realWidth / 1f / realHeight * thumbHeight);
                }
                String linkToShow = mWebsiteConfig.getBaseUrl() + "posts/" + id;
                thumbList.add(new ThumbBean(id, thumbWidth, thumbHeight, thumbUrl, realSize, linkToShow));
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
            // 解析时间字符串，格式：2018-05-29T21:06-04:00（-04:00为时区）
            // 注意PostBean.createdTime单位为second
            Element time = doc.getElementsByTag("time").first();
            String createdTime = time.attr("datetime");
            long mills = TimeFormat.timeToMillsWithZone(createdTime, "yyyy-MM-dd'T'HH:mm", TimeZone.getTimeZone("GMT-5:00"));
            createdTime = String.valueOf(mills / 1000);

            // 解析原图文件大小
            Element info = doc.getElementById("post-information");
            String author = "";
            String jpegFileSize = "";
            for (Element li : info.getElementsByTag("li")) {
                if (TextUtils.equals(li.id(), "post-info-uploader")) {
                    author = li.getElementsByTag("a").first().attr("data-user-name");
                } else if (TextUtils.equals(li.id(), "post-info-size")) {
                    String size = li.getElementsByTag("a").first().text();
                    if (size.contains(" ") && size.indexOf(" ") != size.lastIndexOf(" ")) {
                        size = size.substring(0, size.lastIndexOf(" "));
                    }
                    jpegFileSize = String.valueOf(FileUtils.parseFileSize(size));
                    break;
                }
            }

            Element container = doc.getElementsByClass("image-container").first();
            Element image = doc.getElementById("image");
            builder.id(container.attr("data-id"))
                    .tags(container.attr("data-tags"))
                    .createdTime(createdTime)
                    .creatorId(container.attr("data-uploader-id"))
                    .author(author)
                    .source(container.attr("data-normalized-source"))
                    .score(container.attr("data-score"))
                    .md5(container.attr("data-md5"))
                    .fileSize(jpegFileSize)
                    .fileUrl(container.attr("data-file-url"))
                    .previewUrl(container.attr("data-preview-file-url"))
                    .sampleUrl(image.attr("src"))
                    .sampleWidth(image.attr("width"))
                    .sampleHeight(image.attr("height"))
                    .sampleFileSize("-1") // danbooru无法获得sample尺寸图片大小，又需要提供下载，因此用-1代替
                    .jpegUrl(container.attr("data-file-url"))
                    .jpegWidth(container.attr("data-width"))
                    .jpegHeight(container.attr("data-height"))
                    .jpegFileSize(jpegFileSize)
                    .rating(container.attr("data-rating"))
                    .hasChildren(container.attr("data-has-children"))
                    .parentId(container.attr("data-parent-id"))
                    .width(container.attr("data-width"))
                    .height(container.attr("data-height"))
                    .flagDetail(container.attr("data-flags"));

            // 解析图集信息
            Element span = doc.getElementsByClass("pool-name").first();
            if (span != null) {
                Element a = span.getElementsByTag("a").first();
                if (a != null) {
                    String href = a.attr("href");
                    builder.poolId(href.substring(href.lastIndexOf("/") + 1));
                    String name = a.text();
                    builder.poolName(name.substring(name.indexOf(":") + 1).trim());
                    builder.poolCreatedTime("");
                    builder.poolUpdatedTime("");
                    builder.poolDescription("");
                }
            }

            // tags
            Element tag = doc.getElementById("tag-list");
            for (Element copyright : tag.getElementsByClass("tag-type-3")) {
                builder.addCopyrightTags(copyright.getElementsByClass("search-tag")
                        .first().text().replace(" ", "_"));
            }
            for (Element character : tag.getElementsByClass("tag-type-4")) {
                builder.addCharacterTags(character.getElementsByClass("search-tag")
                        .first().text().replace(" ", "_"));
            }
            for (Element artist : tag.getElementsByClass("tag-type-1")) {
                builder.addArtistTags(artist.getElementsByClass("search-tag")
                        .first().text().replace(" ", "_"));
            }
            for (Element general : tag.getElementsByClass("tag-type-0")) {
                builder.addGeneralTags(general.getElementsByClass("search-tag")
                        .first().text().replace(" ", "_"));
            }
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
                String id = "#c" + e.attr("data-id");
                String author = e.getElementsByClass("user").first().attr("data-user-name");
                String date = e.getElementsByTag("time").first().attr("title");
                long mills = TimeFormat.timeToMillsWithZone(date, "yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("GMT-5:00"));
                date = "Posted at " + TimeFormat.dateFormat(mills, "yyyy-MM-dd HH:mm:ss");
                String avatar = "";
                Elements body = e.getElementsByClass("body prose");
                body.select(".spoiler").unwrap();
                body.select(".info").remove();
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
                commentList.add(new CommentBean(id, author, date, avatar, quote, comment));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return commentList;
    }

    @Override
    public List<PoolListBean> getPoolListList(Document doc) {
        List<PoolListBean> poolList = new ArrayList<>();
        for (Element e : doc.getElementsByTag("pool")) {
            try {
                PoolListBean poolListBean = new PoolListBean();
                poolListBean.id = e.getElementsByTag("id").first().text();
                poolListBean.name = e.getElementsByTag("name").first().text();
                poolListBean.linkToShow = mWebsiteConfig.getPostUrl(1, Arrays.asList("pool:" + poolListBean.id, "order:id"));
                poolListBean.createTime = TimeFormat.dateFormat(TimeFormat.timeToMillsWithZone(e.getElementsByTag("created-at").first().text(),
                        "yyyy-MM-dd'T'HH:mm", TimeZone.getTimeZone("GMT-5:00")), "yyyy-MM-dd HH:mm:ss");
                poolListBean.updateTime = TimeFormat.dateFormat(TimeFormat.timeToMillsWithZone(e.getElementsByTag("updated-at").first().text(),
                        "yyyy-MM-dd'T'HH:mm", TimeZone.getTimeZone("GMT-5:00")), "yyyy-MM-dd HH:mm:ss");
                poolListBean.postCount = e.getElementsByTag("post-count").first().text();
                poolList.add(poolListBean);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return poolList;
    }

    @Override
    public List<ThumbBean> getThumbListOfPool(Document doc) {
        return getThumbList(doc);
    }
}
