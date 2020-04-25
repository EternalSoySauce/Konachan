package com.ess.anime.wallpaper.website;

import com.ess.anime.wallpaper.http.parser.SankakuParser;

import java.util.ArrayList;
import java.util.List;

public class SankakuConfig extends WebsiteConfig<SankakuParser> {

    @Override
    public String getBaseUrl() {
        return BASE_URL_SANKAKU;
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
        return getBaseUrl() + "pool/index?page=" + page + "&query=" + name;
    }

    @Override
    public String getPoolPostUrl(String linkToShow, int page) {
        return linkToShow + "?page=" + page;
    }

    @Override
    public String getSavedImageHead() {
        return "Sankaku-";
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
