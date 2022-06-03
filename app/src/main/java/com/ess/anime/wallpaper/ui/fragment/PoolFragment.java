package com.ess.anime.wallpaper.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.adapter.RecyclerPoolAdapter;
import com.ess.anime.wallpaper.bean.PoolListBean;
import com.ess.anime.wallpaper.http.HandlerFuture;
import com.ess.anime.wallpaper.http.OkHttp;
import com.ess.anime.wallpaper.listener.DoubleTapEffector;
import com.ess.anime.wallpaper.model.helper.SoundHelper;
import com.ess.anime.wallpaper.ui.activity.MainActivity;
import com.ess.anime.wallpaper.ui.view.CustomLoadMoreView;
import com.ess.anime.wallpaper.ui.view.GeneralRecyclerView;
import com.ess.anime.wallpaper.ui.view.GridDividerItemDecoration;
import com.ess.anime.wallpaper.utils.UIUtils;
import com.ess.anime.wallpaper.website.WebsiteManager;
import com.zyyoona7.popup.EasyPopup;

import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.OnClick;

public class PoolFragment extends BaseFragment implements
        WebsiteManager.OnWebsiteChangeListener,
        BaseQuickAdapter.RequestLoadMoreListener {

    public final static String TAG = PoolFragment.class.getName();

    @BindView(R.id.tool_bar)
    Toolbar mToolbar;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefresh;
    @BindView(R.id.fl_pool_post)
    FrameLayout mLayoutFragment;
    @BindView(R.id.rv_pool)
    GeneralRecyclerView mRvPools;
    @BindView(R.id.iv_page)
    ImageView mIvPage;

    private MainActivity mActivity;
    private FragmentManager mFragmentManager;
    private PoolPostFragment mPoolPostFragment;
    private GridLayoutManager mLayoutManager;
    private RecyclerPoolAdapter mPoolAdapter;

    private EasyPopup mPopupPage;
    private TextView mTvFrom;
    private TextView mTvTo;
    private EditText mEtGoto;
    private int mCurrentPage;   // 当前页码
    private int mGoToPage;   // 跳转到的起始页码
    private String mCurrentSearchName;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = (MainActivity) context;
    }

    @Override
    int layoutRes() {
        return R.layout.fragment_pool;
    }

    @Override
    void init(Bundle savedInstanceState) {
        mFragmentManager = getChildFragmentManager();
        initToolBarLayout();
        initPopupPage();
        initSwipeRefreshLayout();
        initRecyclerView();
        resetAll(1);
        getNewPools(mCurrentPage);
        changeFromPage(mCurrentPage);
        changeToPage(mCurrentPage);
        WebsiteManager.getInstance().registerWebsiteChangeListener(this);
    }

    @Override
    void updateUI() {
        super.updateUI();
        updateRecyclerViewSpanCount();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mPoolPostFragment != null) {
            mFragmentManager.putFragment(outState, PoolPostFragment.class.getName(), mPoolPostFragment);
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mPoolPostFragment = (PoolPostFragment) mFragmentManager.getFragment(
                    savedInstanceState, PoolPostFragment.class.getName());
            if (mPoolPostFragment != null) {
                removePoolPostFragment();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        OkHttp.cancel(TAG);
        WebsiteManager.getInstance().unregisterWebsiteChangeListener(this);
    }

    private void initToolBarLayout() {
        mActivity.setSupportActionBar(mToolbar);
        DrawerLayout drawerLayout = mActivity.getDrawerLayout();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                mActivity,
                drawerLayout,
                mToolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        mToolbar.setNavigationIcon(WebsiteManager.getInstance().getWebsiteConfig().getWebsiteLogoRes());

        //双击返回顶部
        DoubleTapEffector.addDoubleTapEffect(mToolbar, () -> {
            if (isPoolPostFragmentVisible()) {
                mPoolPostFragment.scrollToTop();
            } else {
                scrollToTop();
            }
        });
    }

    // 弹出跳转页弹窗
    @OnClick(R.id.iv_page)
    void gotoPage(View view) {
        mPopupPage.showAsDropDown(view);
        mEtGoto.selectAll();
        mEtGoto.post(() -> UIUtils.showSoftInput(mActivity, mEtGoto));
    }

    //搜索
    @OnClick({R.id.iv_search})
    void openSearch() {
//        Intent searchIntent = new Intent(mActivity, SearchActivity.class);
//        startActivityForResult(searchIntent, Constants.SEARCH_CODE);
    }

    private void initPopupPage() {
        mPopupPage = EasyPopup.create()
                .setContentView(mActivity, R.layout.layout_popup_goto_page)
                .setFocusAndOutsideEnable(true)
                .setBackgroundDimEnable(true)
                .setDimValue(0.4f)
                .apply();

        // 当前显示的起始页与终止页
        mTvFrom = mPopupPage.findViewById(R.id.tv_from);
        mTvTo = mPopupPage.findViewById(R.id.tv_to);

        // 页码跳转
        mEtGoto = mPopupPage.findViewById(R.id.et_goto);
        mEtGoto.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                String num = mEtGoto.getText().toString();
                if (!TextUtils.isEmpty(num)) {
                    int newPage = Integer.parseInt(num);
                    if (newPage > 0) {
                        resetAll(newPage);
                        getNewPools(mCurrentPage);
                        changeFromPage(mCurrentPage);
                        changeToPage(mCurrentPage);
                    }
                }
                mPopupPage.dismiss();
            }
            return false;
        });
    }

    private void initSwipeRefreshLayout() {
        mSwipeRefresh.setRefreshing(true);
        //下拉刷新
        mSwipeRefresh.setOnRefreshListener(() -> {
            if (mPoolAdapter.getData().isEmpty()) {
                mPoolAdapter.setEmptyView(R.layout.layout_loading_sakuya, mRvPools);
            }
            getNewPools(mGoToPage);
        });
    }

    private void initRecyclerView() {
        mLayoutManager = new GridLayoutManager(mActivity, 1);
        mRvPools.setLayoutManager(mLayoutManager);

        mPoolAdapter = new RecyclerPoolAdapter();
        mPoolAdapter.bindToRecyclerView(mRvPools);
        mPoolAdapter.setOnLoadMoreListener(this, mRvPools);
        mPoolAdapter.setLoadMoreView(new CustomLoadMoreView());
        mPoolAdapter.setEmptyView(R.layout.layout_loading_sakuya, mRvPools);
        mPoolAdapter.setOnItemClickListener((adapter, view, position) -> {
            PoolListBean poolListBean = mPoolAdapter.getItem(position);
            if (poolListBean != null) {
                String title = getString(R.string.nav_pool) + " #" + poolListBean.id.replaceAll("[^0-9]", "");
                addPoolPostFragment(title, poolListBean.linkToShow);
            }
        });

    }

    private void updateRecyclerViewSpanCount() {
        Activity activity = getActivity();
        if (activity != null && mLayoutManager != null && mRvPools != null) {
            int span = UIUtils.isLandscape(mActivity) ? 2 : 1;
            mLayoutManager.setSpanCount(span);

            int spaceHor = UIUtils.dp2px(mActivity, 6);
            int spaceVer = UIUtils.dp2px(mActivity, 12);
            mRvPools.clearItemDecorations();
            mRvPools.addItemDecoration(new GridDividerItemDecoration(
                    span, GridDividerItemDecoration.VERTICAL, spaceHor, spaceVer, true));

            mPoolAdapter.setPreLoadNumber(span * 5);
        }
    }

    // 滑动加载更多
    @Override
    public void onLoadMoreRequested() {
        String url = WebsiteManager.getInstance().getWebsiteConfig().getPoolUrl(++mCurrentPage, mCurrentSearchName);
        OkHttp.connect(url, TAG, new OkHttp.OkHttpCallback() {
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
                                    .getPoolListList(Jsoup.parse(body1));
                        })
                        .runOn(HandlerFuture.IO.UI)
                        .applyThen(poolListList -> {
                            addMorePoolList(poolListList);
                        });
            }
        }, Request.Priority.IMMEDIATE);
    }

    //加载更多完成后刷新界面
    private void addMorePoolList(final List<PoolListBean> newList) {
        if (!mPoolAdapter.isLoading()) {
            return;
        }

        mPoolAdapter.setEmptyView(R.layout.layout_load_no_pool, mRvPools);
        if (mPoolAdapter.loadMoreDatas(newList)) {
            mPoolAdapter.loadMoreComplete();
            changeToPage(mCurrentPage);
        } else {
            mPoolAdapter.loadMoreEnd(true);
        }
    }

    private void changeFromPage(int page) {
        mTvFrom.setText(String.valueOf(page));
    }

    private void changeToPage(int page) {
        mTvTo.setText(String.valueOf(page));
    }

    private void scrollToTop() {
        int smoothPos = 4 * mLayoutManager.getSpanCount();
        if (mLayoutManager.findLastVisibleItemPosition() > smoothPos) {
            mRvPools.scrollToPosition(smoothPos);
        }
        mRvPools.smoothScrollToPosition(0);
    }

    private void addPoolPostFragment(String title, String linkToShow) {
        mToolbar.setTitle(title);
        mSwipeRefresh.setVisibility(View.GONE);
        mPoolPostFragment = PoolPostFragment.newInstance(linkToShow);
        mFragmentManager.beginTransaction()
                .add(R.id.fl_pool_post, mPoolPostFragment, PoolPostFragment.class.getName())
                .commitAllowingStateLoss();
        mLayoutFragment.setVisibility(View.VISIBLE);
        mIvPage.setVisibility(View.GONE);
    }

    public void removePoolPostFragment() {
        mToolbar.setTitle(R.string.nav_pool);
        mSwipeRefresh.setVisibility(View.VISIBLE);
        mFragmentManager.beginTransaction()
                .remove(mPoolPostFragment)
                .commitAllowingStateLoss();
        mLayoutFragment.setVisibility(View.GONE);
        mIvPage.setVisibility(View.VISIBLE);
        mPoolPostFragment = null;
    }

    public boolean isPoolPostFragmentVisible() {
        return mLayoutFragment.getVisibility() == View.VISIBLE;
    }

    //    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == Constants.SEARCH_CODE) {
