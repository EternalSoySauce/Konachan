package com.ess.anime.wallpaper.adapter;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Priority;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.bean.CollectionBean;
import com.ess.anime.wallpaper.glide.GlideApp;
import com.ess.anime.wallpaper.listener.OnTouchScaleListener;
import com.ess.anime.wallpaper.utils.FileUtils;
import com.ess.anime.wallpaper.utils.UIUtils;
import com.mixiaoxiao.smoothcompoundbutton.SmoothCheckBox;

import java.util.ArrayList;
import java.util.List;

public class RecyclerCollectionAdapter extends BaseQuickAdapter<CollectionBean, BaseViewHolder> {

    private boolean mEditing;
    private List<CollectionBean> mSelectList = new ArrayList<>();
    private OnSelectChangedListener mSelectChangedListener;

    public RecyclerCollectionAdapter(@NonNull List<CollectionBean> collectionList) {
        super(R.layout.recyclerview_item_collection, collectionList);
    }

    @Override
    protected void convert(final BaseViewHolder holder, final CollectionBean collectionBean) {

        // 编辑模式选择框
        holder.setGone(R.id.cb_choose, isEditMode());
        holder.setChecked(R.id.cb_choose, isSelected(collectionBean));
        holder.getView(R.id.cb_choose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = ((SmoothCheckBox) v).isChecked();
                if (isChecked) {
                    select(collectionBean);
                } else {
                    deselect(collectionBean);
                }
            }
        });

        // 编辑模式放大查看
        holder.setGone(R.id.iv_enlarge, isEditMode());
        holder.addOnClickListener(R.id.iv_enlarge);

        // 图片格式标记
        int tagResId = 0;
        String imageUrl = collectionBean.url;
        if (FileUtils.isImageType(imageUrl) && imageUrl.toLowerCase().endsWith("gif")) {
            tagResId = R.drawable.ic_tag_gif;
        } else if (FileUtils.isVideoType(imageUrl)) {
            tagResId = R.drawable.ic_tag_video;
        }
        holder.setImageResource(R.id.iv_tag, tagResId);

        // 图片
        // 固定ImageView尺寸防止notify时图片闪烁
        ImageView ivCollection = holder.getView(R.id.iv_collection);
        int slideLength = (int) ((UIUtils.getScreenWidth(mContext) - UIUtils.dp2px(mContext, 6)) / 3f);
        ivCollection.getLayoutParams().width = slideLength;
        ivCollection.getLayoutParams().height = slideLength;

        GlideApp.with(mContext)
                .asBitmap()
                .load(imageUrl)
                .priority(Priority.HIGH)
                .into(ivCollection);
        ivCollection.setOnTouchListener(new OnTouchScaleListener());
        holder.addOnClickListener(R.id.iv_collection);
        holder.addOnLongClickListener(R.id.iv_collection);
    }

    @Override
    public void addData(int position, @NonNull CollectionBean data) {
        super.addData(position, data);
        notifyItemRangeChanged(0, mData.size());
    }

    public void removeData(CollectionBean collectionBean) {
        int position = mData.indexOf(collectionBean);
        if (position != -1) {
            mData.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(0, mData.size());
        }
    }

    public void removeDatas(ArrayList<CollectionBean> deleteList) {
        for (CollectionBean collectionBean : deleteList) {
            int position = mData.indexOf(collectionBean);
            if (position != -1) {
                mData.remove(position);
                notifyItemRemoved(position);
            }
        }
        notifyItemRangeChanged(0, mData.size());
    }

    public void enterEditMode() {
        mEditing = true;
        notifyDataSetChanged();
    }

    public void exitEditMode(boolean notify) {
        mEditing = false;
        mSelectList.clear();
        notifySelectChanged();
        if (notify) {
            notifyDataSetChanged();
        }
    }

    public boolean isEditMode() {
        return mEditing;
    }

    public void select(CollectionBean collectionBean) {
        if (!isSelected(collectionBean)) {
            mSelectList.add(collectionBean);
            notifySelectChanged();
        }
    }

    public void deselect(CollectionBean collectionBean) {
        mSelectList.remove(collectionBean);
        notifySelectChanged();
    }

    public boolean isSelected(CollectionBean collectionBean) {
        return mSelectList.contains(collectionBean);
    }

    public void selectAll() {
        mSelectList.clear();
        mSelectList.addAll(mData);
        notifyDataSetChanged();
        notifySelectChanged();
    }

    public void deselectAll() {
        mSelectList.clear();
        notifyDataSetChanged();
        notifySelectChanged();
    }

    public List<CollectionBean> getSelectList() {
        return mSelectList;
    }

    public void setOnSelectChangedListener(OnSelectChangedListener listener) {
        mSelectChangedListener = listener;
        notifySelectChanged();
    }

    private void notifySelectChanged() {
        if (mSelectChangedListener != null) {
            mSelectChangedListener.onSelectChanged(mSelectList.size(),
                    !mSelectList.isEmpty() && mSelectList.size() >= mData.size());
        }
    }

    public interface OnSelectChangedListener {
        void onSelectChanged(int selectCount, boolean allSelected);
    }

}
