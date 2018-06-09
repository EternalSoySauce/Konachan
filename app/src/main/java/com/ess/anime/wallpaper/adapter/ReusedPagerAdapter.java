package com.ess.anime.wallpaper.adapter;

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.ess.anime.wallpaper.R;

import java.util.LinkedList;

/**
 * 仿照 RecyclerView.Adapter 实现的具有 itemView 复用功能的 PagerAdapter
 * 需要添加attrs资源，<item name="pager_holder_id" type="id" />
 */
public abstract class ReusedPagerAdapter<VH extends ReusedPagerAdapter.PagerViewHolder> extends PagerAdapter {
    private SparseArray<LinkedList<VH>> holders = new SparseArray<>(1);

    /**
     * 获取 item count
     *
     * @return count
     */
    public abstract int getItemCount();

    /**
     * 获取 view type
     *
     * @param position position
     * @return type
     */
    public int getItemViewType(int position) {
        return 0;
    }

    /**
     * 创建 holder
     *
     * @param parent   parent
     * @param viewType type
     * @return holder
     */
    public abstract VH onCreateViewHolder(ViewGroup parent, int viewType);

    /**
     * 绑定 holder
     *
     * @param holder   holder
     * @param position position
     */
    public abstract void onBindViewHolder(VH holder, int position);

    @Override
    public int getCount() {
        return getItemCount();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        // 获取 position 对应的 type
        int itemViewType = getItemViewType(position);
        // 根据 type 找到缓存的 list
        LinkedList<VH> holderList = holders.get(itemViewType);
        VH holder;
        if (holderList == null) {
            // 如果 list 为空,表示没有缓存
            // 调用 onCreateViewHolder 创建一个 holder
            holder = onCreateViewHolder(container, itemViewType);
            holder.itemView.setTag(R.id.pager_holder_id, holder);
        } else {
            holder = holderList.pollLast();
            if (holder == null) {
                // 如果 list size = 0,表示没有缓存
                // 调用 onCreateViewHolder 创建一个 holder
                holder = onCreateViewHolder(container, itemViewType);
                holder.itemView.setTag(R.id.pager_holder_id, holder);
            }
        }
        holder.position = position;
        holder.viewType = itemViewType;
        // 调用 onBindViewHolder 对 itemView 填充数据
        onBindViewHolder(holder, position);
        container.addView(holder.itemView);
        return holder.itemView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        View view = (View) object;
        container.removeView(view);
        VH holder = (VH) view.getTag(R.id.pager_holder_id);
        int itemViewType = holder.viewType;
        LinkedList<VH> holderList = holders.get(itemViewType);
        if (holderList == null) {
            holderList = new LinkedList<>();
            holders.append(itemViewType, holderList);
        }
        // 缓存 holder
        holderList.push(holder);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    public static abstract class PagerViewHolder {
        public View itemView;
        public int viewType;
        public int position;

        public PagerViewHolder(View view) {
            if (view == null) {
                throw new IllegalArgumentException("itemView may not be null");
            }
            itemView = view;
        }
    }
}
