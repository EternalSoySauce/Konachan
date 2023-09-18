package com.ess.anime.wallpaper.website;

import android.text.TextUtils;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.website.parser.WallhavenParser;
import com.ess.anime.wallpaper.website.search.GeneralAutoCompleteParser;

import java.util.ArrayList;
import java.util.List;

public class WallhavenConfig extends WebsiteConfig<WallhavenParser> {

    private static final String API_KEY = "JdpvgaGnr7rysgPdkuKUdpzSX0quGdRW";

    @Override
    public String getWebsiteName() {
        return "Wallhaven";
    }

    @Override
    public int getWebsiteLogoRes() {
        return R.drawable.ic_website_wallhaven;
    }

    @Override
    public String getBaseUrl() {
        return BASE_URL_WALLHAVEN;
    }

    @Override
    public boolean hasTagJson() {
        return true;
    }

    @Override
    public String getTagJsonUrl() {
        // Wallhaven没有搜索提示，借用Konachan(r18)的
        return TAG_JSON_URL_KONACHAN_E;
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

        boolean isRandom = false;
        StringBuilder tags = new StringBuilder();
        for (int i = 0; i < tagList.size(); i++) {
            String tag = tagList.get(i);
            if (TextUtils.equals(tag, "order:random")) {
                isRandom = true;
            } else {
                tag = tag.replaceAll("_", "+");
                tags.append(tag);
            }
            if (i < tagList.size() - 1) {
                tags.append(",");
            }
        }

        String sorting = isRandom ? "random" : "date_added";

        return getBaseUrl() + "api/v1/search?q=" + tags + "&sorting=" + sorting + "&page=" + page
                + "&categories=010&purity=111&ai_art_filter=0&apikey=" + API_KEY;
    }

    @Override
    public String getPostDetailUrl(String id) {
        return getBaseUrl() + "api/v1/w/" + id + "?apikey=" + WallhavenConfig.API_KEY;
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
    public boolean needReloadDetailByIdForPoolPost() {
        return false;
    }

    @Override
    public String getSavedImageHead() {
        return "Wallhaven-";
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
