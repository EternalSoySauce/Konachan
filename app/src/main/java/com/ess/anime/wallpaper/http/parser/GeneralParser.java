package com.ess.anime.wallpaper.http.parser;

import android.content.Context;
import android.text.Html;

import com.ess.anime.wallpaper.bean.CommentBean;
import com.ess.anime.wallpaper.bean.PoolListBean;
import com.ess.anime.wallpaper.bean.ThumbBean;
import com.ess.anime.wallpaper.http.OkHttp;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Konachan,Yande,Lolibooru通用
 */
public class GeneralParser extends HtmlParser {

    GeneralParser(Context context, Document doc) {
        super(context, doc);
    }

    @Override
    public List<ThumbBean> getThumbList() {
        List<ThumbBean> thumbList = new ArrayList<>();
        Element list = mDoc.getElementById("post-list-posts");
        if (list == null) {
            return thumbList;
        }

        Elements elements = list.getElementsByTag("li");
        for (Element e : elements) {
            try {
                String id = e.attr("id").replaceAll("[^0-9]", "");
                Element img = e.getElementsByTag("img").first();
                int thumbWidth = Integer.valueOf(img.attr("width")) * 2;
                int thumbHeight = Integer.valueOf(img.attr("height")) * 2;
                String thumbUrl = img.attr("src");
                if (thumbUrl.contains("deleted-preview")) {
                    continue;
                } else if (!thumbUrl.startsWith("http")) {
                    thumbUrl = "https:" + thumbUrl;
                }
                String realSize = "";
                Elements directLink = e.getElementsByClass("directlink-res");
                if (!directLink.isEmpty()) {
                    realSize = directLink.first().ownText();
                }
                String linkToShow = e.getElementsByClass("plid").first().ownText();
                linkToShow = linkToShow.substring(linkToShow.indexOf("http"));
                thumbList.add(new ThumbBean(id, thumbWidth, thumbHeight, thumbUrl, realSize, linkToShow));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return thumbList;
    }

    @Override
    public String getImageDetailJson() {
        try {
            Element div = mDoc.getElementById("post-view");
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

    @Override
    public List<CommentBean> getCommentList() {
        List<CommentBean> commentList = new ArrayList<>();
        Elements elements = mDoc.getElementsByClass("comment avatar-container");
        for (Element e : elements) {
            try {
                String id = "#" + e.attr("id");
                String author = e.getElementsByTag("h6").first().text();
                String date = e.getElementsByClass("date").first().attr("title");
                String avatar = "";
                Elements avatars = e.getElementsByClass("avatar");
                if (!avatars.isEmpty()) {
                    avatar = avatars.first().attr("src");
                    if (!avatar.startsWith("http")) {
                        avatar = avatar.startsWith("//") ? "https:" + avatar
                                : OkHttp.getBaseUrl(mContext) + avatar;
                    }
                }
                Element body = e.getElementsByClass("body").first();
                Elements blockquote = body.select("blockquote");
                CharSequence quote = Html.fromHtml(blockquote.select("div").html());
                blockquote.remove();
                CharSequence comment = Html.fromHtml(body.html());
                commentList.add(new CommentBean(id, author, date, avatar, quote, comment));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return commentList;
    }

    @Override
    public List<PoolListBean> getPoolListList() {
        //解析预览图和id
        List<PoolListBean> poolList = new ArrayList<>();
        PoolListBean poolListBean = new PoolListBean();
        Elements eleScripts = mDoc.getElementsByTag("script");
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
            Element poolDetail = mDoc.getElementById(pool.id);
            Elements tds = poolDetail.getElementsByTag("td");
            for (int index = 0; index < tds.size(); index++) {
                Element td = tds.get(index);
                switch (index) {
                    case 0:
                        String linkToShow = td.getElementsByTag("a").first().attr("href");
                        linkToShow = OkHttp.getBaseUrl(mContext) + linkToShow.substring(1);
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

}
