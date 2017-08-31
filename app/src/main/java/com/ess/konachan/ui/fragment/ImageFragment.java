package com.ess.konachan.ui.fragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.ess.konachan.R;
import com.ess.konachan.bean.ImageBean;
import com.ess.konachan.bean.MsgBean;
import com.ess.konachan.bean.ThumbBean;
import com.ess.konachan.global.Constants;
import com.ess.konachan.global.GlideConfig;
import com.ess.konachan.ui.activity.ImageDetailActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import uk.co.senab.photoview.PhotoView;

public class ImageFragment extends Fragment {

    private ImageDetailActivity mActivity;
    private ThumbBean mThumbBean;
    private ImageBean mImageBean;

    private View mRootView;
    private SwipeRefreshLayout mSwipeRefresh;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (ImageDetailActivity) context;
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // 防止软件进入后台过久被系统回收导致切换回来时产生空指针异常
        outState.putParcelable(Constants.THUMB_BEAN, mThumbBean);
        outState.putParcelable(Constants.IMAGE_BEAN, mImageBean);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mThumbBean = savedInstanceState.getParcelable(Constants.THUMB_BEAN);
            mImageBean = savedInstanceState.getParcelable(Constants.IMAGE_BEAN);
        } else {
            mThumbBean = mActivity.getThumbBean();
            mImageBean = mThumbBean.imageBean;
        }
        mRootView = inflater.inflate(R.layout.fragment_image, container, false);
        initView();
        return mRootView;
    }

    private void initView() {
        mSwipeRefresh = (SwipeRefreshLayout) mRootView.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        mSwipeRefresh.setEnabled(false);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadImage();
            }
        });

        if (mImageBean != null) {
            loadImage();
        } else {
            mSwipeRefresh.setRefreshing(true);
            mSwipeRefresh.getChildAt(0).setVisibility(View.GONE);
        }
    }

    private void loadImage() {
        PhotoView ivImage = (PhotoView) mRootView.findViewById(R.id.iv_image);
        String url = mImageBean.posts[0].sampleUrl;
        RequestBuilder<Drawable> listener = Glide.with(mActivity).load(url)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        mSwipeRefresh.setRefreshing(false);
                        mSwipeRefresh.setEnabled(true);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        mSwipeRefresh.setRefreshing(false);
                        mSwipeRefresh.setEnabled(false);
                        return false;
                    }
                });
        GlideConfig.getInstance().loadImage(listener, ivImage);
    }

    //获取到图片详细信息后收到的通知，obj 为 Json (String)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getImageDetail(MsgBean msgBean) {
        if (msgBean.msg.equals(Constants.GET_IMAGE_DETAIL)) {
            String json = (String) msgBean.obj;
            ImageBean imageBean = ImageBean.getImageDetailFromJson(json);
            if (mThumbBean.thumbUrl.equals(imageBean.posts[0].previewUrl)) {
                mImageBean = imageBean;
                loadImage();
                mSwipeRefresh.setRefreshing(false);
                mSwipeRefresh.getChildAt(0).setVisibility(View.VISIBLE);
                mActivity.setId(imageBean);
                mActivity.setImageBean(imageBean);
            }
        }
    }

    public static ImageFragment newInstance(String title) {
        ImageFragment fragment = new ImageFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.PAGE_TITLE, title);
        fragment.setArguments(bundle);
        return fragment;
    }
}
