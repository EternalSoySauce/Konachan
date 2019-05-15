package com.ess.anime.wallpaper.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.bean.CollectionBean;
import com.ess.anime.wallpaper.ui.view.MultipleMediaLayout;
import com.qmuiteam.qmui.widget.QMUIPagerAdapter;

import java.util.List;

public class ViewPagerFullscreenAdapter extends QMUIPagerAdapter {

    private List<CollectionBean> mCollectionList;

    public ViewPagerFullscreenAdapter(@NonNull List<CollectionBean> collectionList) {
        mCollectionList = collectionList;
    }

    @Override
    protected Object hydrate(ViewGroup container, int position) {
        String url = mCollectionList.get(position).url;
        MultipleMediaLayout mediaLayout  =(MultipleMediaLayout) LayoutInflater.from(container.getContext())
                .inflate(R.layout.layout_multiple_media, null);
        mediaLayout.setMediaPath(url, false);
        return mediaLayout;
    }

    @Override
    protected void populate(ViewGroup container, Object item, int position) {
        container.addView((View) item);
    }

    @Override
    protected void destroy(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return mCollectionList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

}
