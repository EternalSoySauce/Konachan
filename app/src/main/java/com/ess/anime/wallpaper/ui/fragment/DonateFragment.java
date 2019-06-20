package com.ess.anime.wallpaper.ui.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.listener.OnTouchScaleListener;
import com.ess.anime.wallpaper.model.helper.DonateHelper;
import com.ess.anime.wallpaper.ui.view.image.MyImageSwitcher;
import com.ess.anime.wallpaper.utils.ComponentUtils;
import com.ess.anime.wallpaper.utils.UIUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class DonateFragment extends DialogFragment {

    private Unbinder mUnbinder;

    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.image_switcher)
    MyImageSwitcher mImageSwitcher;
    @BindView(R.id.layout_donate)
    ViewGroup mLayoutDonate;
    @BindView(R.id.iv_alipay)
    ImageView mIvAlipay;
    @BindView(R.id.iv_wechat)
    ImageView mIvWechat;
    @BindView(R.id.iv_close)
    ImageView mIvClose;

    private boolean mHasClickedDonateButton;
    private boolean mHasDonated;
    private boolean mHasFlipped;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_donate, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        initViews();
        rootView.post(this::startAnim);
        return rootView;
    }

    private void initViews() {
        OnTouchScaleListener listener = new OnTouchScaleListener();
        mIvAlipay.setOnTouchListener(listener);
        mIvWechat.setOnTouchListener(listener);

        mImageSwitcher.loadImage(R.drawable.d2, R.drawable.d4);
    }

    private void startAnim() {
        mImageSwitcher.setScaleX(0);
        mImageSwitcher.setScaleY(0);
        mImageSwitcher.animate()
                .scaleX(1)
                .scaleY(1)
                .setInterpolator(new BounceInterpolator())
                .setDuration(1000)
                .start();

        float transY = UIUtils.getScreenHeight(getContext()) - mLayoutDonate.getY();
        mIvAlipay.setTranslationY(transY);
        mIvAlipay.animate()
                .translationY(0)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(500)
                .setStartDelay(1000)
                .start();

        mIvWechat.setTranslationY(transY);
        mIvWechat.animate()
                .translationY(0)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(500)
                .setStartDelay(1300)
                .start();

        transY = -mTvTitle.getY() - mTvTitle.getHeight() - UIUtils.getStatusBarHeight(getContext());
        mTvTitle.setTranslationY(transY);
        mTvTitle.animate()
                .translationY(0)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(800)
                .setStartDelay(1000)
                .start();

        mIvClose.setAlpha(0f);
        mIvClose.animate()
                .alpha(1)
                .setDuration(500)
                .setStartDelay(1800)
                .start();
    }


    @OnClick(R.id.iv_alipay)
    void donateViaAlipay() {
        if (!mHasClickedDonateButton && ComponentUtils.isActivityActive(getActivity())) {
            mHasClickedDonateButton = true;
            DonateHelper.donateViaAlipay(getActivity());
        }
    }

    @OnClick(R.id.iv_wechat)
    void donateViaWechat() {
        if (!mHasClickedDonateButton && ComponentUtils.isActivityActive(getActivity())) {
            mHasClickedDonateButton = true;
            DonateHelper.donateViaWechat(getActivity());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.setCanceledOnTouchOutside(false);
            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawableResource(android.R.color.transparent);
                window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mHasDonated && !mHasFlipped) {
            mHasFlipped = true;
            mIvAlipay.setVisibility(View.GONE);
            mIvWechat.setVisibility(View.GONE);
            mImageSwitcher.flipImage();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mHasClickedDonateButton) {
            mHasDonated = true;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @OnClick(R.id.iv_close)
    @Override
    public void dismiss() {
        super.dismiss();
    }
}
