package com.ess.konachan.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.ess.konachan.R;
import com.ess.konachan.adapter.RecyclerPoolAdapter;
import com.ess.konachan.bean.MsgBean;
import com.ess.konachan.bean.PoolListBean;
import com.ess.konachan.global.Constants;
import com.ess.konachan.global.GlideConfig;
import com.ess.konachan.http.OkHttp;
import com.ess.konachan.http.ParseHtml;
import com.ess.konachan.ui.activity.MainActivity;
import com.ess.konachan.utils.UIUtils;
import com.ess.konachan.view.GridDividerItemDecoration;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.ess.konachan.http.ParseHtml.getPoolList;

public class PoolFragment extends Fragment {

    private final static long LOAD_MORE_INTERVAL = 5000;

    private MainActivity mActivity;
    private FragmentManager mFragmentManager;

    private View mRootView;
    private Toolbar mToolbar;
    private SwipeRefreshLayout mSwipeRefresh;
    private FrameLayout mLayoutFragment;
    private PoolPostFragment mPoolPostFragment;
    private RecyclerView mRvPools;
    private LinearLayoutManager mLayoutManager;
    private RecyclerPoolAdapter mPoolAdapter;
    private View mLayoutLoadResult;
    private ImageView mIvLoadFailed;
    private ImageView mIvLoading;

    private int mCurrentPage;
    private String mCurrentSearchName;
    private boolean mIsLoadingMore = false;
    private boolean mLoadMoreAgain = true;

    private Handler mHandler = new Handler();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (MainActivity) context;
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
        if (mPoolPostFragment != null) {
            mFragmentManager.putFragment(outState, PoolPostFragment.class.getName(), mPoolPostFragment);
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mPoolPostFragment = (PoolPostFragment) mFragmentManager.getFragment(savedInstanceState,
                    PoolPostFragment.class.getName());
            if (mPoolPostFragment != null) {
                removePoolPostFragment();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mFragmentManager = getChildFragmentManager();
        mRootView = inflater.inflate(R.layout.fragment_pool, container, false);
        initToolBarLayout();
        initSwipeRefreshLayout();
        initPoolPostFragment();
        initRecyclerView();
        initLoadingView();
        mCurrentPage = 1;
        getNewPools(mCurrentPage);
        return mRootView;
    }

    private long lastClickTime = 0;

    private void initToolBarLayout() {
        mToolbar = (Toolbar) mRootView.findViewById(R.id.tool_bar);
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
        mToolbar.setNavigationIcon(R.drawable.ic_nav_drawer);

        //双击返回顶部
        mToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastClickTime <= 0) {
                    lastClickTime = System.currentTimeMillis();
                } else {
                    long currentClickTime = System.currentTimeMillis();
                    if (currentClickTime - lastClickTime < 500) {
                        if (isPoolPostFragmentVisible()) {
                            mPoolPostFragment.scrollToTop();
                        } else {
                            scrollToTop();
                        }
                    } else {
                        lastClickTime = currentClickTime;
                    }
                }
            }
        });

        //搜索
        ImageView ivSearch = (ImageView) mRootView.findViewById(R.id.iv_search);
        ivSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent searchIntent = new Intent(mActivity, SearchActivity.class);
