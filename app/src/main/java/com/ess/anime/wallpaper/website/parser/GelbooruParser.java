package com.ess.anime.wallpaper.website.parser;

import android.text.Html;
import android.text.TextUtils;

import com.ess.anime.wallpaper.bean.CommentBean;
import com.ess.anime.wallpaper.bean.ImageBean;
import com.ess.anime.wallpaper.bean.PoolListBean;
import com.ess.anime.wallpaper.bean.PostBean;
import com.ess.anime.wallpaper.bean.ThumbBean;
import com.ess.anime.wallpaper.utils.StringUtils;
import com.ess.anime.wallpaper.utils.TimeFormat;
import com.ess.anime.wallpaper.website.WebsiteConfig;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GelbooruParser extends HtmlParser {

    public GelbooruParser(WebsiteConfig websiteConfig) {
        super(websiteConfig);
    }

    @Override
    public List<ThumbBean> getThumbList(Document doc) {
        List<ThumbBean> thumbList = new ArrayList<>();
        Elements elements = doc.getElementsByTag("post");
        for (Element e : elements) {
            try {
                String id = e.getElementsByTag("id").first().text().replaceAll("[^0-9]", "");
                int thumbWidth = Integer.parseInt(e.getElementsByTag("preview_width").first().text());
                int thumbHeight = Integer.parseInt(e.getElementsByTag("preview_height").first().text());
                String thumbUrl = e.getElementsByTag("preview_url").first().text();
                if (!thumbUrl.startsWith("http")) {
                    thumbUrl = "https:" + thumbUrl;
                }
                String realSize = e.getElementsByTag("width").first().text() + " x " + e.getElementsByTag("height").first().text();
                String linkToShow = mWebsiteConfig.getPostDetailUrl(id);
                ThumbBean thumbBean = new ThumbBean(id, thumbWidth, thumbHeight, thumbUrl, realSize, linkToShow);
                thumbBean.tempPost = parseTempPost(e);
                thumbList.add(thumbBean);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return thumbList;
    }

    private PostBean parseTempPost(Element e) {
        try {
            PostBean postBean = new PostBean();
            postBean.id = e.getElementsByTag("id").first().text();
            postBean.tags = e.getElementsByTag("tags").first().text().trim();
            postBean.creatorId = e.getElementsByTag("creator_id").first().text();
            postBean.change = e.getElementsByTag("change").first().text();
            postBean.source = e.getElementsByTag("source").first().text();
            postBean.score = Integer.parseInt(e.getElementsByTag("score").first().text());
            postBean.md5 = e.getElementsByTag("md5").first().text();
            postBean.fileUrl = e.getElementsByTag("file_url").first().text();
            postBean.fileSize = -1;
            postBean.previewUrl = e.getElementsByTag("preview_url").first().text();
            postBean.previewWidth = Integer.parseInt(e.getElementsByTag("preview_width").first().text());
            postBean.previewHeight = Integer.parseInt(e.getElementsByTag("preview_height").first().text());
            postBean.sampleUrl = e.getElementsByTag("sample_url").first().text();
            if (TextUtils.isEmpty(postBean.sampleUrl)) {
                postBean.sampleUrl = postBean.fileUrl;
            }
            postBean.sampleWidth = Integer.parseInt(e.getElementsByTag("sample_width").first().text());
            postBean.sampleHeight = Integer.parseInt(e.getElementsByTag("sample_height").first().text());
            postBean.sampleFileSize = -1;
            postBean.jpegUrl = e.getElementsByTag("file_url").first().text();
            postBean.jpegWidth = Integer.parseInt(e.getElementsByTag("width").first().text());
            postBean.jpegHeight = Integer.parseInt(e.getElementsByTag("height").first().text());
            postBean.jpegFileSize = -1;
            postBean.rating = e.getElementsByTag("rating").first().text();
            postBean.hasChildren = Boolean.parseBoolean(e.getElementsByTag("has_children").first().text());
            postBean.parentId = e.getElementsByTag("parent_id").first().text();
            postBean.status = e.getElementsByTag("status").first().text();
            postBean.width = Integer.parseInt(e.getElementsByTag("width").first().text());
            postBean.height = Integer.parseInt(e.getElementsByTag("height").first().text());
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
            // 解析基础图片信息
            for (Element li : doc.getElementsByTag("li")) {
                if (li.text().startsWith("Id:")) {
                    try {
                        // Id
                        builder.id(li.text().replaceAll("[^0-9]", ""));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (li.text().startsWith("Posted:")) {
                    try {
                        // 解析时间字符串，格式：2019-02-07 19:30:27
                        // 注意PostBean.createdTime单位为second
                        String text = li.text();
                        String createdTime = text.substring(text.indexOf(":") + 1, text.indexOf("Uploader")).trim();
                        long mills = TimeFormat.timeToMills(createdTime, "yyyy-MM-dd HH:mm:ss");
                        createdTime = String.valueOf(mills / 1000);
                        builder.createdTime(createdTime);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        // 作者
                        String author = li.getElementsByTag("a").first().text().trim();
                        builder.author(author);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            // 防止为null的参数
            builder.source("").parentId("");

            // tags
            for (Element copyright : doc.getElementsByClass("tag-type-copyright")) {
                builder.addCopyrightTags(copyright.getElementsByTag("a")
                        .get(1).text().replace(" ", "_"));
            }
            for (Element character : doc.getElementsByClass("tag-type-character")) {
                builder.addCharacterTags(character.getElementsByTag("a")
                        .get(1).text().replace(" ", "_"));
            }
            for (Element artist : doc.getElementsByClass("tag-type-artist")) {
                builder.addArtistTags(artist.getElementsByTag("a")
                        .get(1).text().replace(" ", "_"));
            }
            for (Element general : doc.getElementsByClass("tag-type-metadata")) {
                builder.addGeneralTags(general.getElementsByTag("a")
                        .get(1).text().replace(" ", "_"));
            }
            for (Element general : doc.getElementsByClass("tag-type-general")) {
                builder.addGeneralTags(general.getElementsByTag("a")
                        .get(1).text().replace(" ", "_"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return builder.build();
    }

    @Override
    public List<CommentBean> getCommentList(Document doc) {
        List<CommentBean> commentList = new ArrayList<>();
        Elements elements = doc.getElementsByTag("div");
        for (Element e : elements) {
            try {
                Element commentAvatar = e.getElementsByClass("commentAvatar").first();
                Element commentBody = e.getElementsByClass("commentBody").first();
                if (commentAvatar != null && commentAvatar.parent() == e && commentBody != null && commentBody.parent() == e) {
                    String avatar = mWebsiteConfig.getBaseUrl() + "user_avatars/avatar_anonymous.jpg";
                    Element profileAvatar = commentAvatar.getElementsByClass("profileAvatar").first();
                    if (profileAvatar != null) {
                        String style = profileAvatar.attr("style");
                        Pattern pattern = Pattern.compile("url\\(['\"]([^'\"]+)['\"]\\)");
                        Matcher matcher = pattern.matcher(style);
                        if (matcher.find()) {
                            avatar = matcher.group(1);
                            if (!StringUtils.isURL(avatar)) {
                                avatar = mWebsiteConfig.getBaseUrl() + avatar;
                            }
                        }
                    }

                    String author = commentBody.getElementsByTag("a").first().text().trim();
                    String body = commentBody.ownText().trim();
                    Pattern pattern = Pattern.compile("(.*)».*(#\\d*)(.*)", Pattern.DOTALL);
                    Matcher matcher = pattern.matcher(body);
                    if (!matcher.find()) {
                        continue;
                    }
                    String id = matcher.group(2).trim();
                    String date = matcher.group(1).trim();
                    CharSequence quote = "";
                    CharSequence comment = Html.fromHtml(matcher.group(3).trim());
                    commentList.add(new CommentBean(id, author, date, avatar, quote, comment));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return commentList;
    }

    @Override
    public List<PoolListBean> getPoolListList(Document doc) {
        List<PoolListBean> poolList = new ArrayList<>();
        Element table = doc.getElementsByClass("highlightable").first();
        if (table != null) {
            Elements pools = table.getElementsByTag("tr");
            for (Element pool : pools) {
                try {
                    PoolListBean poolListBean = new PoolListBean();
                    Elements tds = pool.getElementsByTag("td");
                    Element detail = tds.get(1);
                    Element link = detail.getElementsByTag("a").first();
                    String href = link.attr("href");
                    poolListBean.id = href.substring(href.lastIndexOf("=") + 1);
                    poolListBean.name = link.text();
                    poolListBean.linkToShow = mWebsiteConfig.getBaseUrl() + href;
                    poolListBean.creator = detail.getElementsByTag("a").get(1).text();
                    poolListBean.updateTime = detail.text().substring(detail.text().lastIndexOf("about"));
                    poolListBean.postCount = tds.get(2).text().replaceAll("[^0-9]", "");
                    if (Integer.parseInt(poolListBean.postCount) > 0) {
                        poolList.add(poolListBean);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return poolList;
    }

    @Override
    public List<ThumbBean> getThumbListOfPool(Document doc) {
        List<ThumbBean> thumbList = new ArrayList<>();
        Element container = doc.getElementsByClass("thumbnail-container").first();
        if (container != null) {
            Elements elements = doc.getElementsByTag("span");
            for (Element e : elements) {
                try {
                    String id = e.id().replaceAll("[^0-9]", "");
                    int thumbWidth = 0;
                    int thumbHeight = 0;
                    Element img = e.getElementsByClass("thumbnail-preview").first();
                    String thumbUrl = img.attr("src");
                    if (!thumbUrl.startsWith("http")) {
                        thumbUrl = "https:" + thumbUrl;
                    }
                    String realSize = e.attr("width") + " x " + e.attr("height");
                    String linkToShow = mWebsiteConfig.getBaseUrl() + "index.php?page=post&s=view&id=" + id;
                    ThumbBean thumbBean = new ThumbBean(id, thumbWidth, thumbHeight, thumbUrl, realSize, linkToShow);
                    thumbList.add(thumbBean);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return thumbList;
    }

}
