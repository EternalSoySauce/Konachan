package com.ess.anime.wallpaper.ui.fragment;

import android.os.Bundle;

import com.android.volley.Request;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.adapter.RecyclerPostAdapter;
import com.ess.anime.wallpaper.bean.ImageBean;
import com.ess.anime.wallpaper.bean.MsgBean;
import com.ess.anime.wallpaper.bean.ThumbBean;
import com.ess.anime.wallpaper.glide.MyGlideModule;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.http.HandlerFuture;
import com.ess.anime.wallpaper.http.OkHttp;
import com.ess.anime.wallpaper.ui.view.CustomLoadMoreView;
import com.ess.anime.wallpaper.ui.view.GridDividerItemDecoration;
import com.ess.anime.wallpaper.utils.SystemUtils;
import com.ess.anime.wallpaper.utils.FileUtils;
import com.ess.anime.wallpaper.utils.UIUtils;
import com.ess.anime.wallpaper.website.WebsiteManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class PoolPostFragment extends BaseFragment implements BaseQuickAdapter.RequestLoadMoreListener {

    public final static String TAG = PoolPostFragment.class.getName();

    private SwipeRefreshLayout mSwipeRefresh;
    private RecyclerView mRvPosts;
    private GridLayoutManager mLayoutManager;
    private RecyclerPostAdapter mPostAdapter;
    private String mLinkToShow;
    private int mCurrentPage;

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
        OkHttp.cancel(TAG);
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
        int span = Math.max(UIUtils.px2dp(getContext(), UIUtils.getScreenWidth(getContext())) / 165, 2);
        mRvPosts = mRootView.findViewById(R.id.rv_pool_post);
        mLayoutManager = new GridLayoutManager(getActivity(), span);
        mRvPosts.setLayoutManager(mLayoutManager);

        mPostAdapter = new RecyclerPostAdapter(TAG);
        mPostAdapter.bindToRecyclerView(mRvPosts);
        mPostAdapter.setOnLoadMoreListener(this, mRvPosts);
        mPostAdapter.setPreLoadNumber(10);
        mPostAdapter.setLoadMoreView(new CustomLoadMoreView());

        int spaceHor = UIUtils.dp2px(getActivity(), 5);
        int spaceVer = UIUtils.dp2px(getActivity(), 10);
        mRvPosts.addItemDecoration(new GridDividerItemDecoration(
                span, GridDividerItemDecoration.VERTICAL, spaceHor, spaceVer, true));
    }

    // 滑动加载更多
    @Override
    public void onLoadMoreRequested() {
        if (!SystemUtils.isActivityActive(getActivity())) {
            return;
        }

        String url = WebsiteManager.getInstance().getWebsiteConfig()
                .getPoolPostUrl(mLinkToShow, ++mCurrentPage);
        OkHttp.connect(url, TAG, new OkHttp.OkHttpCallback() {
            @Override
            public void onFailure() {
                onLoadMoreRequested();
            }

            @Override
            public void onSuccessful(String body) {
                HandlerFuture.ofWork(body)
                        .applyThen(body1 -> {
                            return WebsiteManager.getInstance()
                                    .getWebsiteConfig()
                                    .getHtmlParser()
                                    .getThumbListOfPool(Jsoup.parse(body1));
                        })
                        .runOn(HandlerFuture.IO.UI)
                        .applyThen(thumbList -> {
                            addMoreThumbList(thumbList);
                        });
            }
        }, Request.Priority.IMMEDIATE);
    }

    //加载更多完成后刷新界面
    private void addMoreThumbList(final List<ThumbBean> newList) {
        if (!SystemUtils.isActivityActive(getActivity()) || !mPostAdapter.isLoading()) {
            return;
        }

        mPostAdapter.setEmptyView(R.layout.layout_load_nothing, mRvPosts);
        if (mPostAdapter.loadMoreDatas(newList)) {
            mPostAdapter.loadMoreComplete();
        } else {
            mPostAdapter.loadMoreEnd(true);
        }
    }

    private void getNewPosts(int page) {
        if (!SystemUtils.isActivityActive(getActivity())) {
            return;
        }

        String url = WebsiteManager.getInstance().getWebsiteConfig()
                .getPoolPostUrl(mLinkToShow, page);
        OkHttp.connect(url, TAG, new OkHttp.OkHttpCallback() {
            @Override
            public void onFailure() {
                getNewPosts(page);
            }

            @Override
            public void onSuccessful(String body) {
                HandlerFuture.ofWork(body)
                        .applyThen(body1 -> {
                            return WebsiteManager.getInstance()
                                    .getWebsiteConfig()
                                    .getHtmlParser()
                                    .getThumbListOfPool(Jsoup.parse(body1));
                        })
                        .runOn(HandlerFuture.IO.UI)
                        .applyThen(thumbList -> {
                            refreshThumbList(thumbList);
                        });
            }
        }, Request.Priority.IMMEDIATE);
    }

    // 搜索新内容或下拉刷新完成后刷新界面
    private void refreshThumbList(List<ThumbBean> newList) {
        if (!SystemUtils.isActivityActive(getActivity()) || !mSwipeRefresh.isRefreshing()) {
            return;
        }

        if (mPostAdapter.refreshDatas(newList)) {
            scrollToTop();
        }
        mSwipeRefresh.setRefreshing(false);
    }

    void scrollToTop() {
        int smoothPos = 7 * mLayoutManager.getSpanCount();
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
                        if (WebsiteManager.getInstance().getWebsiteConfig().needReloadDetailByIdForPoolPost()) {
                            reloadDetailById(thumbBean);
                        } else {
                            checkImageBean(thumbBean);
                        }
                    }
                    break;
                }
            }
        }
    }

    // 部分网站（如Gelbooru）在解析PoolPost的时候无法获取到tempPost，
    // 需要根据postId重新进行一次Post查询
    // 解析完成后再checkToReplacePostData()
    private void reloadDetailById(ThumbBean thumbBean) {
        List<String> tagList = new ArrayList<>();
        tagList.add("id:" + thumbBean.id);
        String url = WebsiteManager.getInstance().getWebsiteConfig().getPostUrl(1, tagList);
        OkHttp.connect(url, TAG, new OkHttp.OkHttpCallback() {
            @Override
            public void onFailure() {
                OkHttp.connect(url, TAG, this, Request.Priority.HIGH);
            }

            @Override
            public void onSuccessful(String body) {
                HandlerFuture.ofWork(body)
                        .applyThen(body1 -> {
                            return WebsiteManager.getInstance()
                                    .getWebsiteConfig()
                                    .getHtmlParser()
                                    .getThumbList(Jsoup.parse(body1))
                                    .get(0);
                        })
                        .runOn(HandlerFuture.IO.UI)
                        .applyThen(thumbBean1 -> {
                            thumbBean1.imageBean = thumbBean.imageBean;
                            checkImageBean(thumbBean1);
                            mPostAdapter.replaceData(thumbBean1);
                            // 发送通知到ImageFragment, DetailFragment
                            EventBus.getDefault().post(new MsgBean(Constants.RELOAD_DETAIL_BY_ID, thumbBean1));
                        });
            }
        }, Request.Priority.HIGH);
    }

    private void checkImageBean(ThumbBean thumbBean) {
        thumbBean.checkToReplacePostData();
        String url = thumbBean.imageBean.posts[0].getMinSizeImageUrl();
        if (FileUtils.isImageType(url) && SystemUtils.isActivityActive(getActivity())) {
            MyGlideModule.preloadImage(getActivity(), url);
        }
    }

    public static PoolPostFragment newInstance(String linkToShow) {
        PoolPostFragment fragment = new PoolPostFragment();
        fragment.mLinkToShow = linkToShow;
        return fragment;
    }
}
