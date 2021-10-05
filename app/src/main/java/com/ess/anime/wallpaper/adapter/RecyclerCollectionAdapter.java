package com.ess.anime.wallpaper.adapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Priority;
import com.chad.library.adapter.base.BaseViewHolder;
import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.bean.CollectionBean;
import com.ess.anime.wallpaper.glide.GlideApp;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.listener.OnTouchScaleListener;
import com.ess.anime.wallpaper.model.holder.ImageDataHolder;
import com.ess.anime.wallpaper.ui.activity.FullscreenActivity;
import com.ess.anime.wallpaper.utils.FileUtils;
import com.ess.anime.wallpaper.utils.UIUtils;
import com.mixiaoxiao.smoothcompoundbutton.SmoothCheckBox;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.GridLayoutManager;

public class RecyclerCollectionAdapter extends BaseRecyclerEditAdapter<CollectionBean> {

    private int mImageSlideLength = -1;

    public RecyclerCollectionAdapter(@NonNull List<CollectionBean> collectionList) {
        super(R.layout.recyclerview_item_collection, collectionList);
    }

    private int getImageSlideLength() {
        if (mImageSlideLength == -1) {
            GridLayoutManager layoutManager = (GridLayoutManager) getRecyclerView().getLayoutManager();
            int span = layoutManager.getSpanCount();
            mImageSlideLength = ((UIUtils.getScreenWidth(mContext) - UIUtils.dp2px(mContext, 1.5f * (span + 1))) / span);
        }
        return mImageSlideLength;
    }

    @Override
    protected void convert(final BaseViewHolder holder, final CollectionBean collectionBean) {

        // 编辑模式选择框
        holder.setGone(R.id.cb_choose, isEditMode());
        holder.setChecked(R.id.cb_choose, isSelected(collectionBean));
        holder.getView(R.id.cb_choose).setOnClickListener(v -> {
            boolean isChecked = ((SmoothCheckBox) v).isChecked();
            if (isChecked) {
                select(collectionBean);
            } else {
                deselect(collectionBean);
            }
        });

        // 编辑模式放大查看
        holder.setGone(R.id.iv_enlarge, isEditMode());
        holder.addOnClickListener(R.id.iv_enlarge);
        holder.getView(R.id.iv_enlarge).setOnClickListener(v -> {
            List<CollectionBean> enlargeList = new ArrayList<>();
            enlargeList.add(collectionBean);
            ImageDataHolder.setCollectionList(enlargeList);
            ImageDataHolder.setCollectionCurrentItem(0);

            Intent intent = new Intent(mContext, FullscreenActivity.class);
            intent.putExtra(Constants.ENLARGE, true);
            mContext.startActivity(intent);
        });

        // 图片格式标记
        int tagResId = 0;
        String imageUrl = collectionBean.url;
        if (FileUtils.isImageType(imageUrl) && imageUrl.toLowerCase().endsWith("gif")) {
            tagResId = R.drawable.ic_tag_gif;
        } else if (FileUtils.isVideoType(imageUrl)) {
            tagResId = R.drawable.ic_tag_video;
        }
        holder.setImageResource(R.id.iv_tag, tagResId);

        // 图片
        // 固定ImageView尺寸防止notify时图片闪烁
        ImageView ivCollection = holder.getView(R.id.iv_collection);
        ivCollection.getLayoutParams().width = getImageSlideLength();
        ivCollection.getLayoutParams().height = getImageSlideLength();
        GlideApp.with(mContext)
                .asBitmap()
                .load(imageUrl)
                .priority(Priority.IMMEDIATE)
                .into(ivCollection);

        // 点击、全屏查看监听器
        ivCollection.setOnTouchListener(OnTouchScaleListener.DEFAULT);
        ivCollection.setOnClickListener(v -> {
            if (isEditMode()) {
                // 编辑模式下切换选中/非选中
                boolean newChecked = !isSelected(collectionBean);
                holder.setChecked(R.id.cb_choose, newChecked);
                if (newChecked) {
                    select(collectionBean);
                } else {
                    deselect(collectionBean);
                }
            } else {
                // 非编辑模式下全屏查看
                ImageDataHolder.setCollectionList(getData());
                ImageDataHolder.setCollectionCurrentItem(holder.getLayoutPosition());

                // TODO 点击全屏查看图片缩放动画
                Activity activity = (Activity) mContext;
                Bundle options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        activity, ivCollection, "s").toBundle();
                Intent intent = new Intent(mContext, FullscreenActivity.class);
                activity.startActivityForResult(intent, Constants.FULLSCREEN_CODE);
//                ActivityCompat.startActivityForResult(activity, intent, Constants.FULLSCREEN_CODE, options);
            }
        });

        // 长按进入编辑模式监听器
        holder.addOnLongClickListener(R.id.iv_collection);
    }

    @Override
    protected boolean showEditTransitionAnimation() {
        return false;
    }

}
