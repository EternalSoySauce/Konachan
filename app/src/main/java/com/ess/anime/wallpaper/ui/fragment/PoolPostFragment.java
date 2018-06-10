package com.ess.anime.wallpaper.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Priority;
import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.adapter.RecyclerPostAdapter;
import com.ess.anime.wallpaper.bean.ImageBean;
import com.ess.anime.wallpaper.bean.MsgBean;
import com.ess.anime.wallpaper.bean.ThumbBean;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.http.OkHttp;
import com.ess.anime.wallpaper.http.ParseHtml;
import com.ess.anime.wallpaper.other.GlideApp;
import com.ess.anime.wallpaper.other.MyGlideModule;
import com.ess.anime.wallpaper.utils.FileUtils;
import com.ess.anime.wallpaper.utils.UIUtils;
import com.ess.anime.wallpaper.view.GridDividerItemDecoration;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class PoolPostFragment extends Fragment {

    private View mRootView;
    private SwipeRefreshLayout mSwipeRefresh;
    private RecyclerView mRvPosts;
    private GridLayoutManager mLayoutManager;
    private RecyclerPostAdapter mPostAdapter;
    private String mLinkToShow;
    private int mCurrentPage;
    private boolean mIsLoadingMore = false;
    private boolean mLoadMoreAgain = true;

    private Call mNewCall;
    private Call mLoadMoreCall;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mNewCall.cancel();
        if (mLoadMoreCall != null) {
            mLoadMoreCall.cancel();
        }
        mPostAdapter.cancelAll();
        mSwipeRefresh.setRefreshing(false);
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Constants.LINK_TO_SHOW, mLinkToShow);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mLinkToShow = savedInstanceState.getString(Constants.LINK_TO_SHOW);
        }
        mRootView = inflater.inflate(R.layout.fragment_pool_post, container, false);
        initSwipeRefreshLayout();
        initRecyclerView();
        mCurrentPage = 1;
        getNewPosts(mCurrentPage);
        return mRootView;
    }

    private void initSwipeRefreshLayout() {
        mSwipeRefresh = (SwipeRefreshLayout) mRootView.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefresh.setRefreshing(true);
        //下拉刷新
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getNewPosts(1);
            }
        });
    }

    private void initRecyclerView() {
        mRvPosts = (RecyclerView) mRootView.findViewById(R.id.rv_pool_post);
        mLayoutManager = new GridLayoutManager(getActivity(), 2);
        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return position >= mPostAdapter.getDataListSize() ? 2 : 1;
            }
        });
        mRvPosts.setLayoutManager(mLayoutManager);

        mPostAdapter = new RecyclerPostAdapter(getActivity(), new ArrayList<ThumbBean>());
        mRvPosts.setAdapter(mPostAdapter);

        int spaceHor = UIUtils.dp2px(getActivity(), 5);
        int spaceVer = UIUtils.dp2px(getActivity(), 10);
        mRvPosts.addItemDecoration(new GridDividerItemDecoration(
                2, GridDividerItemDecoration.VERTICAL, spaceHor, spaceVer, true));

        //滑动加载更多
        mRvPosts.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastVisiblePosition = mLayoutManager.findLastVisibleItemPosition();
                int totalCount = mPostAdapter.getDataListSize();
                if (totalCount > 0 && lastVisiblePosition >= totalCount - 10
                        && !mIsLoadingMore && mLoadMoreAgain) {
                    mIsLoadingMore = true;
                    mPostAdapter.showLoadMore();
                    loadMore();
                }
            }
        });
    }

    public void getNewPosts(int page) {
        final String url = OkHttp.getPoolPostUrl(getActivity(), mLinkToShow, page);
        mNewCall = OkHttp.getInstance().connect(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (OkHttp.isNetworkProblem(e)) {
                    mNewCall = OkHttp.getInstance().connect(url, this);
                }
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (response.isSuccessful()) {
                    String html = response.body().string();
                    if (getActivity() != null) {
                        refreshThumbList(ParseHtml.getThumbListOfPool(html));
                    }
                } else {
                    mNewCall = OkHttp.getInstance().connect(url, this);
                }
                response.close();
            }
        });
    }

    // 搜索新内容或下拉刷新完成后刷新界面
    private void refreshThumbList(final ArrayList<ThumbBean> newList) {
        if (!mSwipeRefresh.isRefreshing()) {
            return;
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!newList.isEmpty()) {
                    mPostAdapter.refreshDatas(newList);
                    scrollToTop();
                }
                mSwipeRefresh.setRefreshing(false);
            }
        });
    }

    // 滑动加载更多
    private void loadMore() {
        final String url = OkHttp.getPoolPostUrl(getActivity(), mLinkToShow, ++mCurrentPage);
        mLoadMoreCall = OkHttp.getInstance().connect(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (OkHttp.isNetworkProblem(e)) {
                    mLoadMoreCall = OkHttp.getInstance().connect(url, this);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String html = response.body().string();
                    if (getActivity() != null) {
                        addMoreThumbList(ParseHtml.getThumbListOfPool(html));
                    }
                } else {
                    mLoadMoreCall = OkHttp.getInstance().connect(url, this);
                }
                response.close();
            }
        });
    }

    //加载更多完成后刷新界面
    private void addMoreThumbList(final ArrayList<ThumbBean> newList) {
        if (!mIsLoadingMore) {
            return;
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPostAdapter.loadMoreDatas(newList);
                mPostAdapter.showNormal();
                if (newList.isEmpty()) {
                    Toast.makeText(getActivity(), R.string.no_more_load, Toast.LENGTH_SHORT).show();
                } else {
                    mIsLoadingMore = false;
                }
            }
        });
    }

    public void scrollToTop() {
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
            ArrayList<ThumbBean> thumbList = mPostAdapter.getThumbList();
            for (ThumbBean thumbBean : thumbList) {
                if (thumbBean.checkImageBelongs(imageBean)) {
                    if (thumbBean.imageBean == null) {
                        thumbBean.imageBean = imageBean;
                        String url = imageBean.posts[0].sampleUrl;
                        if (FileUtils.isImageType(url) && !getActivity().isDestroyed()) {
                            GlideApp.with(getActivity())
                                    .load(MyGlideModule.makeGlideUrl(url))
                                    .priority(Priority.HIGH)
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
