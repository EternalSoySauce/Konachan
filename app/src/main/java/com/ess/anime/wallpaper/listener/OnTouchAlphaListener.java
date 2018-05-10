package com.ess.anime.wallpaper.listener;

import android.view.MotionEvent;
import android.view.View;

public class OnTouchAlphaListener implements View.OnTouchListener {

    private float mAlphaDown;
    private float mAlphaUp;

    public OnTouchAlphaListener(float alphaDown, float alphaUp) {
        mAlphaDown = alphaDown;
        mAlphaUp = alphaUp;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                v.setAlpha(mAlphaDown);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                v.setAlpha(mAlphaUp);
                break;
        }
        return false;
    }
}
