package com.ess.anime.wallpaper.listener;

import android.annotation.SuppressLint;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class FlingEffector {

    @SuppressLint("ClickableViewAccessibility")
    public static void addFlingEffect(View view, OnFlingListener listener) {
        GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (listener != null) {
                    listener.onFling(e1, e2, velocityX, velocityY);
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        };
        GestureDetector gestureDetector = new GestureDetector(view.getContext(), gestureListener);
        view.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
    }

    public interface OnFlingListener {
        void onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY);
    }

}