//            if (data != null) {
//                mPoolAdapter.clear();
//                mSwipeRefresh.setRefreshing(true);
//                mCurrentPage = 1;
//                mGoToPage = 1;
//                mCurrentSearchName.clear();
//                mIsLoadingMore = false;
//                setLoadingGif();
//
//                OkHttp.getInstance().cancelAll();
//                String searchTag = data.getStringExtra(Constants.SEARCH_TAG);
//                switch (resultCode) {
//                    case Constants.SEARCH_CODE_CHINESE:
//                        getNameFromBaidu(searchTag);
//                        break;
//                    case Constants.SEARCH_CODE_TAGS:
//                        mCurrentSearchName.addAll(Arrays.asList(searchTag.split(",")));
//                        getNewPools(mCurrentPage);
//                        changeFromPage(mCurrentPage);
//                        changeToPage(mCurrentPage);
//                        break;
//                    case Constants.SEARCH_CODE_ID:
//                        mCurrentSearchName.add("id:" + searchTag);
//                        getNewPools(mCurrentPage);
//                        changeFromPage(mCurrentPage);
//                        changeToPage(mCurrentPage);
//                }
//            }
//        }
//    }
//

    /**
     * 初始化所有数据，清空adapter，以便加载新内容
     *
     * @param startPage 加载的起始页
     */
    private void resetAll(int startPage) {
        OkHttp.cancel(TAG);
        mPoolAdapter.setEmptyView(R.layout.layout_loading_sakuya, mRvPools);
        mPoolAdapter.setNewData(null);
        mSwipeRefresh.setRefreshing(true);
        mCurrentPage = startPage;
        mGoToPage = startPage;
    }

    private void getNewPools(int page) {
        if (WebsiteManager.getInstance().getWebsiteConfig().hasPool()) {
            String url = WebsiteManager.getInstance().getWebsiteConfig().getPoolUrl(page, mCurrentSearchName);
            OkHttp.connect(url, TAG, new OkHttp.OkHttpCallback() {
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
                                        .getPoolListList(Jsoup.parse(body1));
                            })
                            .runOn(HandlerFuture.IO.UI)
                            .applyThen(poolListList -> {
                                refreshPoolList(poolListList);
                            });
                }
            }, Request.Priority.IMMEDIATE);
        } else {
            refreshPoolList(new ArrayList<>());
        }
    }

    //搜索新内容或下拉刷新完成后刷新界面
    private void refreshPoolList(List<PoolListBean> newList) {
        if (!mSwipeRefresh.isRefreshing()) {
            return;
        }

        mPoolAdapter.setEmptyView(R.layout.layout_load_no_pool, mRvPools);
        if (mPoolAdapter.refreshDatas(newList)) {
            scrollToTop();
        } else if (mPoolAdapter.getData().isEmpty()) {
            getNoData();
        }
        mSwipeRefresh.setRefreshing(false);
    }

    //搜所无结果
    private void getNoData() {
        mPoolAdapter.setNewData(null);
        mSwipeRefresh.setRefreshing(false);
        SoundHelper.getInstance().playLoadNothingSound(getActivity());
    }

    //访问网络失败
    private void checkNetwork() {
        mSwipeRefresh.setRefreshing(false);
        if (mPoolAdapter.getData().isEmpty()) {
            mPoolAdapter.setEmptyView(R.layout.layout_load_no_network, mRvPools);
            SoundHelper.getInstance().playLoadNoNetworkSound(getActivity());
        } else {
            mPoolAdapter.loadMoreFail();
        }
    }

    @Override
    public void onWebsiteChanged(String baseUrl) {
        mToolbar.setNavigationIcon(WebsiteManager.getInstance().getWebsiteConfig().getWebsiteLogoRes());
        if (isPoolPostFragmentVisible()) {
            removePoolPostFragment();
        }
        resetAll(1);
        getNewPools(mCurrentPage);
        changeFromPage(mCurrentPage);
        changeToPage(mCurrentPage);
    }

    public static PoolFragment newInstance() {
        PoolFragment fragment = new PoolFragment();
        return fragment;
    }

}
