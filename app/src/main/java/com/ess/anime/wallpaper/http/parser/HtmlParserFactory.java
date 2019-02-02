package com.ess.anime.wallpaper.http.parser;

import android.content.Context;

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
        } else if (webTitle.toLowerCase().contains("gelbooru")) {
            return new GelbooruParser(context, doc);
        } else {
            return new GeneralParser(context, doc);
        }
    }

}
