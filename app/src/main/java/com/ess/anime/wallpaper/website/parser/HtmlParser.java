package com.ess.anime.wallpaper.website.parser;

import android.text.TextUtils;

import com.ess.anime.wallpaper.bean.CommentBean;
import com.ess.anime.wallpaper.bean.PoolListBean;
import com.ess.anime.wallpaper.bean.ThumbBean;
import com.ess.anime.wallpaper.utils.StringUtils;
import com.ess.anime.wallpaper.website.WebsiteConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class HtmlParser {

    WebsiteConfig mWebsiteConfig;

    public HtmlParser(WebsiteConfig websiteConfig) {
        mWebsiteConfig = websiteConfig;
    }

    public abstract List<ThumbBean> getThumbList(Document doc);

    public abstract String getImageDetailJson(Document doc);

    public abstract List<CommentBean> getCommentList(Document doc);

    public abstract List<PoolListBean> getPoolListList(Document doc);

    public List<ThumbBean> getThumbListOfPool(Document doc) {
        List<ThumbBean> thumbList = getThumbList(doc);
        Elements scripts = doc.getElementsByTag("script");
        for (Element script : scripts) {
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
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return thumbList;
    }

    public static String checkBaiduAlternateUrl(String originalUrl, String html) {
        try {
            Document doc = Jsoup.parse(html);
            if (!doc.getElementsByClass("lemmaWgt-subLemmaListTitle").isEmpty()) {
                Element alternate = doc.getElementsByClass("list-dot list-dot-paddingleft").first();
                if (alternate != null) {
                    Element a = alternate.getElementsByTag("a").first();
                    if (a != null && a.hasAttr("data-lemmaid")) {
                        String childPageId = a.attr("data-lemmaid");
                        return originalUrl + "/" + childPageId;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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

    protected String fitLineBreaksToHtml(String input) {
        return input.replaceAll("\r\n|\n\r|\n|\r", "<br>");
    }

    protected String fitHyperlinkToHtml(String input) {
        String regex = "\\b(https?|ftp)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]*[-A-Za-z0-9+&@#/%=~_|]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        StringBuffer htmlFormatted = new StringBuffer();
        while (matcher.find()) {
            String url = matcher.group();
            String linkHtml = "<a href='" + url + "'>" + url + "</a>";
            matcher.appendReplacement(htmlFormatted, linkHtml);
        }
        matcher.appendTail(htmlFormatted);
        return htmlFormatted.toString();
    }

}
