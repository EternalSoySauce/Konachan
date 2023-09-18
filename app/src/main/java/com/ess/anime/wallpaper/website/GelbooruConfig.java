package com.ess.anime.wallpaper.website;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.website.parser.GelbooruParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

public class GelbooruConfig extends WebsiteConfig<GelbooruParser> {

    @Override
    public String getWebsiteName() {
        return "Gelbooru";
    }

    @Override
    public int getWebsiteLogoRes() {
        return R.drawable.ic_website_gelbooru;
    }

    @Override
    public String getBaseUrl() {
        return BASE_URL_GELBOORU;
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

        return getBaseUrl() + "index.php?page=dapi&s=post&q=index&pid=" + (page - 1) + "&tags=" + tags + "&limit=42";
    }

    @Override
    public String getPostDetailUrl(String id) {
        return getBaseUrl() + "index.php?page=post&s=view&id=" + id;
    }

    @Override
    public String getCommentUrl(String id) {
        return getPostDetailUrl(id);
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
    public boolean needReloadDetailByIdForPoolPost() {
        return true;
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

    @Override
    public String getSearchAutoCompleteUrl(String tag) {
        return getBaseUrl() + "index.php?page=autocomplete2&term=" + tag;
    }

    @Override
    public List<String> parseSearchAutoCompleteListFromNetwork(String promptResult, String search) {
        List<String> list = new ArrayList<>();
        try {
            JsonArray tagArray = new JsonParser().parse(promptResult).getAsJsonArray();
            for (int i = 0; i < tagArray.size(); i++) {
                JsonObject item = tagArray.get(i).getAsJsonObject();
                list.add(item.get("value").getAsString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

}
