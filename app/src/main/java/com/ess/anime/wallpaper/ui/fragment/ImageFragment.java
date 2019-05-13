package com.ess.anime.wallpaper.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.bean.ImageBean;
import com.ess.anime.wallpaper.bean.MsgBean;
import com.ess.anime.wallpaper.bean.ThumbBean;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.ui.activity.ImageDetailActivity;
import com.ess.anime.wallpaper.ui.view.MultipleMediaLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
        mSwipeRefresh = mRootView.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefresh.setEnabled(false);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadMedia();
            }
        });

        if (mImageBean != null) {
            loadMedia();
        } else {
            mSwipeRefresh.setRefreshing(true);
            mSwipeRefresh.getChildAt(0).setVisibility(View.GONE);
        }
    }

    private void loadMedia() {
        MultipleMediaLayout mediaLayout = mRootView.findViewById(R.id.layout_multiple_media);
        mediaLayout.setMediaPath(mImageBean.posts[0].sampleUrl);
    }

    //获取到图片详细信息后收到的通知，obj 为 Json (String)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getImageDetail(MsgBean msgBean) {
        if (msgBean.msg.equals(Constants.GET_IMAGE_DETAIL)) {
            String json = (String) msgBean.obj;
            ImageBean imageBean = ImageBean.getImageDetailFromJson(json);
            if (mThumbBean.checkImageBelongs(imageBean)) {
                mThumbBean.imageBean = imageBean;
                mThumbBean.checkToReplacePostData();
                mImageBean = imageBean;
                loadMedia();
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
