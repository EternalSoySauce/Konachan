package com.ess.anime.wallpaper.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.ess.anime.wallpaper.R;

import java.util.List;

import androidx.annotation.NonNull;

public class RecyclerSingleChoiceAdapter<T> extends BaseQuickAdapter<T, BaseViewHolder> {

    private int mSelectPos = -1;

    public RecyclerSingleChoiceAdapter(@NonNull List<T> data) {
        super(R.layout.recycler_item_dialog_singlechoice, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder holder, T data) {
        holder.setChecked(R.id.md_control, holder.getLayoutPosition() == mSelectPos);
        holder.setText(R.id.md_title, data.toString());
        holder.itemView.setOnClickListener((v) -> {
            setSelectPos(holder.getLayoutPosition(), true);
        });
    }

    public void setSelectPos(int selectPos, boolean animate) {
        if (mSelectPos != selectPos) {
            mSelectPos = selectPos;
            if (animate) {
                notifyItemRangeChanged(0, getItemCount());
            } else {
                notifyDataSetChanged();
            }
        }
    }

    public int getSelectPos() {
        return mSelectPos;
    }

    public T getSelectData() {
        return getItem(mSelectPos);
    }

}
