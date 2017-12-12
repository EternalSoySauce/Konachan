package com.ess.anime.utils;

import android.content.Context;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;

import org.xml.sax.XMLReader;

import java.lang.reflect.Field;
import java.util.HashMap;

public class HtmlFontSizeTagHandler implements Html.TagHandler {
    private static final String TAG_FONT_SIZE = "font-size";

    private Context mContext;
    private int mStartIndex = 0;
    private int mStopIndex = 0;
    private final HashMap<String, String> mAttributes = new HashMap<>();

    public HtmlFontSizeTagHandler(Context context) {
        mContext = context;
    }

    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
        processAttributes(xmlReader);

        if (tag.equalsIgnoreCase(TAG_FONT_SIZE)) {
            if (opening) {
                startFont(output);
            } else {
                endFont(output);
            }
        }
    }

    private void startFont(Editable output) {
        mStartIndex = output.length();
    }

    private void endFont(Editable output) {
        mStopIndex = output.length();

        String size = mAttributes.get("size");
        if (!TextUtils.isEmpty(size)) {
            size = size.split("sp")[0];
            output.setSpan(new AbsoluteSizeSpan(UIUtils.sp2px(mContext, Integer.parseInt(size))),
                    mStartIndex, mStopIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private void processAttributes(final XMLReader xmlReader) {
        try {
            Field elementField = xmlReader.getClass().getDeclaredField("theNewElement");
            elementField.setAccessible(true);
            Object element = elementField.get(xmlReader);
            Field attsField = element.getClass().getDeclaredField("theAtts");
            attsField.setAccessible(true);
            Object atts = attsField.get(element);
            Field dataField = atts.getClass().getDeclaredField("data");
            dataField.setAccessible(true);
            String[] data = (String[]) dataField.get(atts);
            Field lengthField = atts.getClass().getDeclaredField("length");
            lengthField.setAccessible(true);
            int len = (Integer) lengthField.get(atts);

            for (int i = 0; i < len; i++) {
                mAttributes.put(data[i * 5 + 1], data[i * 5 + 4]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
