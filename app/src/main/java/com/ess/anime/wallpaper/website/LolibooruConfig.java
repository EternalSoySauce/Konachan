package com.ess.anime.wallpaper.website;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.website.parser.GeneralParser;
import com.ess.anime.wallpaper.website.search.GeneralAutoCompleteParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LolibooruConfig extends WebsiteConfig<GeneralParser> {

    @Override
    public String getWebsiteName() {
        return "Lolibooru";
    }

    @Override
    public int getWebsiteLogoRes() {
        return R.drawable.ic_website_lolibooru;
    }

    @Override
    public String getBaseUrl() {
        return BASE_URL_LOLIBOORU;
    }

    @Override
    public boolean hasTagJson() {
        return true;
    }

    @Override
    public String getTagJsonUrl() {
        return TAG_JSON_URL_LOLIBOORU;
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

        StringBuilder tags = new StringBuilder();
        for (String tag : tagList) {
            tags.append(tag).append("+");
        }

        return getBaseUrl() + "post?page=" + page + "&tags=" + tags;
    }

    @Override
    public String getPopularDailyUrl(int year, int month, int day, int page) {
        return getBaseUrl() + "post/popular_by_day?day=" + day + "&month=" + month + "&year=" + year;
    }

    @Override
    public String getPopularWeeklyUrl(int year, int month, int day, int page) {
        return getBaseUrl() + "post/popular_by_week?day=" + day + "&month=" + month + "&year=" + year;
    }

    @Override
    public String getPopularMonthlyUrl(int year, int month, int day, int page) {
        return getBaseUrl() + "post/popular_by_month?month=" + month + "&year=" + year;
    }

    @Override
    public String getPopularOverallUrl(int year, int month, int day, int page) {
        return getPostUrl(page, Collections.singletonList("order:score"));
    }

    @Override
    public String getCommentUrl(String id) {
        return getPostDetailUrl(id);
    }

    @Override
    public String getPostDetailUrl(String id) {
        return getBaseUrl() + "post/show/" + id;
    }

    @Override
    public boolean hasPool() {
        return true;
    }

    @Override
    public String getPoolUrl(int page, String name) {
        name = name == null ? "" : name;
        return getBaseUrl() + "pool?commit=Search&page=" + page + "&query=" + name;
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
        return "Lolibooru-";
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
