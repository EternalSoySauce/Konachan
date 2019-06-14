package com.ess.anime.wallpaper.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

public class PhotoViewPager extends ViewPager {

    public PhotoViewPager(@NonNull Context context) {
        super(context);
    }

    public PhotoViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            // fix PhotoView 和 ViewPager 组合时，用双指进行缩小抛出pointerIndex out of range异常
            return super.onInterceptTouchEvent(ev);
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

}
