package com.ess.anime.wallpaper.ui.view.image;

import android.content.Context;
import android.util.AttributeSet;
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
        int width = LayoutParams.MATCH_PARENT;
        int height = (int) (UIUtils.getScreenHeight(getContext()) * 0.6f);
        LayoutParams params = new LayoutParams(width, height);
        ImageView imageView = new ImageView(getContext());
        imageView.setLayoutParams(params);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
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
