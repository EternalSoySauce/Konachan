package com.ess.anime.wallpaper.website;

import com.ess.anime.wallpaper.MyApp;
import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.utils.FileUtils;
import com.ess.anime.wallpaper.website.parser.GeneralParser;
import com.ess.anime.wallpaper.website.search.GeneralAutoCompleteParser;

import java.util.ArrayList;
import java.util.List;

public class KonachanSConfig extends WebsiteConfig<GeneralParser> {

    @Override
    public String getWebsiteName() {
        return "KonachanS";
    }

    @Override
    public int getWebsiteLogoRes() {
        return R.drawable.ic_website_konachan_s;
    }

    @Override
    public String getBaseUrl() {
        return BASE_URL_KONACHAN_S;
    }

    @Override
    public boolean hasTagJson() {
        return true;
    }

    @Override
    public String getTagJsonUrl() {
        return TAG_JSON_URL_KONACHAN_S;
    }

    @Override
    public void saveTagJson(String key, String json) {
        super.saveTagJson(key, json);
    }

    @Override
    public String getTagJson() {
        return super.getTagJson();
    }

    @Override
    public List<String> parseSearchAutoCompleteListFromTagJson(String search) {
        return GeneralAutoCompleteParser.getSearchAutoCompleteListFromDB(getTagJson(), search);
    }

    @Override
    public String getPostUrl(int page, List<String> tagList) {
        if (tagList == null) {
            tagList = new ArrayList<>();
        }

        StringBuilder tags = new StringBuilder();
        for (String tag : tagList) {
            tags.append(tag).append("+");
        }

        return getBaseUrl() + "post?page=" + page + "&tags=" + tags;
    }

    @Override
    public String getPostDetailUrl(String id) {
        return getBaseUrl() + "/post/show/" + id;
    }

    @Override
    public boolean hasPool() {
        return true;
    }

    @Override
    public String getPoolUrl(int page, String name) {
        name = name == null ? "" : name;
        return getBaseUrl() + "pool?commit=Search&page=" + page + "&query=" + name;
    }

    @Override
    public String getPoolPostUrl(String linkToShow, int page) {
        return linkToShow + "?page=" + page;
    }

    @Override
    public boolean needReloadDetailByIdForPoolPost() {
        return false;
    }

    @Override
    public String getSavedImageHead() {
        return "Konachan-";
    }

    @Override
    public boolean isSupportRandomPost() {
        return true;
    }

    @Override
    public boolean isSupportAdvancedSearch() {
        return true;
    }

    @Override
    public String getSearchAutoCompleteUrl(String tag) {
        String dirPath = MyApp.getInstance().getFilesDir().getPath();
        String fileName = FileUtils.encodeMD5String(TAG_JSON_URL_KONACHAN_S);
        return dirPath + "/" + fileName;
    }

    @Override
    public List<String> parseSearchAutoCompleteListFromNetwork(String promptResult, String search) {
        return null;
    }

}
