package com.ess.wallpaper.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ess.wallpaper.adapter.RecyclerCommentAdapter;
import com.ess.wallpaper.bean.CommentBean;
import com.ess.wallpaper.bean.ThumbBean;
import com.ess.wallpaper.global.Constants;
import com.ess.wallpaper.http.OkHttp;
import com.ess.wallpaper.http.ParseHtml;
import com.ess.wallpaper.ui.activity.ImageDetailActivity;
import com.ess.wallpaper.utils.UIUtils;
import com.ess.wallpaper.view.GridDividerItemDecoration;
import com.ess.wallpaper.R;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class CommentFragment extends Fragment {

    private ImageDetailActivity mActivity;
    private ThumbBean mThumbBean;

    private View mRootView;
    private SwipeRefreshLayout mSwipeRefresh;
    private TextView mTvNoComment;
    private RecyclerCommentAdapter mCommentAdapter;

    private Call mCommentCall;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (ImageDetailActivity) context;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // 防止软件进入后台过久被系统回收导致切换回来时产生空指针异常
        outState.putParcelable(Constants.THUMB_BEAN, mThumbBean);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mThumbBean = savedInstanceState.getParcelable(Constants.THUMB_BEAN);
        } else {
            mThumbBean = mActivity.getThumbBean();
        }
        mRootView = inflater.inflate(R.layout.fragment_comment, container, false);
        initView();
        initNoCommentView();
        initRecyclerView();
        showComments(null);
        return mRootView;
    }

    private void initView() {
        mSwipeRefresh = (SwipeRefreshLayout) mRootView.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getCommentList();
            }
        });

        mSwipeRefresh.setRefreshing(true);
        mSwipeRefresh.getChildAt(0).setVisibility(View.GONE);
        getCommentList();
    }

    private void initNoCommentView() {
        mTvNoComment = (TextView) mRootView.findViewById(R.id.tv_no_comment);
    }

    private void initRecyclerView() {
        RecyclerView rvComment = (RecyclerView) mRootView.findViewById(R.id.rv_comment);
        rvComment.setLayoutManager(new LinearLayoutManager(mActivity));

        ArrayList<CommentBean> commentList = new ArrayList<>();
        mCommentAdapter = new RecyclerCommentAdapter(mActivity, commentList);
        rvComment.setAdapter(mCommentAdapter);

        int spaceHor = UIUtils.dp2px(mActivity, 5);
        int spaceVer = UIUtils.dp2px(mActivity, 10);
        rvComment.addItemDecoration(new GridDividerItemDecoration(
                1, GridDividerItemDecoration.VERTICAL, spaceHor, spaceVer, true));
    }

    // 显示评论
    private void showComments(ArrayList<CommentBean> commentList) {
        mCommentAdapter.getCommentList().clear();
        if (commentList == null || commentList.isEmpty()) {
            mTvNoComment.setVisibility(View.VISIBLE);
        } else {
            mTvNoComment.setVisibility(View.GONE);
            mCommentAdapter.getCommentList().addAll(commentList);
        }
        mCommentAdapter.notifyDataSetChanged();
    }

    // 获取评论列表
    private void getCommentList() {
        mCommentCall = OkHttp.getInstance().connect(mThumbBean.linkToShow, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (OkHttp.isNetworkProblem(e)) {
                    getCommentList();
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String html = response.body().string();
                    ArrayList<CommentBean> commentList = ParseHtml.getCommentList(mActivity, html);
                    setCommentList(commentList);
                } else {
                    getCommentList();
                }
                response.close();
            }
        });
    }

    // 获取到评论列表后刷新界面
    private void setCommentList(final ArrayList<CommentBean> commentList) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showComments(commentList);
                mSwipeRefresh.setRefreshing(false);
                mSwipeRefresh.getChildAt(0).setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mCommentCall.cancel();
    }

    public static CommentFragment newInstance(String title) {
        CommentFragment fragment = new CommentFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.PAGE_TITLE, title);
        fragment.setArguments(bundle);
        return fragment;
    }
}
