package com.ess.anime.wallpaper.http.parser;

import android.content.Context;
import android.text.TextUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class HtmlParserFactory {

    public static HtmlParser createParser(Context context, String html) {
        Document doc = Jsoup.parse(html);
        String webTitle = doc.getElementsByTag("title").text();
        if (webTitle.toLowerCase().contains("danbooru")) {
            return new DanbooruParser(context, doc);
        } else if (webTitle.toLowerCase().contains("sankaku")) {
            return new SankakuParser(context, doc);
        } else if (webTitle.toLowerCase().contains("zerochan")) {
            return new ZerochanParser(context, doc);
        } else if (TextUtils.isEmpty(webTitle) || webTitle.toLowerCase().contains("gelbooru")) {
            // Gelbooru用特殊url获取的图片列表数据，html无webTitle
            return new GelbooruParser(context, doc);
        } else {
            return new GeneralParser(context, doc);
        }
    }

}
