package com.ess.anime.wallpaper.ui.view.image;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SwitcherTitle extends MyImageSwitcher {

    public SwitcherTitle(@NonNull Context context) {
        super(context);
    }

    public SwitcherTitle(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SwitcherTitle(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    ImageView createImageView() {
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        ImageView imageView = new ImageView(getContext());
        imageView.setLayoutParams(params);
        return imageView;
    }

    @Override
    void loadImage(int resId, ImageView imageView) {
        imageView.setImageResource(resId);
    }

}
