package com.lss.anime.wallpaper.view;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;

import com.lss.anime.wallpaper.R;

public class MySwipeRefreshLayout extends SwipeRefreshLayout {

    public MySwipeRefreshLayout(Context context) {
        this(context, null);
    }

    public MySwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initColor();
    }

    private void initColor() {
        setColorSchemeResources(R.color.color_text_unselected);
        setProgressBackgroundColorSchemeResource(R.color.colorPrimary);
    }
}
