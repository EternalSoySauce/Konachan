package com.ess.anime.wallpaper.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class BaseRecyclerEditAdapter<T> extends BaseQuickAdapter<T, BaseViewHolder> {

    protected final static int TOGGLE_EDIT_MODE = 1;

    protected boolean mIsEditing;
    protected List<T> mSelectList = new ArrayList<>();
    protected OnSelectChangedListener mSelectChangedListener;

    public BaseRecyclerEditAdapter(int layoutResId, @Nullable List<T> data) {
        super(layoutResId, data);
    }

    @Override
    public void addData(int position, @NonNull T data) {
        super.addData(position, data);
        notifyItemRangeChanged(0, mData.size());
    }

    public void removeData(T data) {
        int position = mData.indexOf(data);
        if (position != -1) {
            mData.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(0, mData.size());
        }
    }

    public void removeDatas(List<T> deleteList) {
        for (T data : deleteList) {
            int position = mData.indexOf(data);
            if (position != -1) {
                mData.remove(position);
                notifyItemRemoved(position);
            }
        }
        notifyItemRangeChanged(0, mData.size());
    }

    public void enterEditMode() {
        if (!isEditMode()) {
            mIsEditing = true;
            if (showEditTransitionAnimation()) {
                notifyItemRangeChanged(0, getItemCount(), TOGGLE_EDIT_MODE);
            } else {
                notifyDataSetChanged();
            }
        }
    }

    public void exitEditMode(boolean notify) {
        if (isEditMode()) {
            mIsEditing = false;
            mSelectList.clear();
            notifySelectChanged();
            if (notify) {
                if (showEditTransitionAnimation()) {
                    notifyItemRangeChanged(0, getItemCount(), TOGGLE_EDIT_MODE);
                } else {
                    notifyDataSetChanged();
                }
            }
        }
    }

    public boolean isEditMode() {
        return mIsEditing;
    }

    public void select(T data) {
        if (!isSelected(data)) {
            mSelectList.add(data);
            notifySelectChanged();
        }
    }

    public void deselect(T data) {
        mSelectList.remove(data);
        notifySelectChanged();
    }

    public boolean isSelected(T data) {
        return mSelectList.contains(data);
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

    public List<T> getSelectList() {
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

    protected abstract boolean showEditTransitionAnimation();

    public interface OnSelectChangedListener {
        void onSelectChanged(int selectCount, boolean allSelected);
    }

}
