package com.ess.anime.wallpaper.adapter;

import android.text.TextUtils;
import android.view.View;

import com.balysv.materialripple.MaterialRippleLayout;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.model.entity.CommonSettingItem;
import com.mixiaoxiao.smoothcompoundbutton.SmoothCheckBox;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

public class RecyclerCommonSettingAdapter extends BaseQuickAdapter<CommonSettingItem, BaseViewHolder> {

    private final static int UPDATE_DATA = 1;

    public RecyclerCommonSettingAdapter() {
        this(new ArrayList<>());
    }

    public RecyclerCommonSettingAdapter(@NonNull List<CommonSettingItem> items) {
        super(R.layout.recycler_item_common_setting, items);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder holder, CommonSettingItem commonSettingItem) {
        bindUI(holder, commonSettingItem);
    }

    @Override
    protected void convertPayloads(@NonNull BaseViewHolder holder, CommonSettingItem commonSettingItem, @NonNull List<Object> payloads) {
        super.convertPayloads(holder, commonSettingItem, payloads);
        for (Object payload : payloads) {
            if (payload.equals(UPDATE_DATA)) {
                bindUI(holder, commonSettingItem);
            }
        }
    }

    private void bindUI(BaseViewHolder holder, CommonSettingItem commonSettingItem) {
        // 标题
        holder.setGone(R.id.tv_title, !TextUtils.isEmpty(commonSettingItem.getTitle()));
        holder.setText(R.id.tv_title, commonSettingItem.getTitle());

        // 描述
        holder.setGone(R.id.tv_desc, !TextUtils.isEmpty(commonSettingItem.getDesc()));
        holder.setText(R.id.tv_desc, commonSettingItem.getDesc());

        // Tips
        holder.setGone(R.id.tv_tips, !TextUtils.isEmpty(commonSettingItem.getTips()));
        holder.setText(R.id.tv_tips, commonSettingItem.getTips());

        // Checkbox
        SmoothCheckBox checkBox = holder.getView(R.id.checkbox);
        checkBox.setVisibility(commonSettingItem.isCheckboxShown() ? View.VISIBLE : View.GONE);
        checkBox.setOnCheckedChangeListener(null);
        checkBox.setChecked(commonSettingItem.isCheckboxChecked(), false, false);
        checkBox.setOnCheckedChangeListener((smoothCompoundButton, isChecked) -> {
            commonSettingItem.setCheckboxChecked(isChecked);
            if (commonSettingItem.getOnCheckedChangeListener() != null) {
                commonSettingItem.getOnCheckedChangeListener().onCheckedChanged(smoothCompoundButton, isChecked);
            }
        });

        // 分割线
        holder.setGone(R.id.view_divide_line, commonSettingItem.isDividerShown());

        // 点击事件
        MaterialRippleLayout rippleLayout = holder.getView(R.id.ripple_layout);
        rippleLayout.setRippleDelayClick(commonSettingItem.isRippleDelayClick());
        holder.getView(R.id.layout_content).setOnClickListener(v -> {
            if (commonSettingItem.isCheckboxShown()) {
                checkBox.toggle();
            }
            if (commonSettingItem.getOnClickListener() != null) {
                commonSettingItem.getOnClickListener().onClick(v);
            }
        });
    }

    public void notifyDataSetChanged(boolean animate) {
        if (animate) {
            notifyItemRangeChanged(0, getItemCount(), UPDATE_DATA);
        } else {
            notifyDataSetChanged();
        }
    }
}
