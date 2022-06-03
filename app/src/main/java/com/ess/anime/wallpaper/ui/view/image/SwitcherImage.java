package com.ess.anime.wallpaper.ui.view.image;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;

import com.bumptech.glide.Priority;
import com.ess.anime.wallpaper.glide.GlideApp;
import com.ess.anime.wallpaper.utils.UIUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SwitcherImage extends MyImageSwitcher {

    public SwitcherImage(@NonNull Context context) {
        super(context);
    }

    public SwitcherImage(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SwitcherImage(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    ImageView createImageView() {
        Activity activity = UIUtils.getActivityFromView(this);
        int width = LayoutParams.WRAP_CONTENT;
        int height = (activity != null && UIUtils.isLandscape(activity))
                ? (int) (UIUtils.getScreenHeight(getContext()) * 0.85f)
                : (int) (UIUtils.getScreenHeight(getContext()) * 0.6f);
        LayoutParams params = new LayoutParams(width, height);
        params.gravity = Gravity.CENTER;
        ImageView imageView = new ImageView(getContext());
        imageView.setLayoutParams(params);
        imageView.setAdjustViewBounds(true);
        return imageView;
    }

    @Override
    void loadImage(int resId, ImageView imageView) {
        GlideApp.with(getContext())
                .load(resId)
                .priority(Priority.IMMEDIATE)
                .into(imageView);
    }

}
