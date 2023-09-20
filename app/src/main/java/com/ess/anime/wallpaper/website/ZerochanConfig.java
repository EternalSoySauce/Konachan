package com.ess.anime.wallpaper.website;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.website.parser.ZerochanParser;

import java.util.ArrayList;
import java.util.List;

public class ZerochanConfig extends WebsiteConfig<ZerochanParser> {

    @Override
    public String getWebsiteName() {
        return "Zerochan";
    }

    @Override
    public int getWebsiteLogoRes() {
        return R.drawable.ic_website_zerochan;
    }

    @Override
    public String getBaseUrl() {
        return BASE_URL_ZEROCHAN;
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
        for (int i = 0; i < tagList.size(); i++) {
            String tag = tagList.get(i);
            if (tag.startsWith("id:")) {
                tag = tag.replaceFirst("id:", "");
            }
            tag = tag.replaceAll("_", "+");
            tags.append(tag);
            if (i < tagList.size() - 1) {
                tags.append(",");
            }
        }

        return getBaseUrl() + tags + "?p=" + page + "&l=50&s=id&json";
    }

    @Override
    public String getPopularDailyUrl(int year, int month, int day, int page) {
        return getBaseUrl() + "popular?json";
    }

    @Override
    public String getPopularWeeklyUrl(int year, int month, int day, int page) {
        return getBaseUrl() + "?p=" + page + "&l=50&json&s=fav&t=1";
    }

    @Override
    public String getPopularMonthlyUrl(int year, int month, int day, int page) {
        return getBaseUrl() + "?p=" + page + "&l=50&json&s=fav&t=2";
    }

    @Override
    public String getPopularOverallUrl(int year, int month, int day, int page) {
        return getBaseUrl() + "?p=" + page + "&l=50&json&s=fav&t=0";
    }

    @Override
    public String getPostDetailUrl(String id) {
        return getBaseUrl() + id;
    }

    @Override
    public String getCommentUrl(String id) {
        return getPostDetailUrl(id);
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

    @Override
    public String getSearchAutoCompleteUrl(String tag) {
        return getBaseUrl() + "suggest?q=" + tag;
    }

    @Override
    public List<String> parseSearchAutoCompleteListFromNetwork(String promptResult, String search) {
        List<String> list = new ArrayList<>();
        try {
            String[] items = promptResult.split("\n");
            for (String item : items) {
                String tag = item.split("\\|")[0];
                list.add(tag);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

}
