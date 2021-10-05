package com.ess.anime.wallpaper.adapter;

import android.app.Activity;
import android.text.TextUtils;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseViewHolder;
import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.database.FavoriteTagBean;
import com.ess.anime.wallpaper.model.helper.TagOperationHelper;
import com.ess.anime.wallpaper.ui.view.CustomDialog;
import com.ess.anime.wallpaper.utils.TimeFormat;
import com.mixiaoxiao.smoothcompoundbutton.SmoothCheckBox;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

public class RecyclerFavoriteTagAdapter extends BaseRecyclerEditAdapter<FavoriteTagBean> {

    public RecyclerFavoriteTagAdapter() {
        this(new ArrayList<>());
    }

    public RecyclerFavoriteTagAdapter(@NonNull List<FavoriteTagBean> data) {
        super(R.layout.recyclerview_item_favorite_tag, data);
    }

    @Override
    protected void convert(BaseViewHolder holder, FavoriteTagBean tagBean) {
        // 编辑模式选择框
        holder.setChecked(R.id.cb_choose, isSelected(tagBean));
        holder.setGone(R.id.cb_choose, isEditMode());
        holder.itemView.setOnClickListener(v -> {
            boolean select = !((SmoothCheckBox) holder.getView(R.id.cb_choose)).isChecked();
            if (select) {
                select(tagBean);
            } else {
                deselect(tagBean);
            }
            holder.setChecked(R.id.cb_choose, select);
        });

        // 标签内容
        holder.setText(R.id.tv_tag, tagBean.getTag());

        // 标签备注
        TextView tvAnnotation = holder.getView(R.id.tv_annotation);
        if (TextUtils.isEmpty(tagBean.getAnnotation())) {
            tvAnnotation.setText(R.string.favorite_tag_annotation_empty);
            tvAnnotation.setActivated(false);
        } else {
            tvAnnotation.setText(tagBean.getAnnotation());
            tvAnnotation.setActivated(true);
        }

        // 标签收藏时间
        String date = TimeFormat.dateFormat(tagBean.getFavoriteTime(), "yyyy-MM-dd  HH:mm:ss");
        holder.setText(R.id.tv_favorite_time, mContext.getString(R.string.favorite_tag_favorite_at_time, date));

        // 编辑备注
        holder.getView(R.id.iv_edit).setOnClickListener(v -> {
            CustomDialog.showEditTagAnnotationDialog(mContext, tagBean.getTag(), new CustomDialog.SimpleDialogActionListener() {
                @Override
                public void onPositive() {
                    notifyItemChanged(holder.getLayoutPosition());
                }
            });
        });

        // 搜索标签
        holder.getView(R.id.iv_search).setOnClickListener(v -> {
            TagOperationHelper.searchTag((Activity) mContext, tagBean.getTag());
        });

        // 复制到剪贴板
        holder.getView(R.id.iv_copy).setOnClickListener(v -> {
            TagOperationHelper.copyTagToClipboard((Activity) mContext, tagBean.getTag());
        });

        // 追加到剪贴板
        holder.getView(R.id.iv_append).setOnClickListener(v -> {
            TagOperationHelper.appendTagToClipboard((Activity) mContext, tagBean.getTag());
        });
    }

    @Override
    protected void convertPayloads(@NonNull BaseViewHolder holder, FavoriteTagBean tagBean, @NonNull List<Object> payloads) {
        super.convertPayloads(holder, tagBean, payloads);
        for (Object payload : payloads) {
            if (payload.equals(TOGGLE_EDIT_MODE)) {
                // 编辑模式选择框
                holder.setChecked(R.id.cb_choose, isSelected(tagBean));
                holder.setGone(R.id.cb_choose, isEditMode());
            }
        }
    }

    @Override
    protected boolean showEditTransitionAnimation() {
        return true;
    }

}
