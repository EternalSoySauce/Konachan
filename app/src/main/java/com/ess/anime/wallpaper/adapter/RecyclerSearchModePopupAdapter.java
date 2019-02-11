package com.ess.anime.wallpaper.adapter;

import android.support.annotation.NonNull;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.ess.anime.wallpaper.R;

import java.util.List;

public class RecyclerSearchModePopupAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

    private int mSelectedPos;

    public RecyclerSearchModePopupAdapter(@NonNull List<String> searchModeList) {
        super(R.layout.recyclerview_item_popup_search_mode, searchModeList);
    }

    @Override
    protected void convert(BaseViewHolder holder, String mode) {
        // 搜索模式文字
        TextView tvMode = holder.getView(R.id.tv_search_mode);
        tvMode.setText(mode);
        tvMode.setSelected(holder.getLayoutPosition() == mSelectedPos);
    }

    public void setSelection(int position) {
        mSelectedPos = position;
        notifyDataSetChanged();
    }

}
