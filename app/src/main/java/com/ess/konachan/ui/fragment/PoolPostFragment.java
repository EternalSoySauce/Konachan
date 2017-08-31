package com.ess.konachan.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.ess.konachan.R;
import com.ess.konachan.adapter.RecyclerPostAdapter;
import com.ess.konachan.bean.ImageBean;
import com.ess.konachan.bean.MsgBean;
import com.ess.konachan.bean.ThumbBean;
import com.ess.konachan.global.Constants;
import com.ess.konachan.http.OkHttp;
import com.ess.konachan.http.ParseHtml;
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

public class PoolPostFragment extends Fragment {

    private View mRootView;
    private SwipeRefreshLayout mSwipeRefresh;
    private RecyclerView mRvPosts;
    private RecyclerPostAdapter mPostAdapter;
    private String mLinkToShow;

    private Call mCall;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCall.cancel();
        mPostAdapter.cancelAll();
        mSwipeRefresh.setRefreshing(false);
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Constants.LINK_TO_SHOW, mLinkToShow);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mLinkToShow = savedInstanceState.getString(Constants.LINK_TO_SHOW);
        }
        mRootView = inflater.inflate(R.layout.fragment_pool_post, container, false);
        initSwipeRefreshLayout();
        initRecyclerView();
        loadPosts();
        return mRootView;
    }

    private void initSwipeRefreshLayout() {
        mSwipeRefresh = (SwipeRefreshLayout) mRootView.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        mSwipeRefresh.setEnabled(false);
        mSwipeRefresh.setRefreshing(true);
    }

    private void initRecyclerView() {
        mRvPosts = (RecyclerView) mRootView.findViewById(R.id.rv_pool_post);
        final GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return position == layoutManager.getItemCount() - 1 ? 2 : 1;
            }
        });
        mRvPosts.setLayoutManager(layoutManager);

        ArrayList<ThumbBean> thumbList = new ArrayList<>();
        mPostAdapter = new RecyclerPostAdapter(getActivity(), thumbList);
        mRvPosts.setAdapter(mPostAdapter);

        int spaceHor = UIUtils.dp2px(getActivity(), 5);
        int spaceVer = UIUtils.dp2px(getActivity(), 10);
        mRvPosts.addItemDecoration(new GridDividerItemDecoration(
                2, GridDividerItemDecoration.VERTICAL, spaceHor, spaceVer, true));
    }

    public void loadPosts() {
        if (!mSwipeRefresh.isRefreshing()) {
            mSwipeRefresh.setRefreshing(true);
        }
        mCall = OkHttp.getInstance().connect(mLinkToShow, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (OkHttp.isNetworkProblem(e)) {
                    mCall = OkHttp.getInstance().connect(mLinkToShow, this);
                }
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String html = response.body().string();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mSwipeRefresh.setRefreshing(false);
                            mPostAdapter.refreshDatas(ParseHtml.getThumbListOfPool(html));
                        }
                    });
                }
                response.close();
            }
        });
    }

    public void scrollToTop() {
        mRvPosts.scrollToPosition(0);
    }

    //获取到图片详细信息后收到的通知，obj 为 Json (String)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void setImageBean(MsgBean msgBean) {
        if (msgBean.msg.equals(Constants.GET_IMAGE_DETAIL)) {
            String json = (String) msgBean.obj;
            ImageBean imageBean = ImageBean.getImageDetailFromJson(json);
            ArrayList<ThumbBean> thumbList = mPostAdapter.getThumbList();
            for (ThumbBean thumbBean : thumbList) {
                if (thumbBean.thumbUrl.equals(imageBean.posts[0].previewUrl)) {
                    if (thumbBean.imageBean == null) {
                        thumbBean.imageBean = imageBean;
                        Glide.with(getActivity()).load(imageBean.posts[0].sampleUrl).submit();
                    }
                    break;
                }
            }
        }
    }

    public static PoolPostFragment newInstance(String linkToShow) {
        PoolPostFragment fragment = new PoolPostFragment();
        fragment.mLinkToShow = linkToShow;
        return fragment;
    }
}
