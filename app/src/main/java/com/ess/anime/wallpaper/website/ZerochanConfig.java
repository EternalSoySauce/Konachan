package com.ess.anime.wallpaper.website;

import com.ess.anime.wallpaper.http.parser.ZerochanParser;

import java.util.ArrayList;
import java.util.List;

class ZerochanConfig extends WebsiteConfig<ZerochanParser> {

    @Override
    public String getBaseUrl() {
        return BASE_URL_ZEROCHAN;
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
            tag = tag.replaceAll("_", " ");
            tags.append(tag).append("+");
        }

        return getBaseUrl() + tags + "?s=id&p=" + page;
    }

    @Override
    public boolean hasPool() {
        return false;
    }

    @Override
    public String getPoolUrl(int page, String name) {
        return null;
    }

    @Override
    public String getPoolPostUrl(String linkToShow, int page) {
        return null;
    }

    @Override
    public String getSavedImageHead() {
        return "Zerochan-";
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
