package com.ess.anime.wallpaper.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.ess.anime.wallpaper.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;

public class RecyclerCompleteSearchAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

    public RecyclerCompleteSearchAdapter() {
        this(new ArrayList<String>());
    }

    public RecyclerCompleteSearchAdapter(@NonNull ArrayList<String> tagList) {
        super(R.layout.recyclerview_item_auto_complete_search, tagList);
    }

    @Override
    protected void convert(BaseViewHolder holder, String tag) {
        // 搜索提示
        holder.setText(R.id.tv_auto_complete,tag);
    }

}
