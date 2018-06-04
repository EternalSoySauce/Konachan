package com.ess.anime.wallpaper.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.ess.anime.wallpaper.other.Sound;
import com.ess.anime.wallpaper.ui.activity.MainActivity;
import com.ess.anime.wallpaper.ui.activity.SearchActivity;
import com.ess.anime.wallpaper.utils.UIUtils;
import com.ess.anime.wallpaper.view.GridDividerItemDecoration;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class PostFragment extends Fragment {

    private final static long LOAD_MORE_INTERVAL = 5000;

    private MainActivity mActivity;

    private View mRootView;
    private FloatingActionMenu mFloatingMenu;
    private SwipeRefreshLayout mSwipeRefresh;
    private RecyclerView mRvPosts;
    private GridLayoutManager mLayoutManager;
    private RecyclerPostAdapter mPostAdapter;

    private int mCurrentPage;
    private String mCurrentTag;   // 当前正在搜索的tag
    private ArrayList<String> mCurrentTagList;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_post, container, false);
        initToolBarLayout();
        initSwipeRefreshLayout();
        initRecyclerView();
        initFloatingButton();
        mCurrentPage = 1;
        mCurrentTag = "";
        mCurrentTagList = new ArrayList<>();
        getNewPosts(mCurrentPage);
        return mRootView;
    }

    private long lastClickTime = 0;

    private void initToolBarLayout() {
        Toolbar toolbar = (Toolbar) mRootView.findViewById(R.id.tool_bar);
        mActivity.setSupportActionBar(toolbar);
        DrawerLayout drawerLayout = mActivity.getDrawerLayout();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                mActivity,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        toolbar.setNavigationIcon(R.drawable.ic_nav_drawer);

        //双击返回顶部
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastClickTime <= 0) {
                    lastClickTime = System.currentTimeMillis();
                } else {
                    long currentClickTime = System.currentTimeMillis();
                    if (currentClickTime - lastClickTime < 500) {
                        scrollToTop();
                        mFloatingMenu.close(true);
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
                mFloatingMenu.close(true);
                Intent searchIntent = new Intent(mActivity, SearchActivity.class);
                startActivityForResult(searchIntent, Constants.SEARCH_CODE);
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
                getNewPosts(1);
                if (mPostAdapter.getThumbList().isEmpty()) {
                    mPostAdapter.showLoading();
                }
            }
        });
    }

    private void initRecyclerView() {
        mRvPosts = (RecyclerView) mRootView.findViewById(R.id.rv_post);
        mLayoutManager = new GridLayoutManager(mActivity, 2);
        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return position >= mPostAdapter.getDataListSize() ? 2 : 1;
            }
        });
        mRvPosts.setLayoutManager(mLayoutManager);

        mPostAdapter = new RecyclerPostAdapter(mActivity, new ArrayList<ThumbBean>());
        mPostAdapter.setOnItemClickListener(new RecyclerPostAdapter.OnItemClickListener() {
            @Override
            public void onViewDetails() {
                mFloatingMenu.close(true);
            }
        });
        mPostAdapter.showLoading();
        mRvPosts.setAdapter(mPostAdapter);

        int spaceHor = UIUtils.dp2px(mActivity, 5);
        int spaceVer = UIUtils.dp2px(mActivity, 10);
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

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!mPostAdapter.getThumbList().isEmpty()) {
                    switch (newState) {
                        case RecyclerView.SCROLL_STATE_IDLE:
                            mFloatingMenu.showMenu(true);
                            break;
                        case RecyclerView.SCROLL_STATE_DRAGGING:
                            mFloatingMenu.hideMenu(true);
                            break;
                    }
                }
            }
        });
    }

    private void initFloatingButton() {
        mFloatingMenu = (FloatingActionMenu) mRootView.findViewById(R.id.floating_action_menu);

        FloatingActionButton fabHome = (FloatingActionButton) mRootView.findViewById(R.id.fab_home);
        fabHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFloatingMenu.close(true);
                onActivityResult(Constants.SEARCH_CODE, Constants.SEARCH_CODE_HOME, new Intent());
            }
        });

        FloatingActionButton fabRandom = (FloatingActionButton) mRootView.findViewById(R.id.fab_random);
        fabRandom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFloatingMenu.close(true);
                Intent intent = new Intent();
                intent.putExtra(Constants.SEARCH_TAG, "order:random");
                onActivityResult(Constants.SEARCH_CODE, Constants.SEARCH_CODE_RANDOM, intent);
            }
        });
    }

    private void scrollToTop() {
        int smoothPos = 14;
        if (mLayoutManager.findLastVisibleItemPosition() > smoothPos) {
            mRvPosts.scrollToPosition(smoothPos);
        }
        mRvPosts.smoothScrollToPosition(0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.SEARCH_CODE) {
            if (data != null) {
                mPostAdapter.clear();
                if (!mSwipeRefresh.isRefreshing()) {
                    mSwipeRefresh.setRefreshing(true);
                }
                mCurrentPage = 1;
                mCurrentTagList.clear();
                mIsLoadingMore = false;
                mPostAdapter.showLoading();

                OkHttp.getInstance().cancelAll();
                String searchTag = data.getStringExtra(Constants.SEARCH_TAG);
                mCurrentTag = "#" + searchTag;
                switch (resultCode) {
                    case Constants.SEARCH_CODE_TAGS:
                        mCurrentTagList.addAll(Arrays.asList(searchTag.split(",|，")));
                        getNewPosts(mCurrentPage);
                        break;
                    case Constants.SEARCH_CODE_ID:
                        mCurrentTag = "#id:" + searchTag;
                        mCurrentTagList.add("id:" + searchTag);
                        getNewPosts(mCurrentPage);
                        break;
                    case Constants.SEARCH_CODE_CHINESE:
                        getNameFromBaidu(searchTag);
                        break;
                    case Constants.SEARCH_CODE_ADVANCED:
                        String[] tags = searchTag.split(" ");
                        mCurrentTagList.addAll(Arrays.asList(tags));
                        getNewPosts(mCurrentPage);
                        break;
                    case Constants.SEARCH_CODE_HOME:
                        mCurrentTag = "";
                        getNewPosts(mCurrentPage);
                        break;
                    case Constants.SEARCH_CODE_RANDOM:
                        mCurrentTagList.add(searchTag);
                        getNewPosts(mCurrentPage);
                        break;
                }
            }
        }
    }

    private void getNewPosts(int page) {
        String url = OkHttp.getPostUrl(mActivity, page, mCurrentTagList);
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
                    ArrayList<ThumbBean> thumbList = ParseHtml.getThumbList(html);
                    refreshThumbList(thumbList);
                } else {
                    checkNetwork();
                }
                response.close();
            }
        });
    }

    //搜索新内容或下拉刷新完成后刷新界面
    private void refreshThumbList(final ArrayList<ThumbBean> newList) {
        if (!mSwipeRefresh.isRefreshing()) {
            return;
        }

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!newList.isEmpty()) {
                    mPostAdapter.refreshDatas(newList);
                    scrollToTop();
                } else if (mPostAdapter.getThumbList().isEmpty()) {
                    getNoData();
                }
                mSwipeRefresh.setRefreshing(false);
            }
        });
    }

    //滑动加载更多
    private void loadMore() {
        String url = OkHttp.getPostUrl(mActivity, ++mCurrentPage, mCurrentTagList);
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
                    addMoreThumbList(ParseHtml.getThumbList(html));
                } else {
                    checkNetwork();
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

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPostAdapter.loadMoreDatas(newList);
                mPostAdapter.showNormal();
                if (newList.isEmpty()) {
                    Toast.makeText(mActivity, R.string.no_more_load, Toast.LENGTH_SHORT).show();
                } else {
                    mIsLoadingMore = false;
                }
            }
        });
    }

    private void getNameFromBaidu(String searchTag) {
        String url = Constants.BASE_URL_BAIDU + searchTag;
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
                    String name = ParseHtml.getNameFromBaidu(html);
                    if (!TextUtils.isEmpty(name)) {
                        String tag1 = "~" + name;
                        mCurrentTagList.add(tag1);

                        String[] split = name.split("_");
                        StringBuilder tag2 = new StringBuilder("~");
                        for (int i = split.length - 1; i >= 0; i--) {
                            tag2.append(split[i]).append("_");
                        }
                        tag2.replace(tag2.length() - 1, tag2.length(), "");
                        mCurrentTagList.add(tag2.toString());
                        getNewPosts(mCurrentPage);
                    } else {
                        getNoData();
                    }
                } else {
                    checkNetwork();
                }
                response.close();
            }
        });
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
                        if (!getActivity().isDestroyed()) {
                            GlideApp.with(mActivity)
                                    .load(MyGlideModule.makeGlideUrl(imageBean.posts[0].sampleUrl))
                                    .priority(Priority.HIGH)
                                    .submit();
                        }
                    }
                    break;
                }
            }
        }
    }

    //百度或K站搜所无结果
    private void getNoData() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPostAdapter.clear();
                mSwipeRefresh.setRefreshing(false);
                mPostAdapter.showNoData();
                Sound.getInstance().playLoadNothingSound(getActivity());
            }
        });
    }

    //访问网络失败
    private void checkNetwork() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSwipeRefresh.setRefreshing(false);
                mIsLoadingMore = false;
                mLoadMoreAgain = false;
                if (mPostAdapter.getThumbList().isEmpty()) {
                    mPostAdapter.showNoNetwork();
                    Sound.getInstance().playLoadNoNetworkSound(getActivity());
                } else {
                    Toast.makeText(mActivity, R.string.check_network, Toast.LENGTH_SHORT).show();
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

    //切换搜图网站后收到的通知，obj 为 null
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void changeBaseUrl(MsgBean msgBean) {
        if (msgBean.msg.equals(Constants.CHANGE_BASE_URL)) {
            OkHttp.getInstance().cancelAll();
            mPostAdapter.clear();
            if (!mSwipeRefresh.isRefreshing()) {
                mSwipeRefresh.setRefreshing(true);
            }
            mIsLoadingMore = false;
            mLoadMoreAgain = true;
            mCurrentPage = 1;
            mPostAdapter.showLoading();
            getNewPosts(mCurrentPage);
        }
    }

    public static PostFragment newInstance() {
        PostFragment fragment = new PostFragment();
        return fragment;
    }
}
