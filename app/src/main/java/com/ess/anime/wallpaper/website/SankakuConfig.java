package com.ess.anime.wallpaper.website;

import android.text.TextUtils;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.website.parser.SankakuParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

public class SankakuConfig extends WebsiteConfig<SankakuParser> {

    @Override
    public String getWebsiteName() {
        return "Sankaku";
    }

    @Override
    public int getWebsiteLogoRes() {
        return R.drawable.ic_website_sankaku;
    }

    @Override
    public String getBaseUrl() {
        return BASE_URL_SANKAKU;
    }

    @Override
    public boolean hasTagJson() {
        return false;
    }

    @Override
    public String getTagJsonUrl() {
        return null;
    }

    @Override
    public void saveTagJson(String key, String json) {
    }

    @Override
    public String getTagJson() {
        return null;
    }

    @Override
    public List<String> parseSearchAutoCompleteListFromTagJson(String search) {
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

        String url = getBaseUrl() + "post?page=" + page;
        if (tags.length() > 0) {
            url += "&tags=" + tags;
        }
        return url;
    }

    @Override
    public boolean hasPool() {
        return true;
    }

    @Override
    public String getPoolUrl(int page, String name) {
        name = name == null ? "" : name;
        String url = getBaseUrl() + "pool/index?page=" + page;
        if (!TextUtils.isEmpty(name)) {
            url += "&query=" + name;
        }
        return url;
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

    @Override
    public String getSearchAutoCompleteUrl(String tag) {
        return getBaseUrl() + "tag/autosuggest?tag=" + tag;
    }

    @Override
    public List<String> parseSearchAutoCompleteListFromNetwork(String promptResult, String search) {
        List<String> list = new ArrayList<>();
        try {
            JsonArray tagArray = new JsonParser().parse(promptResult).getAsJsonArray().get(1).getAsJsonArray();
            for (int i = 0; i < tagArray.size(); i++) {
                list.add(tagArray.get(i).getAsString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

}
