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

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.listener.OnTouchScaleListener;
import com.ess.anime.wallpaper.model.helper.DonateHelper;
import com.ess.anime.wallpaper.ui.view.image.MyImageSwitcher;
import com.ess.anime.wallpaper.utils.SystemUtils;
import com.ess.anime.wallpaper.utils.UIUtils;

import java.util.Random;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class DonateFragment extends DialogFragment {

    private Unbinder mUnbinder;

    @BindView(R.id.switcher_title)
    MyImageSwitcher mSwitcherTitle;
    @BindView(R.id.switcher_image)
    MyImageSwitcher mSwitcherImage;
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
        OnTouchScaleListener listener = OnTouchScaleListener.DEFAULT;
        mIvAlipay.setOnTouchListener(listener);
        mIvWechat.setOnTouchListener(listener);

        int index = new Random().nextInt(2);
        int titleA = getResources().getIdentifier("ic_donate_title_a_" + index,
                "drawable", getContext().getPackageName());
        int titleB = getResources().getIdentifier("ic_donate_title_b_" + index,
                "drawable", getContext().getPackageName());
        mSwitcherTitle.loadImage(titleA, titleB);

        int imgA = getResources().getIdentifier("img_donate_a_" + index,
                "drawable", getContext().getPackageName());
        int imgB = getResources().getIdentifier("img_donate_b_" + index,
                "drawable", getContext().getPackageName());
        mSwitcherImage.loadImage(imgA, imgB);
    }

    private void startAnim() {
        mSwitcherImage.setScaleX(0);
        mSwitcherImage.setScaleY(0);
        mSwitcherImage.animate()
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

        transY = -mSwitcherTitle.getY() - mSwitcherTitle.getHeight() - UIUtils.getStatusBarHeight(getContext());
        mSwitcherTitle.setTranslationY(transY);
        mSwitcherTitle.animate()
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
        if (SystemUtils.isActivityActive(getActivity())) {
            mHasClickedDonateButton = true;
            DonateHelper.donateViaAlipay(getActivity());
        }
    }

    @OnClick(R.id.iv_wechat)
    void donateViaWechat() {
        if (SystemUtils.isActivityActive(getActivity())) {
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
            mSwitcherTitle.flipImage();
            mSwitcherImage.flipImage();
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
