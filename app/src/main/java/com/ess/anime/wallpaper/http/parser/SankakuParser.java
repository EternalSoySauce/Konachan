package com.ess.anime.wallpaper.http.parser;

import android.text.Html;

import com.ess.anime.wallpaper.bean.CommentBean;
import com.ess.anime.wallpaper.bean.ImageBean;
import com.ess.anime.wallpaper.bean.PoolListBean;
import com.ess.anime.wallpaper.bean.ThumbBean;
import com.ess.anime.wallpaper.utils.TimeFormat;
import com.ess.anime.wallpaper.website.WebsiteConfig;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class SankakuParser extends HtmlParser {

    public SankakuParser(WebsiteConfig websiteConfig) {
        super(websiteConfig);
    }

    @Override
    public List<ThumbBean> getThumbList(Document doc) {
        List<ThumbBean> thumbList = new ArrayList<>();
        //        doc.select("#popular-preview").remove();  // 移除最上方的四张popular
        Elements elements = doc.getElementsByClass("thumb blacklisted");
        for (Element e : elements) {
            try {
                String id = e.attr("id").replaceAll("[^0-9]", "");
                Element img = e.getElementsByTag("img").first();
                int thumbWidth = Integer.valueOf(img.attr("width")) * 2;
                int thumbHeight = Integer.valueOf(img.attr("height")) * 2;
                String thumbUrl = img.attr("src");
                if (thumbUrl.contains("download-preview.png")) {
                    // 封面为这张图片就是flash，不解析，无意义
                    continue;
                }
                if (!thumbUrl.startsWith("http")) {
                    thumbUrl = "https:" + thumbUrl;
                }
                String title = img.attr("title");
                int startIndex = title.indexOf("Size:") + "Size:".length();
                int endIndex = title.indexOf("User:");
                String realSize = title.substring(startIndex, endIndex).trim().replace("x", " x ");
                String linkToShow = e.getElementsByTag("a").attr("href");
                if (!linkToShow.startsWith("http")) {
                    linkToShow = mWebsiteConfig.getBaseUrl() + linkToShow;
                }
                // 最上方的四张有可能和普通列表中的图片重复，需要排除
                ThumbBean thumbBean = new ThumbBean(id, thumbWidth, thumbHeight, thumbUrl, realSize, linkToShow);
                if (!thumbList.contains(thumbBean)) {
                    thumbList.add(thumbBean);
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
            Element image = doc.getElementById("image");
            builder.id(doc.getElementById("hidden_post_id").text())
                    .tags(image.attr("alt"))
                    .md5(image.attr("pagespeed_url_hash"));

            // 下列li不存在某属性时的备用值
            builder.sampleUrl("https:" + image.attr("src"))
                    .source("")
                    .creatorId("")
                    .author("");

            Elements lis = doc.getElementsByTag("li");
            for (Element li : lis) {
                String text = li.text();
                if (text.contains("Posted:")) {
                    // 解析时间字符串，格式：2018-06-04 08:10（该站时区为-06:00）
                    // 注意PostBean.createdTime单位为second
                    Elements created = li.getElementsByTag("a");
                    String createdTime = created.attr("title");
                    long mills = TimeFormat.timeToMillsWithZone(createdTime, "yyyy-MM-dd HH:mm", TimeZone.getTimeZone("GMT-5:00"));
                    builder.createdTime(String.valueOf(mills / 1000));

                    // 上传用户
                    if (created.size() > 1) {
                        Element author = created.get(1);
                        builder.creatorId(author.attr("href").replaceAll("[^0-9]", ""));
                        builder.author(author.text());
                    }
                } else if (text.contains("Vote Average:")) {
                    // 评分
                    builder.score(li.child(0).text());
                } else if (text.contains("Resized:")) {
                    // 预览图
                    Element sample = li.child(0);
                    String sampleUrl = sample.attr("href");
                    if (!sampleUrl.startsWith("http")) {
                        sampleUrl = "https:" + sampleUrl;
                    }
                    String[] resolution = sample.text().split("x");
                    builder.sampleUrl(sampleUrl)
                            .sampleWidth(resolution[0])
                            .sampleHeight(resolution[1])
                            .sampleFileSize("-1");
                } else if (text.contains("Original:")) {
                    // 大图，原图
                    Element original = li.child(0);
                    String originalUrl = original.attr("href");
                    if (!originalUrl.startsWith("http")) {
                        originalUrl = "https:" + originalUrl;
                    }
                    String originalSize = original.attr("title").replaceAll("[^0-9]", "");
                    String[] resolution = original.text().replaceAll("\\([^)]*?\\)", "").trim().split("x");
                    builder.fileSize(originalSize)
                            .fileUrl(originalUrl)
                            .jpegUrl(originalUrl)
                            .jpegWidth(resolution[0])
                            .jpegHeight(resolution[1])
                            .jpegFileSize(originalSize)
                            .width(resolution[0])
                            .height(resolution[1]);
                } else if (text.contains("Rating:")) {
                    // 评级
                    if (li.children().isEmpty()) {
                        String rating = li.text();
                        if (rating.contains("Safe")) {
                            builder.rating("s");
                        } else if (rating.contains("Explicit")) {
                            builder.rating("e");
                        } else if (rating.contains("Questionable")) {
                            builder.rating("q");
                        }
                    }
                }

                // tags
                Element tag = doc.getElementById("tag-sidebar");
                if (tag != null) {
                    for (Element copyright : tag.getElementsByClass("tag-type-copyright")) {
                        builder.addCopyrightTags(copyright.child(0).text().replace(" ", "_"));
                    }
                    for (Element character : tag.getElementsByClass("tag-type-character")) {
                        builder.addCharacterTags(character.child(0).text().replace(" ", "_"));
                    }
                    for (Element artist : tag.getElementsByClass("tag-type-artist")) {
                        builder.addArtistTags(artist.child(0).text().replace(" ", "_"));
                    }
                    for (Element style : tag.getElementsByClass("tag-type-medium")) {
                        builder.addStyleTags(style.child(0).text().replace(" ", "_"));
                    }
                    for (Element general : tag.getElementsByClass("tag-type-meta")) {
                        builder.addGeneralTags(general.child(0).text().replace(" ", "_"));
                    }
                    for (Element general : tag.getElementsByClass("tag-type-general")) {
                        builder.addGeneralTags(general.child(0).text().replace(" ", "_"));
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
        Element body = doc.getElementsByTag("tbody").first();
        if (body != null) {
            for (Element pool : body.getElementsByTag("tr")) {
                try {
                    PoolListBean poolListBean = new PoolListBean();
                    Elements tds = pool.getElementsByTag("td");
                    Element link = tds.first().getElementsByTag("a").first();
                    String href = link.attr("href");
                    poolListBean.id = href.substring(href.lastIndexOf("/") + 1);
                    poolListBean.name = link.text();
                    poolListBean.linkToShow = mWebsiteConfig.getBaseUrl() + href;
                    poolListBean.creator = tds.get(1).text();
                    poolListBean.postCount = tds.get(2).text();
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
}
