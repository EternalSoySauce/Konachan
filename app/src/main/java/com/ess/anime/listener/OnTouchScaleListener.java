package com.ess.anime.listener;

import android.view.MotionEvent;
import android.view.View;

public class OnTouchScaleListener implements View.OnTouchListener {

    private float mScale;

    public OnTouchScaleListener(float scale) {
        mScale = scale;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                v.setScaleX(mScale);
                v.setScaleY(mScale);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                v.setScaleX(1f);
                v.setScaleY(1f);
                break;
        }
        return false;
    }
}
