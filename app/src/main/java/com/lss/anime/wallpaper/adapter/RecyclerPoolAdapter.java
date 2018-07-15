package com.lss.anime.wallpaper.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Priority;
import com.lss.anime.wallpaper.R;
import com.lss.anime.wallpaper.bean.PoolListBean;
import com.lss.anime.wallpaper.glide.GlideApp;
import com.lss.anime.wallpaper.glide.MyGlideModule;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;

public class RecyclerPoolAdapter extends MultiStateRecyclerAdapter<RecyclerPoolAdapter.MyViewHolder> {

    private Activity mActivity;
    private ArrayList<PoolListBean> mPoolList;
    private OnItemClickListener mItemClickListener;

    public RecyclerPoolAdapter(Activity activity, @NonNull ArrayList<PoolListBean> poolList) {
        super(activity);
        mActivity = activity;
        mPoolList = poolList;
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
        return R.layout.recyclerview_item_pool;
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
    public void onBindNormalHolder(MyViewHolder holder, int position) {
        final PoolListBean poolListBean = mPoolList.get(position);

        //缩略图
        Object imgUrl = TextUtils.isEmpty(poolListBean.thumbUrl)
                ? R.drawable.ic_placeholder_pool_no_cover
                : MyGlideModule.makeGlideUrl(poolListBean.thumbUrl);
        GlideApp.with(mActivity)
                .load(imgUrl)
                .placeholder(R.drawable.ic_placeholder_pool)
                .priority(Priority.HIGH)
                .into(holder.ivThumb);

        //图集名称
        holder.tvName.setText(poolListBean.name.replace("_", " "));

        //创建者
        String creator = TextUtils.isEmpty(poolListBean.creator)
                ? getContext().getString(R.string.unknown)
                : poolListBean.creator;
        holder.tvCreator.setText(creator);

        //图片数量
        holder.tvPostCount.setText(poolListBean.postCount);

        //创建时间
        holder.tvCreateTime.setText(poolListBean.createTime);

        //上传时间
        String update = TextUtils.isEmpty(poolListBean.updateTime)
                ? getContext().getString(R.string.unknown)
                : mActivity.getString(R.string.pool_updated_time, poolListBean.updateTime);
        holder.tvUpdateTime.setText(update);

        //点击加载图片列表
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onLoadPostsOfPool(poolListBean.id, poolListBean.linkToShow);
                }
            }
        });
    }

    @Override
    public int getDataListSize() {
        return getPoolList().size();
    }

    public ArrayList<PoolListBean> getPoolList() {
        return mPoolList;
    }

    public void loadMoreDatas(ArrayList<PoolListBean> poolList) {
        int position = getDataListSize();
        addDatas(position, poolList);
    }

    public void refreshDatas(ArrayList<PoolListBean> poolList) {
        addDatas(0, poolList);
    }

    private void addDatas(int position, ArrayList<PoolListBean> poolList) {
        synchronized (this) {
            //删掉更新时因网站新增图片导致thumbList出现的重复项
            poolList.removeAll(mPoolList);
            mPoolList.addAll(position, poolList);
            showNormal();
            preloadThumbnail(poolList);
        }
    }

    private void preloadThumbnail(ArrayList<PoolListBean> poolList) {
        for (PoolListBean poolListBean : poolList) {
            if (!mActivity.isDestroyed()) {
                GlideApp.with(mActivity)
                        .load(MyGlideModule.makeGlideUrl(poolListBean.thumbUrl))
                        .submit();
            }
        }
    }

    public void clear() {
        mPoolList.clear();
        showNormal();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivThumb;
        private TextView tvName;
        private TextView tvCreator;
        private TextView tvPostCount;
        private TextView tvCreateTime;
        private TextView tvUpdateTime;
        private AVLoadingIndicatorView indicatorView;

        public MyViewHolder(View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.iv_pool_thumb);
            tvName = itemView.findViewById(R.id.tv_name);
            tvCreator = itemView.findViewById(R.id.tv_creator);
            tvPostCount = itemView.findViewById(R.id.tv_post_count);
            tvCreateTime = itemView.findViewById(R.id.tv_create_time);
            tvUpdateTime = itemView.findViewById(R.id.tv_update_time);
            indicatorView = itemView.findViewById(R.id.view_load_more);
        }
    }

    private enum ViewState {
        NORMAL,
        LOAD_MORE
    }

    public interface OnItemClickListener {
        //加载图集里的图片列表
        void onLoadPostsOfPool(String id, String linkToShow);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }
}
