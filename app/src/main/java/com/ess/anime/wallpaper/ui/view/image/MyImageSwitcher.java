package com.ess.anime.wallpaper.ui.view.image;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Priority;
import com.ess.anime.wallpaper.glide.GlideApp;
import com.ess.anime.wallpaper.utils.UIUtils;

public class MyImageSwitcher extends FrameLayout {

    private ImageView mIvA;
    private ImageView mIvB;

    private ScaleAnimation mScaleAnim1 = new ScaleAnimation(1, 0, 1, 1,
            Animation.RELATIVE_TO_PARENT, 0.5f, Animation.RELATIVE_TO_PARENT, 0.5f);
    private ScaleAnimation mScaleAnim2 = new ScaleAnimation(0, 1, 1, 1,
            Animation.RELATIVE_TO_PARENT, 0.5f, Animation.RELATIVE_TO_PARENT, 0.5f);

    public MyImageSwitcher(@NonNull Context context) {
        super(context);
    }

    public MyImageSwitcher(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyImageSwitcher(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        addView(mIvA = createImageView());
        addView(mIvB = createImageView());
        showImageA();
        initScaleAnim();
    }

    private ImageView createImageView() {
        int width = LayoutParams.MATCH_PARENT;
        int height = (int) (UIUtils.getScreenHeight(getContext()) * 0.6f);
        FrameLayout.LayoutParams params = new LayoutParams(width, height);
        ImageView imageView = new ImageView(getContext());
        imageView.setLayoutParams(params);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        return imageView;
    }

    private void showImageA() {
        mIvA.setAnimation(null);
        mIvB.setAnimation(null);
        mIvA.setVisibility(View.VISIBLE);
        mIvB.setVisibility(View.INVISIBLE);
    }

    private void showImageB() {
        mIvA.setAnimation(null);
        mIvB.setAnimation(null);
        mIvA.setVisibility(View.INVISIBLE);
        mIvB.setVisibility(View.VISIBLE);
    }

    private void initScaleAnim() {
        mScaleAnim1.setDuration(500);
        mScaleAnim2.setDuration(500);
        mScaleAnim1.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                if (mIvA.getVisibility() == View.VISIBLE) {
                    showImageB();
                    mIvB.startAnimation(mScaleAnim2);
                } else {
                    showImageA();
                    mIvA.startAnimation(mScaleAnim2);
                }
            }
        });
    }

    /**
     * 执行翻转动画
     */
    public void flipImage() {
        if (mIvA.getVisibility() == View.VISIBLE) {
            mIvA.startAnimation(mScaleAnim1);
        } else {
            mIvB.startAnimation(mScaleAnim1);
        }
    }

    /**
     * 设置两张图片加载的内容
     *
     * @param imgA 第一张内容
     * @param imgB 第二张内容
     */
    public void loadImage(Object imgA, Object imgB) {
        loadImage(imgA, mIvA);
        loadImage(imgB, mIvB);
    }

    private void loadImage(Object obj, ImageView imageView) {
        GlideApp.with(getContext())
                .load(obj)
                .priority(Priority.IMMEDIATE)
                .into(imageView);
    }
}
