package com.ess.anime.wallpaper.http.parser;

import android.content.Context;

import com.ess.anime.wallpaper.bean.CommentBean;
import com.ess.anime.wallpaper.bean.PoolListBean;
import com.ess.anime.wallpaper.bean.ThumbBean;
import com.ess.anime.wallpaper.global.Constants;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class GelbooruParser extends HtmlParser {

    GelbooruParser(Context context, Document doc) {
        super(context, doc);
    }

    @Override
    public ArrayList<ThumbBean> getThumbList() {
        ArrayList<ThumbBean> thumbList = new ArrayList<>();
        Elements elements = mDoc.getElementsByClass("thumbnail-preview");
        for (Element e : elements) {
            try {
                Element a = e.getElementsByTag("a").first();
                String id = a.id().replaceAll("[^0-9]", "");
                String linkToShow = a.attr("href");
                if (linkToShow.startsWith("//")) {
                    linkToShow = "https:" + linkToShow;
                } else if (!linkToShow.startsWith("http")) {
                    linkToShow = Constants.BASE_URL_GELBOORU + linkToShow;
                }
                Element img = e.getElementsByTag("img").first();
                String thumbUrl = img.attr("src");
                if (!thumbUrl.startsWith("http")) {
                    thumbUrl = "https:" + thumbUrl;
                }
                thumbList.add(new ThumbBean(id, thumbUrl, "❤", linkToShow));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return thumbList;
    }

    @Override
    public String getImageDetailJson() {
        return "";
    }

    @Override
    public ArrayList<CommentBean> getCommentList() {
        ArrayList<CommentBean> commentList = new ArrayList<>();
        return commentList;
    }

    @Override
    public ArrayList<PoolListBean> getPoolList() {
        ArrayList<PoolListBean> poolList = new ArrayList<>();
        return poolList;
    }
}
