package com.ess.anime.wallpaper.adapter;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.ess.anime.wallpaper.R;

import java.util.List;

public class RecyclerWebviewMoreAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

    public RecyclerWebviewMoreAdapter(@NonNull List<String> functionList) {
        super(R.layout.recyclerview_item_popup_webview_more, functionList);
    }

    @Override
    protected void convert(BaseViewHolder holder, String function) {
        // 功能文字
        holder.setText(R.id.tv_function, function);
    }

}
