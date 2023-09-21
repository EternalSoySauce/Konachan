package com.ess.anime.wallpaper.website;

import android.text.TextUtils;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.website.parser.WallhallaParser;

import java.util.ArrayList;
import java.util.List;

public class WallhallaConfig extends WebsiteConfig<WallhallaParser> {

    @Override
    public String getWebsiteName() {
        return "Wallhalla";
    }

    @Override
    public int getWebsiteLogoRes() {
        return R.drawable.ic_website_wallhalla;
    }

    @Override
    public String getBaseUrl() {
        return BASE_URL_WALLHALLA;
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

        if (tagList.isEmpty()) {
            return getBaseUrl() + "new?page=" + page;
        } else {
            boolean isRandom = false;
            StringBuilder tags = new StringBuilder();
            for (int i = 0; i < tagList.size(); i++) {
                String tag = tagList.get(i);
                if (TextUtils.equals(tag, "order:random")) {
                    isRandom = true;
                    break;
                } else {
                    tag = tag.replaceAll("_", " ");
                    tags.append("\"").append(tag).append("\"");
                }
                if (i < tagList.size() - 1) {
                    tags.append("+");
                }
            }

            if (isRandom) {
                return getBaseUrl() + "random";
            } else {
                return getBaseUrl() + "search?q=" + tags + "&page=" + page;
            }
        }
    }

    @Override
    public String getPopularDailyUrl(int year, int month, int day, int page) {
        return null;
    }

    @Override
    public String getPopularWeeklyUrl(int year, int month, int day, int page) {
        return null;
    }

    @Override
    public String getPopularMonthlyUrl(int year, int month, int day, int page) {
        return null;
    }

    @Override
    public String getPopularOverallUrl(int year, int month, int day, int page) {
        return getBaseUrl() + "toplist?page=" + page;
    }

    @Override
    public String getPostDetailUrl(String id) {
        return getBaseUrl() + "wallpaper/" + id;
    }

    @Override
    public String getCommentUrl(String id) {
        return null;
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
        return "Wallhalla-";
    }

    @Override
    public boolean isSupportRandomPost() {
        return true;
    }

    @Override
    public boolean isSupportAdvancedSearch() {
        return false;
    }

    @Override
    public boolean isSupportSearchAutoCompleteFromNetwork() {
        return false;
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
