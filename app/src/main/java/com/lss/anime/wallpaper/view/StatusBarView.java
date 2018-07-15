package com.lss.anime.wallpaper.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.lss.anime.wallpaper.utils.UIUtils;

public class StatusBarView extends View {

    public StatusBarView(Context context) {
        super(context);
    }

    public StatusBarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public StatusBarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), UIUtils.getStatusBarHeight(getContext()));
    }
}
