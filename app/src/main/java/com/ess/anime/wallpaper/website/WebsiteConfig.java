package com.ess.anime.wallpaper.website;

import com.ess.anime.wallpaper.http.parser.HtmlParser;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.util.List;

public abstract class WebsiteConfig<T extends HtmlParser> {

    public final static String BASE_URL_BAIDU = "https://baike.baidu.com/item/";
    public final static String BASE_URL_KONACHAN_S = "https://konachan.net/";
    public final static String BASE_URL_KONACHAN_E = "https://konachan.com/";
    public final static String BASE_URL_YANDE = "https://yande.re/";
    public final static String BASE_URL_LOLIBOORU = "https://lolibooru.moe/";
    public final static String BASE_URL_DANBOORU = "https://danbooru.donmai.us/";
    public final static String BASE_URL_SANKAKU = "https://chan.sankakucomplex.com/";
    public final static String BASE_URL_GELBOORU = "https://gelbooru.com/";
    public final static String BASE_URL_ZEROCHAN = "https://www.zerochan.net/";

    public final static String TAG_JSON_URL_KONACHAN_S = "https://konachan.net/tag/summary.json";
    public final static String TAG_JSON_URL_KONACHAN_E = "https://konachan.com/tag/summary.json";
    public final static String TAG_JSON_URL_YANDE = "https://yande.re/tag/summary.json";
    public final static String TAG_JSON_URL_LOLIBOORU = "https://lolibooru.moe/tag/summary.json";

    public final static String[] BASE_URLS = {
            BASE_URL_KONACHAN_S, BASE_URL_KONACHAN_E, BASE_URL_YANDE, BASE_URL_LOLIBOORU,
            BASE_URL_DANBOORU, BASE_URL_SANKAKU, BASE_URL_GELBOORU, BASE_URL_ZEROCHAN
    };

    private T mHtmlParser;

    public WebsiteConfig() {
        try {
            ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
            Class<T> clazz = (Class<T>) type.getActualTypeArguments()[0];
            Constructor constructor = clazz.getConstructor(WebsiteConfig.class);
            mHtmlParser = (T) constructor.newInstance(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 爬虫解析器
    public HtmlParser getHtmlParser() {
        return mHtmlParser;
    }

    // 网站域名
    public abstract String getBaseUrl();

    // 搜索提示的Json文件地址
    public abstract String getTagJsonUrl();

    // 通过tags搜索图片
    public abstract String getPostUrl(int page, List<String> tagList);

    // 是否有图集列表
    public abstract boolean hasPool();

    // 搜索图集
    public abstract String getPoolUrl(int page, String name);

    // 搜索图集中的图片
    public abstract String getPoolPostUrl(String linkToShow, int page);

    // 保存图片名前缀
    public abstract String getSavedImageHead();

    // 是否支持随机看图
    public abstract boolean isSupportRandomPost();

    // 是否支持高级搜索
    public abstract boolean isSupportAdvancedSearch();

}
