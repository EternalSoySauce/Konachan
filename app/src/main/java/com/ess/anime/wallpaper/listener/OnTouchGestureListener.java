package com.ess.anime.wallpaper.listener;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class OnTouchGestureListener implements View.OnTouchListener {

    private GestureDetector mGestureDetector;

    public OnTouchGestureListener(Context context, GestureDetector.SimpleOnGestureListener gestureListener) {
        mGestureDetector = new GestureDetector(context,gestureListener);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

}
