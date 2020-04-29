package com.ess.anime.wallpaper.website;

import android.text.TextUtils;

import com.ess.anime.wallpaper.bean.SearchBean;
import com.ess.anime.wallpaper.http.parser.HtmlParser;
import com.ess.anime.wallpaper.utils.FileUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
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

    // 是否有明确的搜索提示Json文件
    public abstract boolean hasTagJson();

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

    // 本地json路径或网络请求地址
    public abstract String getSearchAutoCompletePath();

    // 根据返回内容解析下拉提示列表
    public abstract List<String> parseSearchAutoCompleteList(String promptResult, String search);

    // Konachan、Yande、Lolibooru网站通用解析TagJson逻辑
    List<String> generalParseLocalTagJson(String filePath, String search, List<SearchBean> searchList, List<String> promptList) {
        // 从Json中解析全部tag
        if (searchList == null) {
            searchList = new ArrayList<>();
            File file = new File(filePath);
            if (file.exists() && file.isFile()) {
                String json = FileUtils.fileToString(file);
                json = json == null ? "" : json;
                try {
                    String data = new JSONObject(json).getString("data");
                    String[] tags = data.split(" ");
                    for (String tag : tags) {
                        String[] details = tag.split("`");
                        if (details.length > 1) {
                            SearchBean searchBean = new SearchBean(details[0]);
                            searchBean.tagList.addAll(Arrays.asList(details).subList(1, details.length));
                            searchList.add(searchBean);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        // 筛选当前搜索内容的提示标签，最多10个
        // 去除"_" "-"等连字符
        if (promptList == null) {
            promptList = new ArrayList<>();
        }
        search = search.replaceAll("[_\\-]", "");
        if (!TextUtils.isEmpty(search)) {
            int length = search.length();
            for (int i = 0; i <= length; i++) {
                String start = search.substring(0, length - i).toLowerCase();
                String contain = search.substring(length - i).toLowerCase();
                filter(start, contain, searchList, promptList);
                if (promptList.size() >= 10) {
                    break;
                }
            }
        }
        return promptList;
    }

    // 层级筛选
    // 例：搜索fla，筛选顺序为
    // startWith("fla")
    // -> startWidth("fl"), contains("a")
    // -> startWith("f"), contains("la")
    // -> contains("fla")
    private void filter(String start, String contain, List<SearchBean> searchList, List<String> promptList) {
        for (SearchBean searchBean : searchList) {
            for (String tag : searchBean.tagList) {
                boolean find = false;
                String[] parts = tag.split("_");
                for (String part : parts) {
                    if (part.startsWith(start) && part.contains(contain)) {
                        if (!promptList.contains(tag)) {
                            promptList.add(tag);
                        }
                        find = true;
                        break;
                    }
                }
                if (find) {
                    break;
                }
            }
            if (promptList.size() >= 15) {
                break;
            }
        }
    }

}
