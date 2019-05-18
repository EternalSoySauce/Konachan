package com.ess.anime.wallpaper.ui.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.adapter.RecyclerPostAdapter;
import com.ess.anime.wallpaper.bean.ImageBean;
import com.ess.anime.wallpaper.bean.MsgBean;
import com.ess.anime.wallpaper.bean.ThumbBean;
import com.ess.anime.wallpaper.glide.GlideApp;
import com.ess.anime.wallpaper.glide.MyGlideModule;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.http.OkHttp;
import com.ess.anime.wallpaper.http.parser.HtmlParserFactory;
import com.ess.anime.wallpaper.ui.view.CustomLoadMoreView;
import com.ess.anime.wallpaper.ui.view.GridDividerItemDecoration;
import com.ess.anime.wallpaper.utils.FileUtils;
import com.ess.anime.wallpaper.utils.UIUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class PoolPostFragment extends BaseFragment implements BaseQuickAdapter.RequestLoadMoreListener {

    private SwipeRefreshLayout mSwipeRefresh;
    private RecyclerView mRvPosts;
    private GridLayoutManager mLayoutManager;
    private RecyclerPostAdapter mPostAdapter;
    private String mLinkToShow;
    private int mCurrentPage;

    private Call mNewCall;
    private Call mLoadMoreCall;

    @Override
    int layoutRes() {
        return R.layout.fragment_pool_post;
    }

    @Override
    void init(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mLinkToShow = savedInstanceState.getString(Constants.LINK_TO_SHOW);
        }
        initSwipeRefreshLayout();
        initRecyclerView();
        mCurrentPage = 1;
        getNewPosts(mCurrentPage);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Constants.LINK_TO_SHOW, mLinkToShow);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mNewCall.cancel();
        if (mLoadMoreCall != null) {
            mLoadMoreCall.cancel();
        }
        mPostAdapter.cancelAll();
        mSwipeRefresh.setRefreshing(false);
        EventBus.getDefault().unregister(this);
    }

    private void initSwipeRefreshLayout() {
        mSwipeRefresh = mRootView.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefresh.setRefreshing(true);
        //下拉刷新
        mSwipeRefresh.setOnRefreshListener(() -> getNewPosts(1));
    }

    private void initRecyclerView() {
        int span = 2;
        mRvPosts = mRootView.findViewById(R.id.rv_pool_post);
        mLayoutManager = new GridLayoutManager(getActivity(), span);
        mRvPosts.setLayoutManager(mLayoutManager);

        mPostAdapter = new RecyclerPostAdapter();
        mPostAdapter.setOnLoadMoreListener(this, mRvPosts);
        mPostAdapter.setPreLoadNumber(10);
        mPostAdapter.setLoadMoreView(new CustomLoadMoreView());
        mRvPosts.setAdapter(mPostAdapter);

        int spaceHor = UIUtils.dp2px(getActivity(), 5);
        int spaceVer = UIUtils.dp2px(getActivity(), 10);
        mRvPosts.addItemDecoration(new GridDividerItemDecoration(
                span, GridDividerItemDecoration.VERTICAL, spaceHor, spaceVer, true));
    }

    // 滑动加载更多
    @Override
    public void onLoadMoreRequested() {
        String url = OkHttp.getPoolPostUrl(getActivity(), mLinkToShow, ++mCurrentPage);
        mLoadMoreCall = OkHttp.getInstance().connect(url, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (OkHttp.isNetworkProblem(e)) {
                    mLoadMoreCall = OkHttp.getInstance().connect(url, this);
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String html = response.body().string();
                    if (getActivity() != null) {
                        addMoreThumbList(HtmlParserFactory.createParser(getActivity(), html).getThumbListOfPool());
                    }
                } else {
                    mLoadMoreCall = OkHttp.getInstance().connect(url, this);
                }
                response.close();
            }
        });
    }

    private void getNewPosts(int page) {
        final String url = OkHttp.getPoolPostUrl(getActivity(), mLinkToShow, page);
        mNewCall = OkHttp.getInstance().connect(url, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (OkHttp.isNetworkProblem(e)) {
                    mNewCall = OkHttp.getInstance().connect(url, this);
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String html = response.body().string();
                    if (getActivity() != null) {
                        refreshThumbList(HtmlParserFactory.createParser(getActivity(), html).getThumbListOfPool());
                    }
                } else {
                    mNewCall = OkHttp.getInstance().connect(url, this);
                }
                response.close();
            }
        });
    }

    // 搜索新内容或下拉刷新完成后刷新界面
    private void refreshThumbList(final List<ThumbBean> newList) {
        if (!mSwipeRefresh.isRefreshing()) {
            return;
        }

        getActivity().runOnUiThread(() -> {
            if (mPostAdapter.refreshDatas(newList)) {
                scrollToTop();
            }
            mSwipeRefresh.setRefreshing(false);
        });
    }

    //加载更多完成后刷新界面
    private void addMoreThumbList(final List<ThumbBean> newList) {
        if (!mPostAdapter.isLoading()) {
            return;
        }

        getActivity().runOnUiThread(() -> {
            mPostAdapter.setEmptyView(R.layout.layout_load_nothing, mRvPosts);
            if (mPostAdapter.loadMoreDatas(newList)) {
                mPostAdapter.loadMoreComplete();
            } else {
                mPostAdapter.loadMoreEnd();
            }
        });
    }

    void scrollToTop() {
        int smoothPos = 14;
        if (mLayoutManager.findLastVisibleItemPosition() > smoothPos) {
            mRvPosts.scrollToPosition(smoothPos);
        }
        mRvPosts.smoothScrollToPosition(0);
    }

    //获取到图片详细信息后收到的通知，obj 为 Json (String)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void setImageBean(MsgBean msgBean) {
        if (msgBean.msg.equals(Constants.GET_IMAGE_DETAIL)) {
            String json = (String) msgBean.obj;
            ImageBean imageBean = ImageBean.getImageDetailFromJson(json);
            List<ThumbBean> thumbList = mPostAdapter.getData();
            for (ThumbBean thumbBean : thumbList) {
                if (thumbBean.checkImageBelongs(imageBean)) {
                    if (thumbBean.imageBean == null) {
                        thumbBean.imageBean = imageBean;
                        thumbBean.checkToReplacePostData();
                        String url = imageBean.posts[0].sampleUrl;
                        if (FileUtils.isImageType(url) && !getActivity().isDestroyed()) {
                            GlideApp.with(getActivity())
                                    .load(MyGlideModule.makeGlideUrl(url))
                                    .submit();
                        }
                    }
                    break;
                }
            }
        }
    }

    public static PoolPostFragment newInstance(String linkToShow) {
        PoolPostFragment fragment = new PoolPostFragment();
        fragment.mLinkToShow = linkToShow;
        return fragment;
    }
}
