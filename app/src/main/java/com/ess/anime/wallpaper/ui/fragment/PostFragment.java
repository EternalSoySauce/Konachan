package com.ess.anime.wallpaper.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.ess.anime.wallpaper.http.parser.HtmlParser;
import com.ess.anime.wallpaper.http.parser.HtmlParserFactory;
import com.ess.anime.wallpaper.model.helper.SoundHelper;
import com.ess.anime.wallpaper.ui.activity.MainActivity;
import com.ess.anime.wallpaper.ui.activity.SearchActivity;
import com.ess.anime.wallpaper.utils.FileUtils;
import com.ess.anime.wallpaper.utils.UIUtils;
import com.ess.anime.wallpaper.view.CustomLoadMoreView;
import com.ess.anime.wallpaper.view.GridDividerItemDecoration;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.zyyoona7.popup.EasyPopup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class PostFragment extends Fragment implements BaseQuickAdapter.RequestLoadMoreListener {

    private MainActivity mActivity;

    private View mRootView;
    private FloatingActionMenu mFloatingMenu;
    private SwipeRefreshLayout mSwipeRefresh;
    private RecyclerView mRvPosts;
    private GridLayoutManager mLayoutManager;
    private RecyclerPostAdapter mPostAdapter;
    private EasyPopup mPopupPage;
    private TextView mTvFrom;
    private TextView mTvTo;
    private EditText mEtGoto;

    private int mCurrentPage;  // 当前页码
    private int mGoToPage;  // 跳转到的起始页码
    private String mCurrentTag;   // 当前正在搜索的tag
    private ArrayList<String> mCurrentTagList;

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
        initPopupPage();
        initSwipeRefreshLayout();
        initRecyclerView();
        initFloatingButton();
        mCurrentPage = 1;
        mGoToPage = 1;
        mCurrentTag = "";
        mCurrentTagList = new ArrayList<>();
        getNewPosts(mCurrentPage);
        changeFromPage(mCurrentPage);
        changeToPage(mCurrentPage);
        return mRootView;
    }

    private long lastClickTime = 0;

    private void initToolBarLayout() {
        Toolbar toolbar = mRootView.findViewById(R.id.tool_bar);
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

        // 弹出跳转页弹窗
        ImageView ivPage = mRootView.findViewById(R.id.iv_page);
        ivPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupPage.showAsDropDown(v);
                mEtGoto.selectAll();
                mEtGoto.post(new Runnable() {
                    @Override
                    public void run() {
                        UIUtils.showSoftInput(mActivity, mEtGoto);
                    }
                });
            }
        });

        //搜索
        ImageView ivSearch = mRootView.findViewById(R.id.iv_search);
        ivSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFloatingMenu.close(true);
                Intent searchIntent = new Intent(mActivity, SearchActivity.class);
                startActivityForResult(searchIntent, Constants.SEARCH_CODE);
            }
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
        mEtGoto.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    String num = mEtGoto.getText().toString();
                    if (!TextUtils.isEmpty(num)) {
                        int newPage = Integer.parseInt(num);
                        if (newPage > 0) {
                            resetAll(newPage);
                            getNewPosts(mCurrentPage);
                            changeFromPage(mCurrentPage);
                            changeToPage(mCurrentPage);
                        }
                    }
                    mPopupPage.dismiss();
                }
                return false;
            }
        });
    }

    private void initSwipeRefreshLayout() {
        mSwipeRefresh = mRootView.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefresh.setRefreshing(true);
        //下拉刷新
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getNewPosts(mGoToPage);
                if (mPostAdapter.getData().isEmpty()) {
                    mPostAdapter.setEmptyView(R.layout.layout_loading, mRvPosts);
                }
            }
        });
    }

    private void initRecyclerView() {
        int span = 2;
        mRvPosts = mRootView.findViewById(R.id.rv_post);
        mLayoutManager = new GridLayoutManager(mActivity, span);
        mRvPosts.setLayoutManager(mLayoutManager);

        mPostAdapter = new RecyclerPostAdapter();
        mPostAdapter.setOnItemClickListener(new RecyclerPostAdapter.OnItemClickListener() {
            @Override
            public void onViewDetails() {
                mFloatingMenu.close(true);
            }
        });
        mPostAdapter.setOnLoadMoreListener(this, mRvPosts);
        mPostAdapter.setPreLoadNumber(10);
        mPostAdapter.setLoadMoreView(new CustomLoadMoreView());
        mPostAdapter.setEmptyView(R.layout.layout_loading, mRvPosts);
        mRvPosts.setAdapter(mPostAdapter);

        int spaceHor = UIUtils.dp2px(mActivity, 5);
        int spaceVer = UIUtils.dp2px(mActivity, 10);
        mRvPosts.addItemDecoration(new GridDividerItemDecoration(
                span, GridDividerItemDecoration.VERTICAL, spaceHor, spaceVer, true));

        // 滑动时隐藏fab
        mRvPosts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!mPostAdapter.getData().isEmpty()) {
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

    // 滑动加载更多
    @Override
    public void onLoadMoreRequested() {
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
                    addMoreThumbList(HtmlParserFactory.createParser(mActivity, html).getThumbList());
                } else {
                    checkNetwork();
                }
                response.close();
            }
        });
    }

    private void initFloatingButton() {
        mFloatingMenu = mRootView.findViewById(R.id.floating_action_menu);

        FloatingActionButton fabHome = mRootView.findViewById(R.id.fab_home);
        fabHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFloatingMenu.close(true);
                onActivityResult(Constants.SEARCH_CODE, Constants.SEARCH_CODE_HOME, new Intent());
            }
        });

        FloatingActionButton fabRandom = mRootView.findViewById(R.id.fab_random);
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

    private void changeFromPage(int page) {
        mTvFrom.setText(String.valueOf(page));
    }

    private void changeToPage(int page) {
        mTvTo.setText(String.valueOf(page));
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
                resetAll(1);
                mCurrentTagList.clear();

                String searchTag = data.getStringExtra(Constants.SEARCH_TAG);
                mCurrentTag = "#" + searchTag;
                switch (resultCode) {
                    case Constants.SEARCH_CODE_TAGS:
                        mCurrentTagList.addAll(Arrays.asList(searchTag.split(",|，")));
                        getNewPosts(mCurrentPage);
                        changeFromPage(mCurrentPage);
                        changeToPage(mCurrentPage);
                        break;
                    case Constants.SEARCH_CODE_ID:
                        mCurrentTag = "#id:" + searchTag;
                        mCurrentTagList.add("id:" + searchTag);
                        getNewPosts(mCurrentPage);
                        changeFromPage(mCurrentPage);
                        changeToPage(mCurrentPage);
                        break;
                    case Constants.SEARCH_CODE_CHINESE:
                        getNameFromBaidu(searchTag);
                        break;
                    case Constants.SEARCH_CODE_ADVANCED:
                        String[] tags = searchTag.split(" ");
                        mCurrentTagList.addAll(Arrays.asList(tags));
                        getNewPosts(mCurrentPage);
                        changeFromPage(mCurrentPage);
                        changeToPage(mCurrentPage);
                        break;
                    case Constants.SEARCH_CODE_HOME:
                        mCurrentTag = "";
                        getNewPosts(mCurrentPage);
                        changeFromPage(mCurrentPage);
                        changeToPage(mCurrentPage);
                        break;
                    case Constants.SEARCH_CODE_RANDOM:
                        mCurrentTagList.add(searchTag);
                        getNewPosts(mCurrentPage);
                        changeFromPage(mCurrentPage);
                        changeToPage(mCurrentPage);
                        break;
                }
            }
        }
    }


    /**
     * 初始化所有数据，清空adapter，以便加载新内容
     *
     * @param startPage 加载的起始页
     */
    private void resetAll(int startPage) {
        OkHttp.getInstance().cancelAll();
        mPostAdapter.setEmptyView(R.layout.layout_loading, mRvPosts);
        mPostAdapter.setNewData(null);
        mSwipeRefresh.setRefreshing(true);
        mCurrentPage = startPage;
        mGoToPage = startPage;
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
                    List<ThumbBean> thumbList = HtmlParserFactory.createParser(mActivity, html).getThumbList();
                    refreshThumbList(thumbList);
                } else {
                    checkNetwork();
                }
                response.close();
            }
        });
    }

    //搜索新内容或下拉刷新完成后刷新界面
    private void refreshThumbList(final List<ThumbBean> newList) {
        if (!mSwipeRefresh.isRefreshing()) {
            return;
        }

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPostAdapter.setEmptyView(R.layout.layout_load_nothing, mRvPosts);
                if (mPostAdapter.refreshDatas(newList)) {
                    scrollToTop();
                } else if (mPostAdapter.getData().isEmpty()) {
                    getNoData();
                }
                mSwipeRefresh.setRefreshing(false);
            }
        });
    }

    //加载更多完成后刷新界面
    private void addMoreThumbList(final List<ThumbBean> newList) {
        if (!mPostAdapter.isLoading()) {
            return;
        }

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPostAdapter.setEmptyView(R.layout.layout_load_nothing, mRvPosts);
                if (mPostAdapter.loadMoreDatas(newList)) {
                    mPostAdapter.loadMoreComplete();
                    changeToPage(mCurrentPage);
                } else {
                    mPostAdapter.loadMoreEnd();
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
                    String name = HtmlParser.getNameFromBaidu(html);
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
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                changeFromPage(mCurrentPage);
                                changeToPage(mCurrentPage);
                            }
                        });
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
            List<ThumbBean> thumbList = mPostAdapter.getData();
            for (ThumbBean thumbBean : thumbList) {
                if (thumbBean.checkImageBelongs(imageBean)) {
                    if (thumbBean.imageBean == null) {
                        thumbBean.imageBean = imageBean;
                        thumbBean.checkToReplacePostData();
                        String url = imageBean.posts[0].sampleUrl;
                        if (FileUtils.isImageType(url) && !mActivity.isDestroyed()) {
                            GlideApp.with(mActivity)
                                    .load(MyGlideModule.makeGlideUrl(url))
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
                mPostAdapter.setNewData(null);
                mSwipeRefresh.setRefreshing(false);
                SoundHelper.getInstance().playLoadNothingSound(getActivity());
            }
        });
    }

    //访问网络失败
    private void checkNetwork() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSwipeRefresh.setRefreshing(false);
                if (mPostAdapter.getData().isEmpty()) {
                    mPostAdapter.setEmptyView(R.layout.layout_load_no_network, mRvPosts);
                    SoundHelper.getInstance().playLoadNoNetworkSound(getActivity());
                } else {
                    mPostAdapter.loadMoreFail();
                }
            }
        });
    }

    //切换搜图网站后收到的通知，obj 为 null
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void changeBaseUrl(MsgBean msgBean) {
        if (msgBean.msg.equals(Constants.CHANGE_BASE_URL)) {
            resetAll(1);
            getNewPosts(mCurrentPage);
            changeFromPage(mCurrentPage);
            changeToPage(mCurrentPage);
        }
    }

    public static PostFragment newInstance() {
        PostFragment fragment = new PostFragment();
        return fragment;
    }
}