//                startActivityForResult(searchIntent, Constants.SEARCH_CODE);
            }
        });
    }

    private void initSwipeRefreshLayout() {
        mSwipeRefresh = (SwipeRefreshLayout) mRootView.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefresh.setRefreshing(true);
        //下拉刷新
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getNewPools(1);
                if (mPoolAdapter.getPoolList().isEmpty()) {
                    setLoadingGif();
                }
            }
        });
    }

    private void initPoolPostFragment() {
        mLayoutFragment = (FrameLayout) mRootView.findViewById(R.id.fl_pool_post);
    }

    private void initRecyclerView() {
        mRvPools = (RecyclerView) mRootView.findViewById(R.id.rv_pool);
        mLayoutManager = new LinearLayoutManager(mActivity);
        mRvPools.setLayoutManager(mLayoutManager);

        ArrayList<PoolListBean> poolList = new ArrayList<>();
        mPoolAdapter = new RecyclerPoolAdapter(mActivity, poolList);
        mRvPools.setAdapter(mPoolAdapter);
        mPoolAdapter.setOnItemClickListener(new RecyclerPoolAdapter.OnItemClickListener() {
            @Override
            public void onLoadPostsOfPool(String id, String linkToShow) {
                String title = getString(R.string.nav_pool) + " #" + id.substring(1);
                addPoolPostFragment(title, linkToShow);
            }
        });

        int space = UIUtils.dp2px(mActivity, 10);
        mRvPools.addItemDecoration(new GridDividerItemDecoration(
                1, GridDividerItemDecoration.VERTICAL, space, true));

        //滑动加载更多
        mRvPools.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastVisiblePosition = mLayoutManager.findLastVisibleItemPosition();
                int totalCount = mLayoutManager.getItemCount();
                if (totalCount != 1 && lastVisiblePosition >= totalCount - 6
                        && !mIsLoadingMore && mLoadMoreAgain) {
                    mIsLoadingMore = true;
                    mPoolAdapter.changeToLoadMoreState();
                    loadMore();
                    Log.i("rrr", "load more");
                }
            }
        });
    }

    private void initLoadingView() {
        mLayoutLoadResult = mRootView.findViewById(R.id.layout_load_result);

        mIvLoading = (ImageView) mRootView.findViewById(R.id.iv_loading);
        GlideConfig.getInstance().loadGif(mActivity, R.drawable.gif_loading, mIvLoading);

        mIvLoadFailed = (ImageView) mRootView.findViewById(R.id.iv_load_failed);
        GlideConfig.getInstance().loadImage(mActivity, R.drawable.img_load_failed, mIvLoadFailed);

        setLoadingGif();
    }

    private void setLoadingGif() {
        mIvLoading.setVisibility(View.VISIBLE);
        mIvLoadFailed.setVisibility(View.GONE);
        mLayoutLoadResult.setVisibility(View.VISIBLE);
    }

    private void setLoadFailedImage() {
        mIvLoading.setVisibility(View.GONE);
        mIvLoadFailed.setVisibility(View.VISIBLE);
        mLayoutLoadResult.setVisibility(View.VISIBLE);
    }

    private void scrollToTop() {
        int smoothPos = 4;
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
    }

    public void removePoolPostFragment() {
        mToolbar.setTitle(R.string.nav_pool);
        mSwipeRefresh.setVisibility(View.VISIBLE);
        mFragmentManager.beginTransaction()
                .remove(mPoolPostFragment)
                .commitAllowingStateLoss();
        mLayoutFragment.setVisibility(View.GONE);
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
//                if (!mSwipeRefresh.isRefreshing()) {
//                    mSwipeRefresh.setRefreshing(true);
//                }
//                mCurrentPage = 1;
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
//                        break;
//                    case Constants.SEARCH_CODE_ID:
//                        mCurrentSearchName.add("id:" + searchTag);
//                        getNewPools(mCurrentPage);
//                }
//            }
//        }
//    }
//
    private void getNewPools(int page) {
        String url = OkHttp.getPoolUrl(mActivity, page, mCurrentSearchName);
        OkHttp.getInstance().connect(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (OkHttp.isNetworkProblem(e)) {
                    checkNetwork();
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String html = response.body().string();
                ArrayList<PoolListBean> poolList = getPoolList(mActivity, html);
                refreshPoolList(poolList);
                if (poolList.isEmpty()) {
                    getNoData();
                }
                response.close();
            }
        });
    }

    //搜索新内容或下拉刷新完成后刷新界面
    private void refreshPoolList(final ArrayList<PoolListBean> newList) {
        if (!mSwipeRefresh.isRefreshing()) {
            return;
        }

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!newList.isEmpty()) {
                    mLayoutLoadResult.setVisibility(View.GONE);
                    mPoolAdapter.refreshDatas(newList);
                    scrollToTop();
                } else if (mPoolAdapter.getPoolList().isEmpty()) {
                    setLoadFailedImage();
                }
                mSwipeRefresh.setRefreshing(false);
            }
        });
    }

    //滑动加载更多
    private void loadMore() {
        String url = OkHttp.getPoolUrl(mActivity, ++mCurrentPage, mCurrentSearchName);
        OkHttp.getInstance().connect(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (OkHttp.isNetworkProblem(e)) {
                    checkNetwork();
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String html = response.body().string();
                addMorePoolList(ParseHtml.getPoolList(mActivity, html));
                response.close();
            }
        });
    }

    //加载更多完成后刷新界面
    private void addMorePoolList(final ArrayList<PoolListBean> newList) {
        if (!mIsLoadingMore) {
            return;
        }

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPoolAdapter.loadMoreDatas(newList);
                mPoolAdapter.changeToNormalState();
                if (newList.isEmpty()) {
                    Toast.makeText(mActivity, R.string.no_more_load, Toast.LENGTH_SHORT).show();
                } else {
                    mIsLoadingMore = false;
                }
            }
        });
    }

    //更换浏览模式后收到的通知，obj 为 null
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void toggleSearchMode(MsgBean msgBean) {
        if (msgBean.msg.equals(Constants.TOGGLE_SEARCH_MODE)) {
            if (isPoolPostFragmentVisible()) {
                removePoolPostFragment();
            }
            mPoolAdapter.clear();
            if (!mSwipeRefresh.isRefreshing()) {
                mSwipeRefresh.setRefreshing(true);
            }
            mCurrentPage = 1;
            setLoadingGif();
            getNewPools(mCurrentPage);
        }
    }

    //搜所无结果
    private void getNoData() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPoolAdapter.clear();
                mSwipeRefresh.setRefreshing(false);
                setLoadFailedImage();
            }
        });
    }

    //访问网络失败
    private void checkNetwork() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mActivity, R.string.check_network, Toast.LENGTH_SHORT).show();
                mSwipeRefresh.setRefreshing(false);
                mIsLoadingMore = false;
                mLoadMoreAgain = false;
                if (mPoolAdapter.getPoolList().isEmpty()) {
                    setLoadFailedImage();
                }

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mLoadMoreAgain = true;
                    }
                }, LOAD_MORE_INTERVAL);
            }
        });
    }

    public static PoolFragment newInstance() {
        PoolFragment fragment = new PoolFragment();
        return fragment;
    }
}
