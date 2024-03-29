package com.ess.anime.wallpaper.website;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.utils.DateUtils;
import com.ess.anime.wallpaper.utils.TimeFormat;
import com.ess.anime.wallpaper.website.parser.SankakuParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Collections;
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

        return getBaseUrl() + "posts?page=" + page + "&tags=" + tags + "&limit=60";
    }

    @Override
    public String getPopularDailyUrl(int year, int month, int day, int page) {
        String format = "yyyy-MM-dd";
        String dateStart = TimeFormat.dateFormat(DateUtils.getToday(year, month, day).getTime(), format);
        String dateEnd = TimeFormat.dateFormat(DateUtils.getNextDay(year, month, day).getTime(), format);
        String dateTag = "date:" + dateStart + ".." + dateEnd;
        List<String> tagList = new ArrayList<>();
        tagList.add(dateTag);
        tagList.add("order:quality");
        return getPostUrl(page, tagList);
    }

    @Override
    public String getPopularWeeklyUrl(int year, int month, int day, int page) {
        String format = "yyyy-MM-dd";
        String dateStart = TimeFormat.dateFormat(DateUtils.getMondayOfWeek(year, month, day).getTime(), format);
        String dateEnd = TimeFormat.dateFormat(DateUtils.getNextDay(DateUtils.getSundayOfWeek(year, month, day)).getTime(), format);
        String dateTag = "date:" + dateStart + ".." + dateEnd;
        List<String> tagList = new ArrayList<>();
        tagList.add(dateTag);
        tagList.add("order:quality");
        return getPostUrl(page, tagList);
    }

    @Override
    public String getPopularMonthlyUrl(int year, int month, int day, int page) {
        String format = "yyyy-MM-dd";
        String dateStart = TimeFormat.dateFormat(DateUtils.getFirstDayOfMonth(year, month, day).getTime(), format);
        String dateEnd = TimeFormat.dateFormat(DateUtils.getNextDay(DateUtils.getLastDayOfMonth(year, month, day)).getTime(), format);
        String dateTag = "date:" + dateStart + ".." + dateEnd;
        List<String> tagList = new ArrayList<>();
        tagList.add(dateTag);
        tagList.add("order:quality");
        return getPostUrl(page, tagList);
    }

    @Override
    public String getPopularOverallUrl(int year, int month, int day, int page) {
        return getPostUrl(page, Collections.singletonList("order:quality"));
    }

    @Override
    public String getPostDetailUrl(String id) {
        return getBaseUrl() + "posts/" + id;
    }

    @Override
    public String getCommentUrl(String id) {
        return getBaseUrl() + "posts/" + id + "/comments";
    }

    @Override
    public boolean hasPool() {
        return true;
    }

    @Override
    public String getPoolUrl(int page, String name) {
        name = name == null ? "" : name;
        // todo 暂不知道Api如何搜索
        return getBaseUrl() + "pools?page=" + page;
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
    public boolean isSupportSearchAutoCompleteFromNetwork() {
        return true;
    }

    @Override
    public String getSearchAutoCompleteUrl(String tag) {
        return getBaseUrl() + "tags/autosuggest/v2?tag=" + tag;
    }

    @Override
    public List<String> parseSearchAutoCompleteListFromNetwork(String promptResult, String search) {
        List<String> list = new ArrayList<>();
        try {
            JsonArray tagArray = new JsonParser().parse(promptResult).getAsJsonArray();
            for (int i = 0; i < tagArray.size(); i++) {
                JsonObject item = tagArray.get(i).getAsJsonObject();
                list.add(item.get("tagName").getAsString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

}
