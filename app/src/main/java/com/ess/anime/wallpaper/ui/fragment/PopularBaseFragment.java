package com.ess.anime.wallpaper.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

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
import com.ess.anime.wallpaper.model.helper.SoundHelper;
import com.ess.anime.wallpaper.model.viewmodel.PopularWebsiteViewModel;
import com.ess.anime.wallpaper.ui.view.CustomLoadMoreView;
import com.ess.anime.wallpaper.ui.view.GeneralRecyclerView;
import com.ess.anime.wallpaper.ui.view.GridDividerItemDecoration;
import com.ess.anime.wallpaper.utils.FileUtils;
import com.ess.anime.wallpaper.utils.SystemUtils;
import com.ess.anime.wallpaper.utils.UIUtils;
import com.ess.anime.wallpaper.website.WebsiteManager;
import com.qmuiteam.qmui.util.QMUIDeviceHelper;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jsoup.Jsoup;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;

public abstract class PopularBaseFragment extends BaseFragment implements
        WebsiteManager.OnWebsiteChangeListener,
        BaseQuickAdapter.RequestLoadMoreListener {

    public final String TAG = PopularBaseFragment.class.getName() + UUID.randomUUID().toString();

    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefresh;
    @BindView(R.id.rv_post)
    GeneralRecyclerView mRvPosts;

    private Activity mActivity;
    private PopularWebsiteViewModel mViewModel;
    private StaggeredGridLayoutManager mLayoutManager;
    private RecyclerPostAdapter mPostAdapter;

    private boolean mHasInit;
    private boolean mHasLoadedData;
    private int mCurrentPage;  // 当前页码

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    @Override
    int layoutRes() {
        return R.layout.fragment_popular_base;
    }

    @Override
    void init(Bundle savedInstanceState) {
        mHasLoadedData = false;
        mCurrentPage = 1;
        initSwipeRefreshLayout();
        initRecyclerView();
        initViewModel();
        WebsiteManager.getInstance().registerWebsiteChangeListener(this);
    }

    @Override
    void updateUI() {
        super.updateUI();
        updateRecyclerViewSpanCount();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        OkHttp.cancel(TAG);
        WebsiteManager.getInstance().unregisterWebsiteChangeListener(this);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            mHasInit = true;
            if (mViewModel != null && !mHasLoadedData) {
                retrieveAllData();
            }
        }
    }

    private boolean isPostImageShownRectangular() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return preferences.getBoolean(Constants.IS_POST_IMAGE_SHOWN_RECTANGULAR, true);
    }

    private void initViewModel() {
        mViewModel = new ViewModelProvider(requireActivity()).get(PopularWebsiteViewModel.class);
        mViewModel.getCalenderLiveData().observe(getViewLifecycleOwner(), new Observer<Calendar>() {
            @Override
            public void onChanged(Calendar calendar) {
                if (mHasInit) {
                    if (getUserVisibleHint()) {
                        retrieveAllData();
                    } else {
                        resetAll();
                    }
                }
            }
        });
    }

    private void initSwipeRefreshLayout() {
        mSwipeRefresh.setEnabled(supportWebsitePopular());
        mSwipeRefresh.setRefreshing(false);
        //下拉刷新
        mSwipeRefresh.setOnRefreshListener(() -> {
            if (mPostAdapter.getData().isEmpty()) {
                mPostAdapter.setEmptyView(R.layout.layout_loading_cirno, mRvPosts);
            }
            getNewPosts(1);
        });
    }

    private void initRecyclerView() {
        mLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        mRvPosts.setLayoutManager(mLayoutManager);

        mPostAdapter = new RecyclerPostAdapter(TAG);
        mPostAdapter.bindToRecyclerView(mRvPosts);
        mPostAdapter.setOnLoadMoreListener(this, mRvPosts);
        mPostAdapter.setLoadMoreView(new CustomLoadMoreView());
        mPostAdapter.changeImageShownFormat(isPostImageShownRectangular());
    }

    private void updateRecyclerViewSpanCount() {
        if (mActivity != null && mLayoutManager != null && mRvPosts != null) {
            int span;
            if (UIUtils.isLandscape(mActivity)) {
                span = 4;
            } else {
                span = QMUIDeviceHelper.isTablet(mActivity) ? 3 : 2;
            }
            mLayoutManager.setSpanCount(span);

            int spaceHor = UIUtils.dp2px(mActivity, 5);
            int spaceVer = UIUtils.dp2px(mActivity, 10);
            mRvPosts.clearItemDecorations();
            mRvPosts.addItemDecoration(new GridDividerItemDecoration(
                    span, GridDividerItemDecoration.VERTICAL, spaceHor, spaceVer, true));

            mPostAdapter.setPreLoadNumber(span * 5);
        }
    }

    public void scrollToTop() {
        int smoothPos = 7 * mLayoutManager.getSpanCount();
        int lastVisiblePos = mLayoutManager.findLastVisibleItemPositions(null)[0];
        if (lastVisiblePos > smoothPos) {
            mRvPosts.scrollToPosition(smoothPos);
        }
        mRvPosts.smoothScrollToPosition(0);
    }

    // 滑动加载更多
    @Override
    public void onLoadMoreRequested() {
        fetchThumbList(mViewModel.getRealYear(), mViewModel.getRealMonth(),
                mViewModel.getRealDay(), ++mCurrentPage, this::addMoreThumbList);
    }

    //加载更多完成后刷新界面
    private void addMoreThumbList(final List<ThumbBean> newList) {
        if (!mPostAdapter.isLoading()) {
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
        fetchThumbList(mViewModel.getRealYear(), mViewModel.getRealMonth(),
                mViewModel.getRealDay(), page, this::refreshThumbList);
    }

    //搜索新内容或下拉刷新完成后刷新界面
    private void refreshThumbList(final List<ThumbBean> newList) {
        if (!mSwipeRefresh.isRefreshing()) {
            return;
        }

        mPostAdapter.setEmptyView(R.layout.layout_load_nothing, mRvPosts);
        if (mPostAdapter.refreshDatas(newList)) {
            scrollToTop();
        } else if (mPostAdapter.getData().isEmpty()) {
            loadNothing();
        }
        mSwipeRefresh.setRefreshing(false);
    }

    private void fetchThumbList(int year, int month, int day, int page, Consumer<List<ThumbBean>> callback) {
        if (supportWebsitePopular()) {
            String url = getWebsitePopularUrl(year, month, day, page);
            Map<String, String> headerMap = WebsiteManager.getInstance().getRequestHeaders();
            OkHttp.connect(url, TAG, headerMap, new OkHttp.OkHttpCallback() {
                @Override
                public void onFailure(int errorCode, String errorMessage) {
                    if (errorCode == 404) {
                        // 404按成功处理，UI显示无搜索结果而不是访问失败
                        onSuccessful(errorMessage);
                    } else {
                        checkNetwork();
                    }
                }

                @Override
                public void onSuccessful(String body) {
                    HandlerFuture.ofWork(body)
                            .applyThen(body1 -> {
                                return WebsiteManager.getInstance()
                                        .getWebsiteConfig()
                                        .getHtmlParser()
                                        .getThumbList(Jsoup.parse(body1));
                            })
                            .runOn(HandlerFuture.IO.UI)
                            .applyThen(callback);

                }
            }, Request.Priority.IMMEDIATE);
        } else {
            callback.accept(Collections.emptyList());
        }
    }

    // 初始化所有数据，清空adapter，以便加载新内容
    private void resetAll() {
        OkHttp.cancel(TAG);
        mPostAdapter.setEmptyView(R.layout.layout_loading_cirno, mRvPosts);
        mPostAdapter.setNewData(null);
        mSwipeRefresh.setEnabled(supportWebsitePopular());
        mSwipeRefresh.setRefreshing(false);
        mHasLoadedData = false;
        mCurrentPage = 1;
    }

    @Override
    public void onWebsiteChanged(String baseUrl) {
        if (getUserVisibleHint()) {
            retrieveAllData();
        } else {
            resetAll();
        }
    }

    private void retrieveAllData() {
        resetAll();
        mHasLoadedData = true;
        mSwipeRefresh.setRefreshing(true);
        getNewPosts(mCurrentPage);
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
                        String url = imageBean.posts[0].getMinSizeImageUrl();
                        if (FileUtils.isImageType(url) && SystemUtils.isActivityActive(mActivity)) {
                            MyGlideModule.preloadImage(mActivity, url);
                        }
                    }
                    break;
                }
            }
        }
    }

    //搜所无结果
    private void loadNothing() {
        if (supportWebsitePopular()) {
            mPostAdapter.setEmptyView(R.layout.layout_load_nothing, mRvPosts);
        } else {
            mPostAdapter.setEmptyView(getNotSupportEmptyView());
        }
        mPostAdapter.setNewData(null);
        mSwipeRefresh.setRefreshing(false);
    }

    //访问网络失败
    private void checkNetwork() {
        mSwipeRefresh.setRefreshing(false);
        if (mPostAdapter.getData().isEmpty()) {
            mPostAdapter.setEmptyView(R.layout.layout_load_no_network, mRvPosts);
            SoundHelper.getInstance().playLoadNoNetworkSound(getActivity());
        } else {
            mPostAdapter.loadMoreFail();
        }
    }

    // todo 目前各站是否支持排行榜与日期参数无关，暂随便给值
    public boolean supportWebsitePopular() {
        return !TextUtils.isEmpty(getWebsitePopularUrl(1, 1, 1, 1));
    }

    abstract String getWebsitePopularUrl(int year, int month, int day, int page);

    abstract int getNotSupportTipsResId();

    private View getNotSupportEmptyView() {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.layout_not_support_popular, mRvPosts, false);
        TextView tvTitle = view.findViewById(R.id.tv_title);
        tvTitle.setText(getNotSupportTipsResId());
        return view;
    }

}
