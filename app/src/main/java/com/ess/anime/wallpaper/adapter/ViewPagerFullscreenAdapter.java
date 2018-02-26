package com.ess.anime.wallpaper.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.bumptech.glide.Priority;
import com.ess.anime.wallpaper.bean.CollectionBean;
import com.ess.anime.wallpaper.other.GlideApp;

import java.util.ArrayList;

import uk.co.senab.photoview.PhotoView;

public class ViewPagerFullscreenAdapter extends PagerAdapter {

    private Context mContext;
    private ArrayList<PhotoView> mPhotoViewList;
    private ArrayList<CollectionBean> mCollectionList;

    public ViewPagerFullscreenAdapter(Context context, ArrayList<PhotoView> photoViewList
            , ArrayList<CollectionBean> collectionList) {
        mContext = context;
        mPhotoViewList = photoViewList;
        mCollectionList = collectionList;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        PhotoView photoView = mPhotoViewList.get(position % mPhotoViewList.size());
        String url = mCollectionList.get(position).url;
        GlideApp.with(mContext)
                .load(url)
                .priority(Priority.IMMEDIATE)
                .into(photoView);
        ViewParent parent = photoView.getParent();
        if (parent != null) {
            ((ViewGroup) parent).removeView(photoView);
        }
        container.addView(photoView);
        return photoView;
    }

    @Override
    public int getCount() {
        return mCollectionList.size();
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return (arg0 == arg1);
    }

}
