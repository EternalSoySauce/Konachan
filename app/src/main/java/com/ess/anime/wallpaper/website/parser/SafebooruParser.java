package com.ess.anime.wallpaper.website.parser;

import com.ess.anime.wallpaper.bean.CommentBean;
import com.ess.anime.wallpaper.bean.ImageBean;
import com.ess.anime.wallpaper.bean.PoolListBean;
import com.ess.anime.wallpaper.bean.PostBean;
import com.ess.anime.wallpaper.bean.ThumbBean;
import com.ess.anime.wallpaper.utils.TimeFormat;
import com.ess.anime.wallpaper.website.WebsiteConfig;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class SafebooruParser extends HtmlParser {

    public SafebooruParser(WebsiteConfig websiteConfig) {
        super(websiteConfig);
    }

    @Override
    public List<ThumbBean> getThumbList(Document doc) {
        List<ThumbBean> thumbList = new ArrayList<>();
        Elements elements = doc.getElementsByTag("post");
        for (Element e : elements) {
            try {
                String id = e.id().replaceAll("[^0-9]", "");
                int thumbWidth = Integer.parseInt(e.attr("preview_width"));
                int thumbHeight = Integer.parseInt(e.attr("preview_height"));
                String thumbUrl = e.attr("preview_url").replaceFirst(".png|.jpeg", ".jpg");
                if (!thumbUrl.startsWith("http")) {
                    thumbUrl = "https:" + thumbUrl;
                }
                String realSize = e.attr("width") + " x " + e.attr("height");
                String linkToShow = mWebsiteConfig.getBaseUrl() + "index.php?page=post&s=view&id=" + id;
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
            postBean.id = e.attr("id");
            postBean.tags = e.attr("tags").trim();
            postBean.creatorId = e.attr("creator_id");
            postBean.change = e.attr("change");
            postBean.source = e.attr("source");
            postBean.md5 = e.attr("md5");
//            postBean.fileUrl = e.attr("file_url");
            postBean.fileSize = -1;
            postBean.previewUrl = e.attr("preview_url").replaceFirst(".png|.jpeg", ".jpg");
            postBean.previewWidth = Integer.parseInt(e.attr("preview_width"));
            postBean.previewHeight = Integer.parseInt(e.attr("preview_height"));
//            postBean.sampleUrl = e.attr("sample_url");
            postBean.sampleWidth = Integer.parseInt(e.attr("sample_width"));
            postBean.sampleHeight = Integer.parseInt(e.attr("sample_height"));
            postBean.sampleFileSize = -1;
//            postBean.jpegUrl = e.attr("file_url");
            postBean.jpegWidth = Integer.parseInt(e.attr("width"));
            postBean.jpegHeight = Integer.parseInt(e.attr("height"));
            postBean.jpegFileSize = -1;
            postBean.rating = e.attr("rating");
            postBean.hasChildren = Boolean.parseBoolean(e.attr("has_children"));
            postBean.parentId = e.attr("parent_id");
            postBean.status = e.attr("status");
            postBean.width = Integer.parseInt(e.attr("width"));
            postBean.height = Integer.parseInt(e.attr("height"));
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
                    // Id
                    builder.id(li.text().replaceAll("[^0-9]", ""));
                } else if (li.text().startsWith("Posted:")) {
                    // 解析时间字符串，格式：2020-04-28 02:00:02
                    // 注意PostBean.createdTime单位为second
                    String text = li.text();
                    String createdTime = text.substring(text.indexOf(":") + 1, text.indexOf("by")).trim();
                    long mills = TimeFormat.timeToMills(createdTime, "yyyy-MM-dd HH:mm:ss");
                    createdTime = String.valueOf(mills / 1000);
                    builder.createdTime(createdTime);

                    // 作者
                    String author = li.getElementsByTag("a").first().text().trim();
                    builder.author(author);
                }
            }

            // parseTempPost中解析的fileUrl、sampleUrl、jpegUrl有误，因此在这重新获取
            Element image = doc.getElementsByAttributeValue("property", "og:image").first();
            if (image != null) {
                String imgUrl = image.attr("content");
                builder.fileUrl(imgUrl).jpegUrl(imgUrl).sampleUrl(imgUrl);
            }
            Element sample = doc.getElementById("image");
            if (sample != null) {
                String imgUrl = sample.attr("src");
                if (imgUrl.contains("/samples/")) {
                    builder.sampleUrl(imgUrl).sampleFileSize("-1");
                }
            }

            // 防止为null的参数
            builder.source("").parentId("");

            // tags
            for (Element copyright : doc.getElementsByClass("tag-type-copyright")) {
                builder.addCopyrightTags(copyright.getElementsByTag("a")
                        .first().text().replace(" ", "_"));
            }
            for (Element character : doc.getElementsByClass("tag-type-character")) {
                builder.addCharacterTags(character.getElementsByTag("a")
                        .first().text().replace(" ", "_"));
            }
            for (Element artist : doc.getElementsByClass("tag-type-artist")) {
                builder.addArtistTags(artist.getElementsByTag("a")
                        .first().text().replace(" ", "_"));
            }
            for (Element general : doc.getElementsByClass("tag-type-metadata")) {
                builder.addGeneralTags(general.getElementsByTag("a")
                        .first().text().replace(" ", "_"));
            }
            for (Element general : doc.getElementsByClass("tag-type-general")) {
                builder.addGeneralTags(general.getElementsByTag("a")
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
        Elements elements = doc.getElementsByAttributeValue("style", "display:inline;");
        for (Element e : elements) {
            try {
                String id = e.id();
                if (id.startsWith("c")) {
                    id = "#" + id.replaceAll("[^0-9]", "");
                    String avatar = "";
                    String author = e.getElementsByTag("a").first().ownText();
                    String date = e.getElementsByTag("b").first().ownText();
                    date = date.substring(0, date.indexOf("Score:")).trim();
                    CharSequence quote = "";
                    CharSequence comment = e.ownText();
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
        Element tbody = doc.getElementsByTag("tbody").first();
        if (tbody != null) {
            Elements pools = tbody.getElementsByTag("tr");
            for (Element pool : pools) {
                try {
                    PoolListBean poolListBean = new PoolListBean();
                    Elements tds = pool.getElementsByTag("td");
                    Element detail = tds.get(0);
                    Element link = detail.getElementsByTag("a").first();
                    String href = link.attr("href");
                    poolListBean.id = href.substring(href.lastIndexOf("=") + 1);
                    poolListBean.name = link.text();
                    poolListBean.linkToShow = mWebsiteConfig.getBaseUrl() + href;
                    poolListBean.creator = tds.get(1).text();
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
        Elements elements = doc.getElementsByClass("thumb");
        for (Element e : elements) {
            try {
                String id = e.id().replaceAll("[^0-9]", "");
                int thumbWidth = 0;
                int thumbHeight = 0;
                Element img = e.getElementsByClass("preview").first();
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
        return thumbList;
    }

}
