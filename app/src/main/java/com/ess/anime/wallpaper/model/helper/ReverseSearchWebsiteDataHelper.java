package com.ess.anime.wallpaper.model.helper;

import android.content.Context;
import android.text.TextUtils;

import com.ess.anime.wallpaper.http.OkHttp;
import com.ess.anime.wallpaper.model.entity.ReverseSearchWebsiteItem;
import com.ess.anime.wallpaper.utils.FileUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReverseSearchWebsiteDataHelper {

    /********************  Parser  ********************/

    public static List<ReverseSearchWebsiteItem> getWebsiteItemList(Context context) {
        List<ReverseSearchWebsiteItem> itemList = new ArrayList<>();

        File localFile = getLocalFile(context);
        if (localFile.exists() && localFile.isFile()) {
            String json = FileUtils.fileToString(localFile);
            itemList.addAll(parseFromJson(json));
        }

        if (itemList.isEmpty()) {
            try (InputStream is = context.getAssets().open(LOCAL_JSON_FILE_NAME)) {
                String json = FileUtils.streamToString(is);
                itemList.addAll(parseFromJson(json));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return itemList;
    }

    private static List<ReverseSearchWebsiteItem> parseFromJson(String json) {
        List<ReverseSearchWebsiteItem> itemList = new ArrayList<>();
        try {
            if (!TextUtils.isEmpty(json)) {
                String language = getCurrentLanguageAbbreviation();
                JsonArray jsonArray = new JsonParser().parse(json).getAsJsonArray();
                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject item = jsonArray.get(i).getAsJsonObject();
                    String name = item.get("name").getAsString();
                    String desc = item.get("desc").getAsJsonObject().get(language).getAsString();
                    String help = item.get("help").getAsJsonObject().get(language).getAsString();
                    String url = item.get("url").getAsString();
                    itemList.add(new ReverseSearchWebsiteItem(name, desc, help, url));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return itemList;
    }

    private static String getCurrentLanguageAbbreviation() {
        String code = Locale.getDefault().getCountry();
        if (code.equals("CN")) {
            return "zh_cn";
        } else if (code.equals("TW") || code.equals("HK")) {
            return "zh_hk";
        } else {
            return "en_us";
        }
    }

    /********************  Download  ********************/

    private final static String SERVER_JSON_FILE_URL = "https://opentext.oss-cn-shenzhen.aliyuncs.com/apk/reverse_search_website";
    private final static String LOCAL_JSON_FILE_NAME = "reverse_search_website";

    public static void loadNewJsonFromServer(Context context) {
        OkHttp.connect(SERVER_JSON_FILE_URL, SERVER_JSON_FILE_URL, new OkHttp.OkHttpCallback() {
            @Override
            public void onFailure(int errorCode, String errorMessage) {
            }

            @Override
            public void onSuccessful(String json) {
                FileUtils.stringToFile(json, getLocalFile(context));
            }
        });
    }

    private static File getLocalFile(Context context) {
        return new File(context.getExternalFilesDir(null), LOCAL_JSON_FILE_NAME);
    }

}
