package com.ess.anime.wallpaper.website;

import android.text.TextUtils;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.website.parser.DanbooruParser;
import com.ess.anime.wallpaper.website.search.GeneralAutoCompleteParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class DanbooruConfig extends WebsiteConfig<DanbooruParser> {

    @Override
    public String getWebsiteName() {
        return "Danbooru";
    }

    @Override
    public int getWebsiteLogoRes() {
        return R.drawable.ic_website_danbooru;
    }

    @Override
    public String getBaseUrl() {
        return BASE_URL_DANBOORU;
    }

    @Override
    public boolean hasTagJson() {
        return true;
    }

    @Override
    public String getTagJsonUrl() {
        // Danbooru没有搜索提示，借用Konachan(r18)的
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

        StringBuilder tags = new StringBuilder();
        for (String tag : tagList) {
            if (TextUtils.equals(tag, "order:random")) {
                tags.setLength(0);
                tags.append("random:20");
                page = 1;
                break;
            } else {
                tags.append(tag).append("+");
            }
        }

        return getBaseUrl() + "posts.xml?page=" + page + "&tags=" + tags;
    }

    @Override
    public String getPopularDailyUrl(int year, int month, int day, int page) {
        String date = String.format(Locale.ENGLISH, "%d-%d-%d", year, month, day);
        return getBaseUrl() + "explore/posts/popular.xml?date=" + date + "&page=" + page + "&scale=day";
    }

    @Override
    public String getPopularWeeklyUrl(int year, int month, int day, int page) {
        String date = String.format(Locale.ENGLISH, "%d-%d-%d", year, month, day);
        return getBaseUrl() + "explore/posts/popular.xml?date=" + date + "&page=" + page + "&scale=week";
    }

    @Override
    public String getPopularMonthlyUrl(int year, int month, int day, int page) {
        String date = String.format(Locale.ENGLISH, "%d-%d-%d", year, month, day);
        return getBaseUrl() + "explore/posts/popular.xml?date=" + date + "&page=" + page + "&scale=month";
    }

    @Override
    public String getPopularOverallUrl(int year, int month, int day, int page) {
        return getPostUrl(page, Collections.singletonList("order:rank"));
    }

    @Override
    public String getPostDetailUrl(String id) {
        return getBaseUrl() + "posts/" + id;
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
        name = name == null ? "" : name;
        return getBaseUrl() + "pools/gallery.xml?commit=Search&limit=20&page=" + page + "&search[name_matches]=" + name + "&search[order]=&utf8=%E2%9C%93";
    }

    @Override
    public String getPoolPostUrl(String linkToShow, int page) {
        return linkToShow.replaceFirst("page=\\d", "page=" + page);
    }

    @Override
    public boolean needReloadDetailByIdForPoolPost() {
        return false;
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
