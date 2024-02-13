package com.ess.anime.wallpaper.ui.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.HapticFeedbackConstants;
import android.view.View;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.bean.ImageBean;
import com.ess.anime.wallpaper.bean.MsgBean;
import com.ess.anime.wallpaper.bean.ThumbBean;
import com.ess.anime.wallpaper.download.image.DownloadBean;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.listener.FlingEffector;
import com.ess.anime.wallpaper.model.helper.ImageDataHelper;
import com.ess.anime.wallpaper.ui.activity.ImageDetailActivity;
import com.ess.anime.wallpaper.ui.view.MultipleMediaLayout;
import com.ess.anime.wallpaper.utils.SystemUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Iterator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;

public class ImageFragment extends BaseFragment {

    @BindView(R.id.view_touch)
    View mTouchView;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefresh;
    @BindView(R.id.layout_multiple_media)
    MultipleMediaLayout mMediaLayout;

    private ImageDetailActivity mActivity;
    private ThumbBean mThumbBean;
    private ImageBean mImageBean;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = (ImageDetailActivity) context;
    }

    @Override
    int layoutRes() {
        return R.layout.fragment_image;
    }

    @Override
    void init(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mThumbBean = savedInstanceState.getParcelable(Constants.THUMB_BEAN);
            mImageBean = savedInstanceState.getParcelable(Constants.IMAGE_BEAN);
        } else {
            mThumbBean = mActivity.getThumbBean();
            mImageBean = mActivity.getImageBean();
        }
        initView();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // 防止软件进入后台过久被系统回收导致切换回来时产生空指针异常
        outState.putParcelable(Constants.THUMB_BEAN, mThumbBean);
        outState.putParcelable(Constants.IMAGE_BEAN, mImageBean);
    }

    @Override
    public void onResume() {
        super.onResume();
        // 发送通知到MultipleMediaLayout
        EventBus.getDefault().post(new MsgBean(Constants.RESUME_VIDEO, mMediaLayout.getMediaPath()));
    }

    @Override
    public void onPause() {
        super.onPause();
        // 发送通知到MultipleMediaLayout
        EventBus.getDefault().post(new MsgBean(Constants.PAUSE_VIDEO, mMediaLayout.getMediaPath()));
    }

    @Override
    public void onDestroyView() {
        mMediaLayout.reset();
        super.onDestroyView();
    }

    private void initView() {
        FlingEffector.addFlingEffect(mTouchView, (e1, e2, velocityX, velocityY) -> {
            if (SystemUtils.isActivityActive(mActivity)) {
                mActivity.flingToQuickSwitch(velocityX, velocityY);
            }
        });

        mSwipeRefresh.setEnabled(false);
        mSwipeRefresh.setOnRefreshListener(this::loadMedia);

        if (mImageBean != null) {
            loadMedia();
            mTouchView.setVisibility(View.GONE);
        } else {
            mSwipeRefresh.setRefreshing(true);
            mSwipeRefresh.getChildAt(0).setVisibility(View.GONE);
            mTouchView.setVisibility(View.VISIBLE);
        }

        mMediaLayout.setOnLongClickListener(v -> {
            downloadImage();
            return true;
        });

        mMediaLayout.getPhotoView().setOnLongClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            downloadImage();
            return true;
        });
        mMediaLayout.getPhotoView().setOnSingleFlingListener((e1, e2, velocityX, velocityY) -> {
            if (SystemUtils.isActivityActive(mActivity)) {
                mActivity.flingToQuickSwitch(velocityX, velocityY);
            }
            return false;
        });

        FlingEffector.addFlingEffect(mMediaLayout, (e1, e2, velocityX, velocityY) -> {
            if (SystemUtils.isActivityActive(mActivity)) {
                mActivity.flingToQuickSwitch(velocityX, velocityY);
            }
        });

        mMediaLayout.setBackgroundColor(Color.TRANSPARENT);
    }

    private void loadMedia() {
        if (SystemUtils.isActivityActive(mActivity)) {
            List<DownloadBean> chosenList = ImageDataHelper.makeDownloadChosenList(mActivity, mThumbBean, mImageBean);
            Iterator<DownloadBean> iterator = chosenList.iterator();
            while (iterator.hasNext()) {
                DownloadBean downloadBean = iterator.next();
                if (!downloadBean.fileExists) {
                    iterator.remove();
                }
            }
            if (chosenList.isEmpty()) {
                mMediaLayout.setMediaPath(mImageBean.posts[0].getMinSizeImageUrl());
            } else {
                DownloadBean downloadBean = chosenList.get(chosenList.size() - 1);
                mMediaLayout.setMediaPath(downloadBean.savePath);
            }
        }
    }

    private void downloadImage() {
        if (SystemUtils.isActivityActive(mActivity)) {
            mActivity.showChooseToDownloadDialog();
        }
    }

    //获取到图片详细信息后收到的通知，obj 为 Json (String)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getImageDetail(MsgBean msgBean) {
        if (msgBean.msg.equals(Constants.GET_IMAGE_DETAIL)) {
            String json = (String) msgBean.obj;
            ImageBean imageBean = ImageBean.getImageDetailFromJson(json);
            if (mThumbBean.checkImageBelongs(imageBean) && imageBean.hasPostBean()) {
                setImageDetail(imageBean);
            }
        }
    }

    // PoolPostFragment获取到imageBean后重新根据ID请求tempPost后收到的通知，obj 为 thumbBean
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void reloadDetailById(MsgBean msgBean) {
        if (msgBean.msg.equals(Constants.RELOAD_DETAIL_BY_ID)) {
            ThumbBean thumbBean = (ThumbBean) msgBean.obj;
            if (TextUtils.equals(mThumbBean.linkToShow, thumbBean.linkToShow)) {
                mThumbBean = thumbBean;
                setImageDetail(thumbBean.imageBean);
            }
        }
    }

    private void setImageDetail(ImageBean imageBean) {
        mImageBean = imageBean;
        mThumbBean.imageBean = imageBean;
        mThumbBean.checkToReplacePostData();
        loadMedia();
        mSwipeRefresh.setRefreshing(false);
        mSwipeRefresh.getChildAt(0).setVisibility(View.VISIBLE);
        mTouchView.setVisibility(View.GONE);
        mActivity.setId(imageBean);
        mActivity.setImageBean(imageBean);
    }

}
