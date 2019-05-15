package com.ess.anime.wallpaper.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.adapter.RecyclerCommentAdapter;
import com.ess.anime.wallpaper.bean.CommentBean;
import com.ess.anime.wallpaper.bean.ThumbBean;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.http.OkHttp;
import com.ess.anime.wallpaper.http.parser.HtmlParserFactory;
import com.ess.anime.wallpaper.ui.activity.ImageDetailActivity;
import com.ess.anime.wallpaper.ui.view.GridDividerItemDecoration;
import com.ess.anime.wallpaper.utils.UIUtils;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class CommentFragment extends BaseFragment {

    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefresh;
    @BindView(R.id.rv_comment)
    RecyclerView mRvComment;

    private ImageDetailActivity mActivity;
    private ThumbBean mThumbBean;
    private Call mCommentCall;
    private RecyclerCommentAdapter mCommentAdapter;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = (ImageDetailActivity) context;
    }

    @Override
    int layoutRes() {
        return R.layout.fragment_comment;
    }

    @Override
    void init(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mThumbBean = savedInstanceState.getParcelable(Constants.THUMB_BEAN);
        } else {
            mThumbBean = mActivity.getThumbBean();
        }
        initView();
        initRecyclerView();
        getCommentList();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // 防止软件进入后台过久被系统回收导致切换回来时产生空指针异常
        outState.putParcelable(Constants.THUMB_BEAN, mThumbBean);
    }

    private void initView() {
        mSwipeRefresh.setOnRefreshListener(this::getCommentList);
        mSwipeRefresh.setRefreshing(true);
    }

    private void initRecyclerView() {
        mRvComment.setLayoutManager(new LinearLayoutManager(mActivity));
        mCommentAdapter = new RecyclerCommentAdapter();
        mRvComment.setAdapter(mCommentAdapter);

        int spaceHor = UIUtils.dp2px(mActivity, 5);
        int spaceVer = UIUtils.dp2px(mActivity, 10);
        mRvComment.addItemDecoration(new GridDividerItemDecoration(
                1, GridDividerItemDecoration.VERTICAL, spaceHor, spaceVer, true));
    }

    // 显示评论
    private void showComments(List<CommentBean> commentList) {
        if (mCommentAdapter.getEmptyView() == null) {
            mCommentAdapter.setEmptyView(getEmptyView());
        }
        mCommentAdapter.setNewData(commentList);
    }

    private View getEmptyView() {
        TextView tvEmpty = new TextView(mActivity);
        tvEmpty.setText(R.string.comment_no_comments);
        tvEmpty.setTextColor(ResourcesCompat.getColor(
                getResources(), R.color.color_text_unselected, null));
        tvEmpty.setTextSize(18);
        tvEmpty.setGravity(Gravity.CENTER);
        return tvEmpty;
    }

    // 获取评论列表
    private void getCommentList() {
        mCommentCall = OkHttp.getInstance().connect(mThumbBean.linkToShow, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (OkHttp.isNetworkProblem(e)) {
                    getCommentList();
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String html = response.body().string();
                    List<CommentBean> commentList = HtmlParserFactory.createParser(mActivity, html).getCommentList();
                    setCommentList(commentList);
                } else {
                    getCommentList();
                }
                response.close();
            }
        });
    }

    // 获取到评论列表后刷新界面
    private void setCommentList(final List<CommentBean> commentList) {
        mActivity.runOnUiThread(() -> {
            if (!mActivity.isFinishing() && !mActivity.isDestroyed()) {
                showComments(commentList);
                mSwipeRefresh.setRefreshing(false);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mCommentCall.cancel();
    }

}
