package com.ess.anime.wallpaper.website.parser;

import android.text.Html;

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
import java.util.List;
import java.util.TimeZone;

public class DanbooruParser extends HtmlParser {

    public DanbooruParser(WebsiteConfig websiteConfig) {
        super(websiteConfig);
    }

    @Override
    public List<ThumbBean> getThumbList(Document doc) {
        List<ThumbBean> thumbList = new ArrayList<>();
        Elements elements = doc.getElementsByTag("article");
        for (Element e : elements) {
            try {
                String id = e.attr("data-id");
                String thumbUrl = e.getElementsByTag("img").attr("src");
                if (!thumbUrl.startsWith("http")) {
                    thumbUrl = mWebsiteConfig.getBaseUrl() + thumbUrl;
                }
                int realWidth = Integer.valueOf(e.attr("data-width"));
                int realHeight = Integer.valueOf(e.attr("data-height"));
                String realSize = realWidth + " x " + realHeight;
                int thumbWidth, thumbHeight;
                if (realWidth >= realHeight) {
                    thumbWidth = 150;
                    thumbHeight = (int) (realHeight / 1f / realWidth * thumbWidth);
                } else {
                    thumbHeight = 150;
                    thumbWidth = (int) (realWidth / 1f / realHeight * thumbHeight);
                }
                String linkToShow = e.getElementsByTag("a").attr("href");
                if (!linkToShow.startsWith("http")) {
                    linkToShow = mWebsiteConfig.getBaseUrl() + linkToShow;
                }
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
            long mills = TimeFormat.timeToMillsWithZone(createdTime, "yyyy-MM-dd'T'HH:mm", TimeZone.getTimeZone("GMT-4:00"));
            createdTime = String.valueOf(mills / 1000);

            // 解析原图文件大小
            Element info = doc.getElementById("post-information");
            String jpegFileSize = "";
            for (Element li : info.getElementsByTag("li")) {
                if (li.text().contains("Size")) {
                    String size = li.getElementsByTag("a").first().text();
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
                    .author(image.attr("data-uploader"))
                    .source(container.attr("data-normalized-source"))
                    .score(container.attr("data-score"))
                    .md5(container.attr("data-md5"))
                    .fileSize(jpegFileSize)
                    .fileUrl(container.attr("data-file-url"))
                    .previewUrl(container.attr("data-preview-file-url"))
                    .sampleUrl(container.attr("data-large-file-url"))
                    .sampleWidth(image.attr("width"))
                    .sampleHeight(image.attr("height"))
                    .sampleFileSize("-1") // danbooru无法获得sample尺寸图片大小，又需要提供下载，因此用-1代替
                    .jpegUrl(container.attr("data-file-url"))
                    .jpegWidth(image.attr("data-original-width"))
                    .jpegHeight(image.attr("data-original-height"))
                    .jpegFileSize(jpegFileSize)
                    .rating(container.attr("data-rating"))
                    .hasChildren(container.attr("data-has-children"))
                    .parentId(container.attr("data-parent-id"))
                    .width(image.attr("data-original-width"))
                    .height(image.attr("data-original-height"))
                    .flagDetail(container.attr("data-flags"));

            // 解析图集信息
            Element pool = doc.getElementById("pool-nav");
            if (pool != null) {
                Element span = pool.getElementsByClass("pool-name").first();
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
                String id = "#c" + e.attr("data-comment-id");
                String author = e.attr("data-creator");
                String date = e.getElementsByTag("time").first().attr("title");
                long mills = TimeFormat.timeToMillsWithZone(date, "yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("GMT-4:00"));
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
        for (Element pool : doc.getElementsByTag("article")) {
            try {
                PoolListBean poolListBean = new PoolListBean();
                Element link = pool.getElementsByTag("a").last();
                String href = link.attr("href");
                poolListBean.id = href.replaceAll("[^0-9]", "");
                poolListBean.name = link.text().trim();
                poolListBean.linkToShow = mWebsiteConfig.getBaseUrl() + href;
                poolListBean.thumbUrl = pool.attr("data-large-file-url");
                poolList.add(poolListBean);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return poolList;
    }

}
