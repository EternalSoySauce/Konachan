package com.ess.anime.http;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;

import com.ess.anime.bean.CommentBean;
import com.ess.anime.bean.PoolListBean;
import com.ess.anime.bean.ThumbBean;
import com.ess.anime.utils.StringUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class ParseHtml {

    // 无搜索结果时 getElementById() 会抛出空指针异常
    public static ArrayList<ThumbBean> getThumbList(String html) throws NullPointerException {
        ArrayList<ThumbBean> thumbList = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        Elements elements = doc.getElementById("post-list-posts").getElementsByTag("li");
        for (Element e : elements) {
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
        }
        return thumbList;
    }

    public static String getImageDetailJson(String html) {
        try {
            Document doc = Jsoup.parse(html);
            Element div = doc.getElementById("post-view");
            String json = div.getElementsByTag("script").get(0).html();
            json = json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1);
            json = json.replace("\\/", "/");
            if (!json.contains("http://konachan") && !json.contains("https://konachan")) {
                json = json.replace("//konachan", "https://konachan");
            }
            return json;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static ArrayList<CommentBean> getCommentList(Context context, String html) {
        ArrayList<CommentBean> commentList = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        Elements elements = doc.getElementsByClass("comment avatar-container");
        for (Element e : elements) {
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
            CharSequence comment = Html.fromHtml(body.html().replace(blockquote.outerHtml(), ""));
            commentList.add(new CommentBean(id, author, date, headUrl, quote, comment));
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
        //解析预览图和id
        ArrayList<PoolListBean> poolList = new ArrayList<>();
        PoolListBean poolListBean = new PoolListBean();
        Document doc = Jsoup.parse(html);
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

    public static ArrayList<ThumbBean> getThumbListOfPool(String html) {
        ArrayList<ThumbBean> thumbList = getThumbList(html);
        Document doc = Jsoup.parse(html);
        Elements eleScripts = doc.getElementsByTag("script");
        for (Element script : eleScripts) {
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
        }
        return thumbList;
    }
}
