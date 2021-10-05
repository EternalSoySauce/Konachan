package com.ess.anime.wallpaper.adapter;

import android.content.Context;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.model.entity.DetailTagMoreItem;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

public class RecyclerDetailTagMorePopupAdapter extends BaseQuickAdapter<DetailTagMoreItem, BaseViewHolder> {

    public RecyclerDetailTagMorePopupAdapter(@NonNull List<DetailTagMoreItem> data) {
        super(R.layout.recyclerview_item_popup_detai_tag_more, data);
    }

    @Override
    protected void convert(BaseViewHolder holder, DetailTagMoreItem item) {
        holder.setImageResource(R.id.iv_icon, item.getIconRes());
        holder.setText(R.id.tv_title, item.getTitle());
    }

    public int[] measureItemsSize(Context context) {
        int layoutWidth = 0;
        int layoutHeight = 0;
        BaseViewHolder holder = createBaseViewHolder(View.inflate(context, mLayoutResId, null));
        for (DetailTagMoreItem item : new ArrayList<>(mData)) {holder.itemView.measure(0, -1);
            holder.itemView.getMeasuredHeight();
            convert(holder, item);
            holder.itemView.measure(0, 0);
            int measuredWidth = holder.itemView.getMeasuredWidth();
            if (measuredWidth > layoutWidth) {
                layoutWidth = measuredWidth;
            }
            layoutHeight += holder.itemView.getMeasuredHeight();
        }
        return new int[]{layoutWidth, layoutHeight};
    }

}
