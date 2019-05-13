package com.ess.anime.wallpaper.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.core.content.res.ResourcesCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.adapter.RecyclerCommentAdapter;
import com.ess.anime.wallpaper.bean.CommentBean;
import com.ess.anime.wallpaper.bean.ThumbBean;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.http.OkHttp;
import com.ess.anime.wallpaper.http.parser.HtmlParserFactory;
import com.ess.anime.wallpaper.ui.activity.ImageDetailActivity;
import com.ess.anime.wallpaper.utils.UIUtils;
import com.ess.anime.wallpaper.ui.view.GridDividerItemDecoration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class CommentFragment extends Fragment {

    private ImageDetailActivity mActivity;
    private ThumbBean mThumbBean;

    private View mRootView;
    private SwipeRefreshLayout mSwipeRefresh;
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
        initRecyclerView();
        getCommentList();
        return mRootView;
    }

    private void initView() {
        mSwipeRefresh = mRootView.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getCommentList();
            }
        });
        mSwipeRefresh.setRefreshing(true);
    }


    private void initRecyclerView() {
        RecyclerView rvComment = mRootView.findViewById(R.id.rv_comment);
        rvComment.setLayoutManager(new LinearLayoutManager(mActivity));

        ArrayList<CommentBean> commentList = new ArrayList<>();
        mCommentAdapter = new RecyclerCommentAdapter(commentList);
        rvComment.setAdapter(mCommentAdapter);

        int spaceHor = UIUtils.dp2px(mActivity, 5);
        int spaceVer = UIUtils.dp2px(mActivity, 10);
        rvComment.addItemDecoration(new GridDividerItemDecoration(
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
            public void onFailure(Call call, IOException e) {
                if (OkHttp.isNetworkProblem(e)) {
                    getCommentList();
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
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
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!mActivity.isFinishing() && !mActivity.isDestroyed()) {
                    showComments(commentList);
                    mSwipeRefresh.setRefreshing(false);
                }
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
