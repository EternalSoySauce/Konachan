package com.ess.anime.wallpaper.ui.activity;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import butterknife.ButterKnife;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layoutRes());
        handleDisplayCutoutMode();
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        ButterKnife.bind(this);
        init(savedInstanceState);
    }

    abstract int layoutRes();

    abstract void init(Bundle savedInstanceState);

    // 官方支持，Android P及以上设备设置任何情况下都使用刘海部分
    private void handleDisplayCutoutMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Window window = getWindow();
            View decorView = window.getDecorView();
            if (ViewCompat.isAttachedToWindow(decorView)) {
                realHandleDisplayCutoutMode(window, decorView);
            } else {
                decorView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                    @Override
                    public void onViewAttachedToWindow(View v) {
                        v.removeOnAttachStateChangeListener(this);
                        realHandleDisplayCutoutMode(window, v);
                    }

                    @Override
                    public void onViewDetachedFromWindow(View v) {
                    }
                });
            }
        }
    }

    private void realHandleDisplayCutoutMode(Window window, View decorView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (decorView.getRootWindowInsets() != null &&
                    decorView.getRootWindowInsets().getDisplayCutout() != null) {
                WindowManager.LayoutParams params = window.getAttributes();
                params.layoutInDisplayCutoutMode = WindowManager.LayoutParams
                        .LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                window.setAttributes(params);
            }
        }
    }

}
