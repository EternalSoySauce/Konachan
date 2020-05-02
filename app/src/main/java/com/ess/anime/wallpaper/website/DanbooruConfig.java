package com.ess.anime.wallpaper.website;

import com.ess.anime.wallpaper.website.parser.DanbooruParser;
import com.ess.anime.wallpaper.website.search.GeneralAutoCompleteParser;

import java.util.ArrayList;
import java.util.List;

public class DanbooruConfig extends WebsiteConfig<DanbooruParser> {

    @Override
    public String getWebsiteName() {
        return "Danbooru";
    }

    @Override
    public String getBaseUrl() {
        return BASE_URL_DANBOORU;
    }

    @Override
    public boolean hasTagJson() {
        return true;
    }

    @Override
    public String getTagJsonUrl() {
        // Danbooru没有搜索提示，借用Konachan(r18)的
        return TAG_JSON_URL_KONACHAN_E;
    }

    @Override
    public void saveTagJson(String json) {
       super.saveTagJson(json);
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
    public boolean hasPool() {
        return true;
    }

    @Override
    public String getPoolUrl(int page, String name) {
        name = name == null ? "" : name;
        return getBaseUrl() + "pools/gallery?commit=Search&limit=20&page=" + page + "&search[name_matches]=" + name + "&search[order]=&utf8=%E2%9C%93";
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
        return "Danbooru-";
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
        return null;
    }

    @Override
    public List<String> parseSearchAutoCompleteListFromNetwork(String promptResult, String search) {
        return null;
    }

}
