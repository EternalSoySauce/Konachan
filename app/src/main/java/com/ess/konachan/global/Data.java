package com.ess.konachan.global;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.text.SpannedString;

import com.ess.konachan.R;
import com.ess.konachan.utils.FileUtils;
import com.ess.konachan.utils.HtmlFontSizeTagHandler;

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
    private final static String TXT_TAG_TYPE_DOC_CHINESE = "tag_type_doc_chinese.txt";
    private final static String TXT_TAG_TYPE_DOC_ENGLISH = "tag_type_doc_english.txt";
    private final static String TXT_ADVANCED_SEARCH_DOC_CHINESE = "advanced_search_doc_chinese.txt";
    private final static String TXT_ADVANCED_SEARCH_DOC_ENGLISH = "advanced_search_doc_english.txt";

    // 搜索界面显示“搜索模式说明”
    public static ArrayList<String> getSearchModeDocumentList(Context context) {
        ArrayList<String> docList = new ArrayList<>();
        String fileName = isChinese() ? TXT_SEARCH_MODE_CHINESE : TXT_SEARCH_MODE_ENGLISH;
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

    // 标签类型说明文档
    public static Spanned getTagTypeHelpDoc(Context context) {
        String fileName = isChinese() ? TXT_TAG_TYPE_DOC_CHINESE : TXT_TAG_TYPE_DOC_ENGLISH;
        InputStream is = null;
        try {
            is = context.getAssets().open(fileName);
            String html = FileUtils.streamToString(is);
            return Html.fromHtml(html);
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
        return new SpannedString(context.getString(R.string.dialog_doc_lost));
    }

    // 高级搜索说明文档
    public static Spanned getAdvancedSearchDoc(Context context) {
        String fileName = isChinese() ? TXT_ADVANCED_SEARCH_DOC_CHINESE : TXT_ADVANCED_SEARCH_DOC_ENGLISH;
        InputStream is = null;
        try {
            is = context.getAssets().open(fileName);
            String html = FileUtils.streamToString(is);
            return Html.fromHtml(html, null, new HtmlFontSizeTagHandler(context));
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
        return new SpannedString(context.getString(R.string.dialog_doc_lost));
    }

    // 判断当前语种是否为汉语
    private static boolean isChinese() {
        return "zh".equalsIgnoreCase(Locale.getDefault().getLanguage());
    }
}
