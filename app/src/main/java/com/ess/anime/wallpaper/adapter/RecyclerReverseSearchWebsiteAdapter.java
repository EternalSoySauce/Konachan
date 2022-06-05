package com.ess.anime.wallpaper.adapter;

import android.text.TextUtils;

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
        holder.setImageResource(R.id.iv_icon, mContext.getResources().getIdentifier(
                "ic_reverse_search_website_" + (holder.getLayoutPosition() + 1),
                "drawable", mContext.getPackageName()));
        if (TextUtils.isEmpty(item.websiteDesc)) {
            holder.setText(R.id.tv_name, item.websiteName);
        } else {
            holder.setText(R.id.tv_name, item.websiteName + " - " + item.websiteDesc);
        }
        holder.itemView.setOnClickListener(v -> {
            ReverseSearchWebsiteActivity.launch(mContext, item);
        });
    }
}
