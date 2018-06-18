package com.ess.anime.wallpaper.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Priority;
import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.bean.MsgBean;
import com.ess.anime.wallpaper.bean.ThumbBean;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.global.ImageDataHolder;
import com.ess.anime.wallpaper.http.OkHttp;
import com.ess.anime.wallpaper.http.ParseHtml;
import com.ess.anime.wallpaper.other.GlideApp;
import com.ess.anime.wallpaper.other.MyGlideModule;
import com.ess.anime.wallpaper.ui.activity.ImageDetailActivity;
import com.wang.avi.AVLoadingIndicatorView;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class RecyclerPostAdapter extends MultiStateRecyclerAdapter<RecyclerPostAdapter.MyViewHolder> {

    private Activity mActivity;
    private ArrayList<ThumbBean> mThumbList;
    private ArrayList<Call> mCallList = new ArrayList<>();
    private OnItemClickListener mItemClickListener;

    public RecyclerPostAdapter(Activity activity, @NonNull ArrayList<ThumbBean> thumbList) {
        super(activity);
        mActivity = activity;
        mThumbList = thumbList;
        getImageDetail(thumbList);
    }

    @Override
    public int bindLoadMoreLayoutRes() {
        return R.layout.layout_load_more;
    }

    @Override
    public int bindLoadingLayoutRes() {
        return R.layout.layout_loading;
    }

    @Override
    public int bindNoDataLayoutRes() {
        return R.layout.layout_load_nothing;
    }

    @Override
    public int bindNoNetworkLayoutRes() {
        return R.layout.layout_load_no_network;
    }

    @Override
    public int bindNormalLayoutRes() {
        return R.layout.recyclerview_item_post;
    }

    @Override
    public MyViewHolder onCreateViewHolder(View view) {
        return new MyViewHolder(view);
    }

    @Override
    public void onBindLoadMoreHolder(MyViewHolder holder, int layoutPos) {
        holder.indicatorView.smoothToShow();
    }

    @Override
    public void onBindLoadingHolder(MyViewHolder holder, int layoutPos) {
    }

    @Override
    public void onBindNoDataHolder(MyViewHolder holder, int layoutPos) {
    }

    @Override
    public void onBindNoNetworkHolder(MyViewHolder holder, int layoutPos) {
    }

    @Override
    public void onBindNormalHolder(MyViewHolder holder, final int position) {
        final ThumbBean thumbBean = mThumbList.get(position);

        //缩略图
        GlideApp.with(mActivity)
                .load(MyGlideModule.makeGlideUrl(thumbBean.thumbUrl))
                .placeholder(R.drawable.ic_placeholder_post)
                .priority(Priority.HIGH)
                .into(holder.ivThumb);

        //尺寸
        holder.tvSize.setText(thumbBean.realSize);

        //点击进入详细页面
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageDataHolder.setThumbList(mThumbList, position);
                Intent intent = new Intent(mActivity, ImageDetailActivity.class);
                intent.putExtra(Constants.THUMB_BEAN, thumbBean);
                mActivity.startActivity(intent);

                if (mItemClickListener != null) {
                    mItemClickListener.onViewDetails();
                }
            }
        });
    }

    @Override
    public int getDataListSize() {
        return getThumbList().size();
    }

    public ArrayList<ThumbBean> getThumbList() {
        return mThumbList;
    }

    public void loadMoreDatas(ArrayList<ThumbBean> imageList) {
        int position = getDataListSize();
        addDatas(position, imageList);
    }

    public void refreshDatas(ArrayList<ThumbBean> imageList) {
        addDatas(0, imageList);
    }

    private void addDatas(int position, ArrayList<ThumbBean> thumbList) {
        synchronized (this) {
            //删掉更新时因网站新增图片导致thumbList出现的重复项
            thumbList.removeAll(mThumbList);
            mThumbList.addAll(position, thumbList);
            showNormal();
            getImageDetail(thumbList);
            preloadThumbnail(thumbList);
        }
    }

    private void getImageDetail(ArrayList<ThumbBean> thumbList) {
        for (final ThumbBean thumbBean : thumbList) {
            if (thumbBean.imageBean == null) {
                Call call = OkHttp.getInstance().connect(thumbBean.linkToShow, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        if (OkHttp.isNetworkProblem(e)) {
                            Call newCall = OkHttp.getInstance().connect(thumbBean.linkToShow, this);
                            mCallList.add(newCall);
                        }
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String html = response.body().string();
                            String json = ParseHtml.getImageDetailJson(html);
                            // 发送通知到PostFragment, PoolFragment, ImageFragment, DetailFragment
                            EventBus.getDefault().post(new MsgBean(Constants.GET_IMAGE_DETAIL, json));
                        } else {
                            Call newCall = OkHttp.getInstance().connect(thumbBean.linkToShow, this);
                            mCallList.add(newCall);
                        }
                        response.close();
                    }
                });
                mCallList.add(call);
            }
        }
    }

    private void preloadThumbnail(ArrayList<ThumbBean> thumbList) {
        for (ThumbBean thumbBean : thumbList) {
            if (!mActivity.isDestroyed()) {
                GlideApp.with(mActivity)
                        .load(MyGlideModule.makeGlideUrl(thumbBean.thumbUrl))
                        .submit();
            }
        }
    }

    public void clear() {
        mThumbList.clear();
        showNormal();
    }

    public void cancelAll() {
        for (Call call : mCallList) {
            call.cancel();
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivThumb;
        private TextView tvSize;
        private AVLoadingIndicatorView indicatorView;

        public MyViewHolder(android.view.View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.iv_post_thumb);
            tvSize = itemView.findViewById(R.id.tv_size);
            indicatorView = itemView.findViewById(R.id.view_load_more);
        }
    }

    public interface OnItemClickListener {
        //进入图片详细界面时收起fab
        void onViewDetails();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }
}
