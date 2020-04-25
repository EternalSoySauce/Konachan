package com.ess.anime.wallpaper.website;

import com.ess.anime.wallpaper.http.parser.DanbooruParser;

import java.util.ArrayList;
import java.util.List;

public class DanbooruConfig extends WebsiteConfig<DanbooruParser> {

    @Override
    public String getBaseUrl() {
        return BASE_URL_DANBOORU;
    }

    @Override
    public String getTagJsonUrl() {
        return null;
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

}
