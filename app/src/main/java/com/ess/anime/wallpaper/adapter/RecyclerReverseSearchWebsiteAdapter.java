package com.ess.anime.wallpaper.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.model.entity.ReverseSearchWebsiteItem;
import com.ess.anime.wallpaper.ui.activity.web.ReverseSearchWebsiteActivity;

import java.util.List;

import androidx.annotation.NonNull;

public class RecyclerReverseSearchWebsiteAdapter extends BaseQuickAdapter<ReverseSearchWebsiteItem, BaseViewHolder> {

    public RecyclerReverseSearchWebsiteAdapter(@NonNull List<ReverseSearchWebsiteItem> data) {
        super(R.layout.recyclerview_item_reverse_search_website, data);
    }

    @Override
    protected void convert(BaseViewHolder holder, ReverseSearchWebsiteItem item) {
        holder.setImageResource(R.id.iv_icon, item.iconRes);
        if (item.websiteDescRes == 0) {
            holder.setText(R.id.tv_name, item.websiteNameRes);
        } else {
            holder.setText(R.id.tv_name, mContext.getString(item.websiteNameRes) + " - " + mContext.getString(item.websiteDescRes));
        }
        holder.itemView.setOnClickListener(v -> {
            ReverseSearchWebsiteActivity.launch(mContext, item);
        });
    }
}
