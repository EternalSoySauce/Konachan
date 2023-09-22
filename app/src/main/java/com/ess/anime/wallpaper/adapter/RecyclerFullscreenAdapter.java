package com.ess.anime.wallpaper.adapter;

import android.graphics.Color;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.bean.CollectionBean;
import com.ess.anime.wallpaper.ui.activity.FullscreenActivity;
import com.ess.anime.wallpaper.ui.view.MultipleMediaLayout;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RecyclerFullscreenAdapter extends BaseQuickAdapter<CollectionBean, BaseViewHolder> {

    public RecyclerFullscreenAdapter(@Nullable List<CollectionBean> data) {
        super(R.layout.layout_multiple_media, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder holder, CollectionBean collectionBean) {
        MultipleMediaLayout mediaLayout = holder.getView(R.id.layout_multiple_media);
        mediaLayout.setMediaPath(collectionBean.url, false);
        mediaLayout.setBackgroundColor(Color.BLACK);

        if (mContext instanceof FullscreenActivity) {
            FullscreenActivity activity = (FullscreenActivity) mContext;
            mediaLayout.setOnClickListener(v -> activity.toggleOperateLayout());
            mediaLayout.setOnLongClickListener(activity);

            PhotoView photoView = mediaLayout.getPhotoView();
            photoView.setOnPhotoTapListener(activity);
            photoView.setOnOutsidePhotoTapListener(activity);
            photoView.setOnLongClickListener(activity);
        }
    }

}
