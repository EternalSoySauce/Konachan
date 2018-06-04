package com.ess.anime.wallpaper.http;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;

import com.ess.anime.wallpaper.bean.CommentBean;
import com.ess.anime.wallpaper.bean.ImageBean;
import com.ess.anime.wallpaper.bean.PoolListBean;
import com.ess.anime.wallpaper.bean.ThumbBean;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.utils.FileUtils;
import com.ess.anime.wallpaper.utils.StringUtils;
import com.ess.anime.wallpaper.utils.TimeFormat;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class ParseHtml {

    public static ArrayList<ThumbBean> getThumbList(String html) {
        ArrayList<ThumbBean> thumbList = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        String webTitle = doc.getElementsByTag("title").text();
        if (webTitle.toLowerCase().contains("danbooru")) {
            getDanbooruThumbList(doc, thumbList);
        } else if (webTitle.toLowerCase().contains("sankaku")) {
            getSankakuThumbList(doc, thumbList);
        } else {
            getGeneralThumbList(doc, thumbList);
        }
        return thumbList;
    }

    // Konachan,Yande,Lolibooru通用
    private static void getGeneralThumbList(Document doc, ArrayList<ThumbBean> thumbList) {
        Element list = doc.getElementById("post-list-posts");
        if (list == null) {
            return;
        }

        Elements elements = list.getElementsByTag("li");
        for (Element e : elements) {
            try {
                String thumbUrl = e.getElementsByTag("img").attr("src");
                if (thumbUrl.contains("deleted-preview")) {
                    continue;
                } else if (!thumbUrl.startsWith("http")) {
                    thumbUrl = "https:" + thumbUrl;
                }
                String realSize = "";
                Elements directLink = e.getElementsByClass("directlink-res");
                if (!directLink.isEmpty()) {
                    realSize = directLink.get(0).ownText();
                }
                String linkToShow = e.getElementsByClass("plid").get(0).ownText();
                linkToShow = linkToShow.substring(linkToShow.indexOf("http"));
                thumbList.add(new ThumbBean(thumbUrl, realSize, linkToShow));
            } catch (Exception ignore) {
            }
        }
    }

    // Danbooru专用
    private static void getDanbooruThumbList(Document doc, ArrayList<ThumbBean> thumbList) {
        Elements elements = doc.getElementsByTag("article");
        for (Element e : elements) {
            try {
                String thumbUrl = e.getElementsByTag("img").attr("src");
                if (!thumbUrl.startsWith("http")) {
                    thumbUrl = Constants.BASE_URL_DANBOORU + thumbUrl;
                }
                String realSize = e.attr("data-width") + " x " + e.attr("data-height");
                String linkToShow = e.getElementsByTag("a").attr("href");
                if (!linkToShow.startsWith("http")) {
                    linkToShow = Constants.BASE_URL_DANBOORU + linkToShow;
                }
                thumbList.add(new ThumbBean(thumbUrl, realSize, linkToShow));
            } catch (Exception ignore) {
            }
        }
    }

    // Sankaku专用
    private static void getSankakuThumbList(Document doc, ArrayList<ThumbBean> thumbList) {
        doc.select("#popular-preview").remove();
        Elements elements = doc.getElementsByClass("thumb blacklisted");
        for (Element e : elements) {
            try {
                Element img = e.getElementsByTag("img").first();
                String thumbUrl = img.attr("src");
                if (!thumbUrl.startsWith("http")) {
                    thumbUrl = "https:" + thumbUrl;
                }
                String title = img.attr("title");
                int startIndex = title.indexOf("Size:") + "Size:".length();
                int endIndex = title.indexOf("User:");
                String realSize = title.substring(startIndex, endIndex).trim().replace("x", " x ");
                String linkToShow = e.getElementsByTag("a").attr("href");
                if (!linkToShow.startsWith("http")) {
                    linkToShow = Constants.BASE_URL_SANKAKU + linkToShow;
                }
                thumbList.add(new ThumbBean(thumbUrl, realSize, linkToShow));
            } catch (Exception ignore) {
            }
        }
    }

    public static String getImageDetailJson(String html) {
        Document doc = Jsoup.parse(html);
        String webTitle = doc.getElementsByTag("title").text();
        if (webTitle.toLowerCase().contains("danbooru")) {
            return getDanbooruImageDetailJson(doc);
        }  else if (webTitle.toLowerCase().contains("sankaku")) {
            return getSankakuImageDetailJson(doc);
        } else {
            return getGeneralImageDetailJson(doc);
        }
    }

    private static String getGeneralImageDetailJson(Document doc) {
        try {
            Element div = doc.getElementById("post-view");
            String json = div.getElementsByTag("script").get(0).html();
            json = json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1);
            json = json.replace("\\/", "/");
            // konachan两种模式url格式总会不同
            if (!json.contains("http://konachan") && !json.contains("https://konachan")) {
                json = json.replace("//konachan", "https://konachan");
            }
            // lolibooru要把最后的"votes":[]统一为"votes":{}
            // TODO 目前为止votes这一属性全部为空，但不排除某一天某个网站有了投票活动，到时后再改replace（懒癌）
            json = json.replace("\"votes\":[]", "\"votes\":{}");
            return json;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private static String getDanbooruImageDetailJson(Document doc) {
        ImageBean.ImageJsonBuilder builder = new ImageBean.ImageJsonBuilder();
        try {
            // 解析时间字符串，格式：2018-05-29T21:06-04:00（-04:00为时区）
            // 注意PostBean.createdTime单位为second
            Element time = doc.getElementsByTag("time").get(0);
            String createdTime = time.attr("datetime");
            long mills = TimeFormat.timeToMillsWithZone(createdTime, "yyyy-MM-dd'T'HH:mm", TimeZone.getTimeZone("GMT-4:00"));
            createdTime = String.valueOf(mills / 1000);

            // 解析原图文件大小
            Element info = doc.getElementById("post-information");
            String jpegFileSize = "";
            for (Element li : info.getElementsByTag("li")) {
                if (li.text().contains("Size")) {
                    String size = li.getElementsByTag("a").get(0).text();
                    jpegFileSize = String.valueOf(FileUtils.parseFileSile(size));
                    break;
                }
            }

            Element container = doc.getElementById("image-container");
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
            Element pool = doc.getElementsByClass("pool-name active").first();
            if (pool != null) {
                Element a = pool.getElementsByTag("a").first();
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

            Element tag = doc.getElementById("tag-list");
            for (Element copyright : tag.getElementsByClass("category-3")) {
                builder.addCopyrightTags(copyright.getElementsByClass("search-tag")
                        .get(0).text().replace(" ", "_"));
            }
            for (Element character : tag.getElementsByClass("category-4")) {
                builder.addCharacterTags(character.getElementsByClass("search-tag")
                        .get(0).text().replace(" ", "_"));
            }
            for (Element artist : tag.getElementsByClass("category-1")) {
                builder.addArtistTags(artist.getElementsByClass("search-tag")
                        .get(0).text().replace(" ", "_"));
            }
            for (Element general : tag.getElementsByClass("category-0")) {
                builder.addGeneralTags(general.getElementsByClass("search-tag")
                        .get(0).text().replace(" ", "_"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return builder.build();
    }

    private static String getSankakuImageDetailJson(Document doc) {
        ImageBean.ImageJsonBuilder builder = new ImageBean.ImageJsonBuilder();
        return builder.build();
    }

    public static ArrayList<CommentBean> getCommentList(Context context, String html) {
        Document doc = Jsoup.parse(html);
        String webTitle = doc.getElementsByTag("title").text();
        if (webTitle.toLowerCase().contains("danbooru")) {
            return getDanbooruCommentList(doc);
        } else {
            return getGeneralCommentList(context, doc);
        }
    }

    private static ArrayList<CommentBean> getGeneralCommentList(Context context, Document doc) {
        ArrayList<CommentBean> commentList = new ArrayList<>();
        Elements elements = doc.getElementsByClass("comment avatar-container");
        for (Element e : elements) {
            try {
                String id = "#" + e.attr("id");
                String author = e.getElementsByTag("h6").get(0).text();
                String date = e.getElementsByClass("date").get(0).attr("title");
                String headUrl = "";
                Elements avatars = e.getElementsByClass("avatar");
                if (!avatars.isEmpty()) {
                    headUrl = avatars.get(0).attr("src");
                    if (!headUrl.startsWith("http")) {
                        headUrl = headUrl.startsWith("//") ? "https:" + headUrl
                                : OkHttp.getBaseUrl(context) + headUrl;
                    }
                }
                Element body = e.getElementsByClass("body").get(0);
                Elements blockquote = body.select("blockquote");
                CharSequence quote = Html.fromHtml(blockquote.select("div").html());
                blockquote.remove();
                CharSequence comment = Html.fromHtml(body.html());
                commentList.add(new CommentBean(id, author, date, headUrl, quote, comment));
            } catch (Exception ignore) {
            }
        }
        return commentList;
    }

    private static ArrayList<CommentBean> getDanbooruCommentList(Document doc) {
        ArrayList<CommentBean> commentList = new ArrayList<>();
        Elements elements = doc.getElementsByClass("comment");
        for (Element e : elements) {
            try {
                String id = "#c" + e.attr("data-comment-id");
                String author = e.attr("data-creator");
                String date = e.getElementsByTag("time").get(0).attr("title");
                long mills = TimeFormat.timeToMillsWithZone(date, "yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("GMT-4:00"));
                date = "Posted at " + TimeFormat.dateFormat(mills, "yyyy-MM-dd HH:mm:ss");
                String headUrl = "";
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
                String s = body.html();
                CharSequence comment = Html.fromHtml(s);
                commentList.add(new CommentBean(id, author, date, headUrl, quote, comment));
            } catch (Exception ignore) {
            }
        }
        return commentList;
    }

    public static String getNameFromBaidu(String html) {
        Document doc = Jsoup.parse(html);
        Elements eleKeys = doc.getElementsByClass("basicInfo-item name");
        ArrayList<String> keyList = new ArrayList<>();
        for (Element e : eleKeys) {
            keyList.add(e.text());
        }
        Elements eleValues = doc.getElementsByClass("basicInfo-item value");
        ArrayList<String> valueList = new ArrayList<>();
        for (Element e : eleValues) {
            valueList.add(e.text());
        }

        String name = "";
        for (int i = 0; i < keyList.size(); i++) {
            if (keyList.get(i).contains("名")) {
                String value = valueList.get(i);
                Pattern pattern = Pattern.compile("[a-zA-Z\\-]+[a-zA-Z\\s\\-]*[a-zA-Z\\-]+");
                String filterName = StringUtils.filter(value, pattern).trim();
                if (!TextUtils.isEmpty(filterName)) {
                    name = filterName;
                    break;
                }
            }
        }

        if (TextUtils.isEmpty(name)) {
            Elements eleParas = doc.getElementsByClass("para");
            for (Element e : eleParas) {
                String parentClass = e.parent().className();
                String para = e.text();
                if (para.contains("名") && !parentClass.contains("lemma-summary")) {
                    Pattern pattern = Pattern.compile("[a-zA-Z\\-]+[a-zA-Z\\s\\-]*[a-zA-Z\\-]+");
                    String filterName = StringUtils.filter(para, pattern);
                    if (!TextUtils.isEmpty(filterName)) {
                        name = filterName;
                        break;
                    }
                }
            }
        }

        name = name.replaceAll(" ", "_");
        return name;
    }

    public static ArrayList<PoolListBean> getPoolList(Context context, String html) {
        Document doc = Jsoup.parse(html);
        String webTitle = doc.getElementsByTag("title").text();
        if (webTitle.toLowerCase().contains("danbooru")) {
            return getDanbooruPoolList(doc);
        } else {
            return getGeneralPoolList(context, doc);
        }
    }

    private static ArrayList<PoolListBean> getGeneralPoolList(Context context, Document doc) {
        //解析预览图和id
        ArrayList<PoolListBean> poolList = new ArrayList<>();
        PoolListBean poolListBean = new PoolListBean();
        Elements eleScripts = doc.getElementsByTag("script");
        for (Element script : eleScripts) {
            String text = script.html().trim();
            if (text.startsWith("var thumb = $(\"hover-thumb\");")) {
                String[] texts = text.split("\n");
                for (String line : texts) {
                    line = line.trim();
                    if (line.startsWith("Post.register")) {
                        poolListBean = new PoolListBean();
                        line = line.substring(line.indexOf("{"), line.lastIndexOf(")"));
                        JsonObject json = new JsonParser().parse(line).getAsJsonObject();
                        String thumbUrl = json.get("sample_url").getAsString();
                        thumbUrl = thumbUrl.replace("\\/", "/");
                        if (!thumbUrl.startsWith("http")) {
                            thumbUrl = "https:" + thumbUrl;
                        }
                        poolListBean.thumbUrl = thumbUrl;
                    } else if (line.startsWith("var hover_row = $")) {
                        line = line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\""));
                        poolListBean.id = line;
                        poolList.add(poolListBean);
                    }
                }
            }
        }

        //根据id解析详细信息
        for (PoolListBean pool : poolList) {
            Element poolDetail = doc.getElementById(pool.id);
            Elements eleTds = poolDetail.getElementsByTag("td");
            for (int index = 0; index < eleTds.size(); index++) {
                Element td = eleTds.get(index);
                switch (index) {
                    case 0:
                        String linkToShow = td.getElementsByTag("a").get(0).attr("href");
                        linkToShow = OkHttp.getBaseUrl(context) + linkToShow.substring(1);
                        pool.linkToShow = linkToShow;
                        pool.name = td.text();
                        break;
                    case 1:
                        pool.creator = td.text();
                        break;
                    case 2:
                        pool.postCount = td.text();
                        break;
                    case 3:
                        pool.createTime = td.text();
                        break;
                    case 4:
                        pool.updateTime = td.text();
                        break;
                }
            }
        }
        return poolList;
    }

    private static ArrayList<PoolListBean> getDanbooruPoolList(Document doc) {
        ArrayList<PoolListBean> poolList = new ArrayList<>();
        for (Element pool : doc.select("tr[id]")) {
            try {
                PoolListBean poolListBean = new PoolListBean();
                Elements tds = pool.getElementsByTag("td");
                Element link = tds.get(1).getElementsByTag("a").first();
                String href = link.attr("href");
                poolListBean.id = href.substring(href.lastIndexOf("/") + 1);
                poolListBean.name = link.text();
                poolListBean.linkToShow = Constants.BASE_URL_DANBOORU + href;
                poolListBean.postCount = tds.last().text();
                poolList.add(poolListBean);
            } catch (Exception ignore) {
                ignore.printStackTrace();
            }
        }
        return poolList;
    }

    public static ArrayList<ThumbBean> getThumbListOfPool(String html) {
        ArrayList<ThumbBean> thumbList = getThumbList(html);
        Document doc = Jsoup.parse(html);
        Elements eleScripts = doc.getElementsByTag("script");
        for (Element script : eleScripts) {
            try {
                String text = script.html();
                String flag = "Post.register_resp(";
                if (text.contains(flag)) {
                    text = text.substring(text.indexOf(flag) + flag.length(), text.lastIndexOf(")"));
                    JsonObject json = new JsonParser().parse(text).getAsJsonObject();
                    JsonArray jsonArray = json.getAsJsonArray("posts");
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JsonObject post = jsonArray.get(i).getAsJsonObject();
                        String width = post.get("jpeg_width").getAsString();
                        String height = post.get("jpeg_height").getAsString();
                        String previewUrl = post.get("preview_url").getAsString();
                        previewUrl = previewUrl.substring(previewUrl.lastIndexOf("/") + 1);
                        for (ThumbBean thumbBean : thumbList) {
                            if (thumbBean.thumbUrl.contains(previewUrl)) {
                                thumbBean.realSize = width + " x " + height;
                                break;
                            }
                        }
                    }
                }
            } catch (Exception ignore) {
            }
        }
        return thumbList;
    }
}
