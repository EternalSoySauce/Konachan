package com.ess.konachan.global;

import android.content.Context;
import android.text.Html;

import com.ess.konachan.utils.FileUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

public class Data {

    private final static String TXT_SEARCH_MODE_CHINESE = "search_mode_chinese.txt";
    private final static String TXT_SEARCH_MODE_ENGLISH = "search_mode_english.txt";

    public static ArrayList<String> getSearchModeDocumentList(Context context) {
        ArrayList<String> docList = new ArrayList<>();
        boolean isChinese = "zh".equalsIgnoreCase(Locale.getDefault().getLanguage());
        String fileName = isChinese ? TXT_SEARCH_MODE_CHINESE : TXT_SEARCH_MODE_ENGLISH;
        InputStream is = null;
        try {
            is = context.getAssets().open(fileName);
            String html = FileUtils.streamToString(is);
            Document document = Jsoup.parse(html);
            Elements modes = document.getElementsByTag("span");
            for (Element mode : modes) {
                docList.add(String.valueOf(Html.fromHtml(mode.html())));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return docList;
    }
}
