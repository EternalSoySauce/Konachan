package com.ess.konachan.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ess.konachan.R;
import com.ess.konachan.bean.PoolListBean;
import com.ess.konachan.global.GlideConfig;
import com.ldoublem.loadingviewlib.view.LVFinePoiStar;

import java.util.ArrayList;
import java.util.Iterator;

public class RecyclerPoolAdapter extends RecyclerView.Adapter<RecyclerPoolAdapter.MyViewHolder> {

    private Context mContext;
    private ArrayList<PoolListBean> mPoolList;
    private ClickListener mClickListener;
    private ViewState mCurrentState;

    public RecyclerPoolAdapter(Context context, @NonNull ArrayList<PoolListBean> poolList) {
        mContext = context;
        mPoolList = poolList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId = viewType == ViewState.NORMAL.ordinal() ? R.layout.recyclerview_item_pool
                : R.layout.layout_load_more;
        View view = LayoutInflater.from(mContext).inflate(layoutId, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1) {
            return ViewState.LOAD_MORE.ordinal();
        } else {
            return ViewState.NORMAL.ordinal();
        }
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        if (position == getItemCount() - 1) {
            ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
            params.height = mCurrentState == ViewState.LOAD_MORE ? ViewGroup.LayoutParams.WRAP_CONTENT : 0;
            holder.loadMoreView.startAnim(1500);
            return;
        }

        final PoolListBean poolListBean = mPoolList.get(position);

        //缩略图
        GlideConfig.getInstance().loadImage(mContext, poolListBean.thumbUrl, holder.ivThumb);

        //图集名称
        holder.tvName.setText(poolListBean.name);

        //创建者
        holder.tvCreator.setText(poolListBean.creator);

        //图片数量
        holder.tvPostCount.setText(poolListBean.postCount);

        //创建时间
        holder.tvCreateTime.setText(poolListBean.createTime);

        //上传时间
        holder.tvUpdateTime.setText(poolListBean.updateTime);

        //点击加载图片列表
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mClickListener != null) {
                    mClickListener.onLoadPostsOfPool(poolListBean.id, poolListBean.linkToShow);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPoolList.size() + 1;
    }

    public ArrayList<PoolListBean> getPoolList() {
        return mPoolList;
    }

    public void loadMoreDatas(ArrayList<PoolListBean> poolList) {
        int position = getItemCount() - 1;
        addDatas(position, poolList);
    }

    public void refreshDatas(ArrayList<PoolListBean> poolList) {
        addDatas(0, poolList);
    }

    private void addDatas(int position, ArrayList<PoolListBean> poolList) {
        synchronized (this) {
            //删掉更新时因网站新增图片导致thumbList出现的重复项
            Iterator<PoolListBean> iterator = poolList.iterator();
            while (iterator.hasNext()) {
                PoolListBean newData = iterator.next();
                for (PoolListBean poolListBean : mPoolList) {
                    if (newData.thumbUrl.equals(poolListBean.thumbUrl)) {
                        iterator.remove();
                        break;
                    }
                }
            }

            mPoolList.addAll(position, poolList);
            notifyDataSetChanged();
            preloadThumbnail(poolList);
        }
    }

    private void preloadThumbnail(ArrayList<PoolListBean> poolList) {
        for (PoolListBean poolListBean : poolList) {
            Glide.with(mContext).load(poolListBean.thumbUrl).submit();
        }
    }

    public void changeToLoadMoreState() {
        mCurrentState = ViewState.LOAD_MORE;
        notifyDataSetChanged();
    }

    public void changeToNormalState() {
        mCurrentState = ViewState.NORMAL;
        notifyDataSetChanged();
    }

    public void clear() {
        mPoolList.clear();
        notifyDataSetChanged();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivThumb;
        private TextView tvName;
        private TextView tvCreator;
        private TextView tvPostCount;
        private TextView tvCreateTime;
        private TextView tvUpdateTime;
        private LVFinePoiStar loadMoreView;

        public MyViewHolder(View itemView) {
            super(itemView);
            ivThumb = (ImageView) itemView.findViewById(R.id.iv_pool_thumb);
            tvName = (TextView) itemView.findViewById(R.id.tv_name);
            tvCreator = (TextView) itemView.findViewById(R.id.tv_creator);
            tvPostCount = (TextView) itemView.findViewById(R.id.tv_post_count);
            tvCreateTime = (TextView) itemView.findViewById(R.id.tv_create_time);
            tvUpdateTime = (TextView) itemView.findViewById(R.id.tv_update_time);
            loadMoreView = (LVFinePoiStar) itemView.findViewById(R.id.view_load_more);
            if (loadMoreView != null) {
                loadMoreView.setViewColor(Color.GREEN);
                loadMoreView.setCircleColor(Color.GREEN);
            }
        }
    }

    private enum ViewState {
        NORMAL,
        LOAD_MORE
    }

    public interface ClickListener {
        //加载图集里的图片列表
        void onLoadPostsOfPool(String id, String linkToShow);
    }

    public void setOnClickListener(ClickListener listener) {
        mClickListener = listener;
    }
}