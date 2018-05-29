package com.ess.anime.wallpaper.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class MultiStateRecyclerAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private Context mContext;
    private ViewState mViewState = ViewState.NORMAL;   // 当前需要显示的状态（正常、无网络、无数据、加载更多）

    public MultiStateRecyclerAdapter(Context context) {
        this.mContext = context;
    }

    public abstract int bindLoadMoreLayoutRes();

    public abstract int bindLoadingLayoutRes();

    public abstract int bindNoDataLayoutRes();

    public abstract int bindNoNetworkLayoutRes();

    public abstract int bindNormalLayoutRes();

    public abstract VH onCreateViewHolder(View view);

    public abstract void onBindLoadMoreHolder(VH holder, int layoutPos);

    public abstract void onBindLoadingHolder(VH holder, int layoutPos);

    public abstract void onBindNoDataHolder(VH holder, int layoutPos);

    public abstract void onBindNoNetworkHolder(VH holder, int layoutPos);

    public abstract void onBindNormalHolder(VH holder, int position);

    public abstract int getDataListSize();

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId;
        if (viewType == ViewState.LOAD_MORE.ordinal()) {
            layoutId = bindLoadMoreLayoutRes();
        } else if (viewType == ViewState.LOADING.ordinal()) {
            layoutId = bindLoadingLayoutRes();
        } else if (viewType == ViewState.NO_DATA.ordinal()) {
            layoutId = bindNoDataLayoutRes();
        } else if (viewType == ViewState.NO_NETWORK.ordinal()) {
            layoutId = bindNoNetworkLayoutRes();
        } else {
            layoutId = bindNormalLayoutRes();
        }

        View view;
        try {
            view = LayoutInflater.from(mContext).inflate(layoutId, parent, false);
        } catch (Resources.NotFoundException e) {
            view = new View(mContext);
            parent.addView(view);
        }
        return onCreateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        // 设置View.GONE 扔会占位，所以只能设置高度为0 来控制显示
        // 然而recycler第一个item高度为0会出现无法触发SwipeRefresh下拉刷新的bug，所以将size设为1，并setVisibility();
        // GridDividerItemDecoration中对应高度为0或1的item不设置offset
        if (getItemViewType(position) == ViewState.LOAD_MORE.ordinal()) {
            boolean  isLoadMoreState = mViewState == ViewState.LOAD_MORE;
            holder.itemView.getLayoutParams().height = isLoadMoreState ? ViewGroup.LayoutParams.WRAP_CONTENT : 1;
            holder.itemView.setVisibility(isLoadMoreState ? View.VISIBLE : View.GONE);
            onBindLoadMoreHolder(holder, position);
            return;
        } else if (getItemViewType(position) == ViewState.LOADING.ordinal()) {
            int size = mViewState == ViewState.LOADING ? ViewGroup.LayoutParams.MATCH_PARENT : 0;
            holder.itemView.getLayoutParams().width = size;
            holder.itemView.getLayoutParams().height = size;
            onBindLoadingHolder(holder, position);
            return;
        } else if (getItemViewType(position) == ViewState.NO_DATA.ordinal()) {
            int size =  mViewState == ViewState.NO_DATA ? ViewGroup.LayoutParams.MATCH_PARENT : 0;
            holder.itemView.getLayoutParams().width = size;
            holder.itemView.getLayoutParams().height = size;
            onBindNoDataHolder(holder, position);
            return;
        } else if (getItemViewType(position) == ViewState.NO_NETWORK.ordinal()) {
            int size = mViewState == ViewState.NO_NETWORK ? ViewGroup.LayoutParams.MATCH_PARENT : 0;
            holder.itemView.getLayoutParams().width = size;
            holder.itemView.getLayoutParams().height = size;
            onBindNoNetworkHolder(holder, position);
            return;
        }

        onBindNormalHolder(holder, position);
    }

    @Override
    public int getItemCount() {
        return getDataListSize() + 4;
    }

    public Context getContext() {
        return mContext;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 4) {
            return ViewState.LOAD_MORE.ordinal();
        } else if (position == getItemCount() - 3) {
            return ViewState.LOADING.ordinal();
        } else if (position == getItemCount() - 2) {
            return ViewState.NO_DATA.ordinal();
        } else if (position == getItemCount() - 1) {
            return ViewState.NO_NETWORK.ordinal();
        } else {
            return ViewState.NORMAL.ordinal();
        }
    }

    public void showLoadMore() {
        setViewState(ViewState.LOAD_MORE);
        notifyDataSetChanged();
    }

    public void showLoading() {
        setViewState(ViewState.LOADING);
        notifyDataSetChanged();
    }

    public void showNoData() {
        setViewState(ViewState.NO_DATA);
        notifyDataSetChanged();
    }

    public void showNoNetwork() {
        setViewState(ViewState.NO_NETWORK);
        notifyDataSetChanged();
    }

    public void showNormal() {
        setViewState(ViewState.NORMAL);
        notifyDataSetChanged();
    }

    public ViewState getViewState() {
        return mViewState;
    }

    public void setViewState(ViewState state) {
        mViewState = state;
    }

    public enum ViewState {
        LOAD_MORE,
        LOADING,
        NO_DATA,
        NO_NETWORK,
        NORMAL
    }
}
