package com.ess.anime.wallpaper.listener;

import android.annotation.SuppressLint;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class DoubleTapEffector {

    @SuppressLint("ClickableViewAccessibility")
    public static void addDoubleTapEffect(View view, OnDoubleTapListener listener) {
        GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (listener != null) {
                    listener.onDoubleTap();
                }
                return super.onDoubleTap(e);
            }
        };
        GestureDetector gestureDetector = new GestureDetector(view.getContext(), gestureListener);
        view.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
    }

    public interface OnDoubleTapListener {
        void onDoubleTap();
    }

}
