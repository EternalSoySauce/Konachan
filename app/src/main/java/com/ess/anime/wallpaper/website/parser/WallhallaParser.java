package com.ess.anime.wallpaper.website.parser;

import com.ess.anime.wallpaper.bean.CommentBean;
import com.ess.anime.wallpaper.bean.ImageBean;
import com.ess.anime.wallpaper.bean.PoolListBean;
import com.ess.anime.wallpaper.bean.ThumbBean;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.website.WebsiteConfig;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class WallhallaParser extends HtmlParser {

    public WallhallaParser(WebsiteConfig websiteConfig) {
        super(websiteConfig);
    }

    @Override
    public List<ThumbBean> getThumbList(Document doc) {
        List<ThumbBean> thumbList = new ArrayList<>();
        try {
            Elements elements = doc.getElementsByClass("wp-item");
            for (Element e : elements) {
                try {
                    String id = e.attr("href").replaceAll("[^0-9]", "");
                    Element img = e.getElementsByTag("img").first();
                    String thumbUrl = checkToWrapImageUrlHost(img.attr("src"));
                    // todo 该站暂时无法准确获取宽高数据，且目前所有图片都是这个尺寸，后续网站更新了再做修改
                    int thumbWidth = 888;
                    int thumbHeight = 500;
                    String realSize = "3840 x 2160";
                    String linkToShow = mWebsiteConfig.getPostDetailUrl(id);
                    ThumbBean thumbBean = new ThumbBean(id, thumbWidth, thumbHeight, thumbUrl, realSize, linkToShow);
                    thumbList.add(thumbBean);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
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
            // 解析时间字符串，格式：2 months, 3 weeks ago / 2 weeks, 2 days ago
            // 注意PostBean.createdTime单位为second
            Element leading5 = doc.getElementsByClass("leading-5").first();
            String date = leading5.child(1).text();
            Calendar calendar = Calendar.getInstance();
            for (String part : date.split(",")) {
                if (part.contains("year")) {
                    calendar.add(Calendar.YEAR, -Integer.parseInt(part.replaceAll("[^0-9]", "")));
                } else if (part.contains("month")) {
                    calendar.add(Calendar.MONTH, -Integer.parseInt(part.replaceAll("[^0-9]", "")));
                } else if (part.contains("week")) {
                    calendar.add(Calendar.WEEK_OF_YEAR, -Integer.parseInt(part.replaceAll("[^0-9]", "")));
                } else if (part.contains("day")) {
                    calendar.add(Calendar.DAY_OF_YEAR, -Integer.parseInt(part.replaceAll("[^0-9]", "")));
                }
            }
            String createdTime = String.valueOf(calendar.getTimeInMillis() / 1000);
            builder.createdTime(createdTime);

            // 解析上传者信息
            String author = leading5.child(0).text().trim();
            builder.creatorId(author).author(author);

            // 解析观看数
            Element flex = doc.getElementsByClass("flex flex-wrap text-base").first();
            String views = flex.child(1).children().last().text().trim();
            builder.score(views);

            // 解析安全等级
            String purity = flex.child(0).children().last().text().trim();
            String rating;
            switch (purity) {
                default:
                case "Safe":
                    rating = Constants.RATING_S;
                    break;
                case "Sketchy":
                    rating = Constants.RATING_Q;
                    break;
                case "NSFW":
                    rating = Constants.RATING_E;
                    break;
            }
            builder.rating(rating);

            // 解析图片信息
            String id = "", oriWidth = "", oriHeight = "";
            Elements scripts = doc.getElementsByTag("script");
            for (Element script : scripts) {
                String content = script.html();
                if (content.contains("WP_ID")) {
                    String[] parts = content.split("\n");
                    for (String part : parts) {
                        if (part.contains("WP_ID")) {
                            id = part.replaceAll("[^0-9]", "");
                        } else if (part.contains("WP_WIDTH")) {
                            oriWidth = part.replaceAll("[^0-9]", "");
                        } else if (part.contains("WP_HEIGHT")) {
                            oriHeight = part.replaceAll("[^0-9]", "");
                        }
                    }
                    break;
                }
            }
            String previewUrl = doc.getElementsByAttributeValue("property", "og:image").first().attr("content");
            String sampleUrl = checkToWrapImageUrlHost(doc.getElementsByAttributeValue("data-variant", "desktop/1280x720").first().getElementsByTag("a").attr("href"));
            String fileUrl = checkToWrapImageUrlHost(doc.getElementsByAttributeValue("data-variant", "desktop/3840x2160").first().getElementsByTag("a").attr("href"));
            builder.id(id)
                    .previewUrl(previewUrl)
                    .sampleUrl(sampleUrl)
                    .sampleWidth("1280")
                    .sampleHeight("720")
                    .sampleFileSize("-1")
                    .fileUrl(fileUrl)
                    .fileSize("-1")
                    .jpegUrl(fileUrl)
                    .jpegWidth("3840")
                    .jpegHeight("2160")
                    .jpegFileSize("-1")
                    .width(oriWidth)
                    .height(oriHeight);

            // 防止为null的参数
            builder.source("").md5("").parentId("");

            // 解析tags
            StringBuilder tags = new StringBuilder();
            Elements tagArray = doc.getElementsByAttributeValueContaining("href", "/search?q=");
            for (Element tagItem : tagArray) {
                String tagName = tagItem.text().trim().replace(" ", "_");
                tags.append(tagName).append(" ");
                builder.addGeneralTags(tagName);
            }
            builder.tags(tags.toString().trim());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return builder.build();
    }

    @Override
    public List<CommentBean> getCommentList(Document doc) {
        return null;
    }

    @Override
    public List<PoolListBean> getPoolListList(Document doc) {
        return null;
    }

    private String checkToWrapImageUrlHost(String url) {
        if (!url.startsWith("http")) {
            if (url.startsWith("/")) {
                url = url.substring(1);
            }
            url = mWebsiteConfig.getBaseUrl() + url;
        }
        return url;
    }

}
