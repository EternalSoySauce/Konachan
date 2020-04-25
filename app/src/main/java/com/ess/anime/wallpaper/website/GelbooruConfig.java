package com.ess.anime.wallpaper.website;

import com.ess.anime.wallpaper.http.parser.GelbooruParser;

import java.util.ArrayList;
import java.util.List;

public class GelbooruConfig extends WebsiteConfig<GelbooruParser> {

    @Override
    public String getBaseUrl() {
        return BASE_URL_GELBOORU;
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

        return getBaseUrl() + "index.php?page=dapi&s=post&q=index&pid=" + (page - 1) + "&tags=" + tags + "&limit=42";
    }

    @Override
    public boolean hasPool() {
        return true;
    }

    @Override
    public String getPoolUrl(int page, String name) {
        return getBaseUrl() + "index.php?page=pool&s=list&pid=" + (page - 1) * 25;
    }

    @Override
    public String getPoolPostUrl(String linkToShow, int page) {
        return linkToShow;
    }

    @Override
    public String getSavedImageHead() {
        return "Gelbooru-";
    }

    @Override
    public boolean isSupportRandomPost() {
        return false;
    }

    @Override
    public boolean isSupportAdvancedSearch() {
        return false;
    }

}
