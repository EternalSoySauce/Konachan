package com.ess.anime.wallpaper.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.adapter.RecyclerPoolAdapter;
import com.ess.anime.wallpaper.bean.MsgBean;
import com.ess.anime.wallpaper.bean.PoolListBean;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.http.OkHttp;
import com.ess.anime.wallpaper.http.parser.HtmlParserFactory;
import com.ess.anime.wallpaper.model.helper.SoundHelper;
import com.ess.anime.wallpaper.ui.activity.MainActivity;
import com.ess.anime.wallpaper.utils.UIUtils;
import com.ess.anime.wallpaper.ui.view.CustomLoadMoreView;
import com.ess.anime.wallpaper.ui.view.GridDividerItemDecoration;
import com.zyyoona7.popup.EasyPopup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class PoolFragment extends Fragment implements BaseQuickAdapter.RequestLoadMoreListener {

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
    private ImageView mIvPage;
    private EasyPopup mPopupPage;
    private TextView mTvFrom;
    private TextView mTvTo;
    private EditText mEtGoto;

    private int mCurrentPage;   // 当前页码
    private int mGoToPage;   // 跳转到的起始页码
    private String mCurrentSearchName;

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
        initPopupPage();
        initSwipeRefreshLayout();
        initPoolPostFragment();
        initRecyclerView();
        mCurrentPage = 1;
        mGoToPage = 1;
        getNewPools(mCurrentPage);
        changeFromPage(mCurrentPage);
        changeToPage(mCurrentPage);
        return mRootView;
    }

    private long lastClickTime = 0;

    private void initToolBarLayout() {
        mToolbar = mRootView.findViewById(R.id.tool_bar);
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
        mToolbar.setOnClickListener(v -> {
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
        });

        // 弹出跳转页弹窗
        mIvPage = mRootView.findViewById(R.id.iv_page);
        mIvPage.setOnClickListener(v -> {
            mPopupPage.showAsDropDown(v);
            mEtGoto.selectAll();
            mEtGoto.post(new Runnable() {
                @Override
                public void run() {
                    UIUtils.showSoftInput(mActivity, mEtGoto);
                }
            });
        });

        //搜索
        ImageView ivSearch = mRootView.findViewById(R.id.iv_search);
        ivSearch.setOnClickListener(v -> {
//                Intent searchIntent = new Intent(mActivity, SearchActivity.class);
//                startActivityForResult(searchIntent, Constants.SEARCH_CODE);
        });
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
        mSwipeRefresh = mRootView.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefresh.setRefreshing(true);
        //下拉刷新
        mSwipeRefresh.setOnRefreshListener(() -> {
            getNewPools(mGoToPage);
            if (mPoolAdapter.getData().isEmpty()) {
                mPoolAdapter.setEmptyView(R.layout.layout_loading, mRvPools);
            }
        });
    }

    private void initPoolPostFragment() {
        mLayoutFragment = mRootView.findViewById(R.id.fl_pool_post);
    }

    private void initRecyclerView() {
        mRvPools = mRootView.findViewById(R.id.rv_pool);
        mLayoutManager = new LinearLayoutManager(mActivity);
        mRvPools.setLayoutManager(mLayoutManager);

        mPoolAdapter = new RecyclerPoolAdapter();
        mRvPools.setAdapter(mPoolAdapter);
        mPoolAdapter.setOnLoadMoreListener(this, mRvPools);
        mPoolAdapter.setPreLoadNumber(5);
        mPoolAdapter.setLoadMoreView(new CustomLoadMoreView());
        mPoolAdapter.setEmptyView(R.layout.layout_loading, mRvPools);
        mPoolAdapter.setOnItemClickListener((adapter, view, position) -> {
            PoolListBean poolListBean = mPoolAdapter.getItem(position);
            if (poolListBean != null) {
                String title = getString(R.string.nav_pool) + " #" + poolListBean.id.replaceAll("[^0-9]", "");
                addPoolPostFragment(title, poolListBean.linkToShow);
            }
        });

        int space = UIUtils.dp2px(mActivity, 12);
        mRvPools.addItemDecoration(new GridDividerItemDecoration(
                1, GridDividerItemDecoration.VERTICAL, space, true));
    }

    // 滑动加载更多
    @Override
    public void onLoadMoreRequested() {
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
                if (response.isSuccessful()) {
                    String html = response.body().string();
                    addMorePoolList(HtmlParserFactory.createParser(mActivity, html).getPoolList());
                } else {
                    checkNetwork();
                }
                response.close();
            }
        });
    }

    private void changeFromPage(int page) {
        mTvFrom.setText(String.valueOf(page));
    }

    private void changeToPage(int page) {
        mTvTo.setText(String.valueOf(page));
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
        mPoolAdapter.setEmptyView(R.layout.layout_loading, mRvPools);
        mPoolAdapter.setNewData(null);
        mSwipeRefresh.setRefreshing(true);
        mCurrentPage = startPage;
        mGoToPage = startPage;
    }

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
                if (response.isSuccessful()) {
                    String html = response.body().string();
                    List<PoolListBean> poolList = HtmlParserFactory.createParser(mActivity, html).getPoolList();
                    refreshPoolList(poolList);
                } else {
                    checkNetwork();
                }
                response.close();
            }
        });
    }

    //搜索新内容或下拉刷新完成后刷新界面
    private void refreshPoolList(final List<PoolListBean> newList) {
        if (!mSwipeRefresh.isRefreshing()) {
            return;
        }

        mActivity.runOnUiThread(() -> {
            mPoolAdapter.setEmptyView(R.layout.layout_load_nothing, mRvPools);
            if (mPoolAdapter.refreshDatas(newList)) {
                scrollToTop();
            } else if (mPoolAdapter.getData().isEmpty()) {
                getNoData();
            }
            mSwipeRefresh.setRefreshing(false);
        });
    }

    //加载更多完成后刷新界面
    private void addMorePoolList(final List<PoolListBean> newList) {
        if (!mPoolAdapter.isLoading()) {
            return;
        }

        mActivity.runOnUiThread(() -> {
            mPoolAdapter.setEmptyView(R.layout.layout_load_nothing, mRvPools);
            if (mPoolAdapter.loadMoreDatas(newList)) {
                mPoolAdapter.loadMoreComplete();
                changeToPage(mCurrentPage);
            } else {
                mPoolAdapter.loadMoreEnd();
            }
        });
    }

    //搜所无结果
    private void getNoData() {
        mActivity.runOnUiThread(() -> {
            mPoolAdapter.setNewData(null);
            mSwipeRefresh.setRefreshing(false);
            SoundHelper.getInstance().playLoadNothingSound(getActivity());
        });
    }

    //访问网络失败
    private void checkNetwork() {
        mActivity.runOnUiThread(() -> {
            mSwipeRefresh.setRefreshing(false);
            if (mPoolAdapter.getData().isEmpty()) {
                mPoolAdapter.setEmptyView(R.layout.layout_load_no_network, mRvPools);
                SoundHelper.getInstance().playLoadNoNetworkSound(getActivity());
            } else {
                mPoolAdapter.loadMoreFail();
            }
        });
    }

    //切换搜图网站后收到的通知，obj 为 null
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void changeBaseUrl(MsgBean msgBean) {
        if (msgBean.msg.equals(Constants.CHANGE_BASE_URL)) {
            if (isPoolPostFragmentVisible()) {
                removePoolPostFragment();
            }
            resetAll(1);
            getNewPools(mCurrentPage);
            changeFromPage(mCurrentPage);
            changeToPage(mCurrentPage);
        }
    }

    public static PoolFragment newInstance() {
        PoolFragment fragment = new PoolFragment();
        return fragment;
    }
}
