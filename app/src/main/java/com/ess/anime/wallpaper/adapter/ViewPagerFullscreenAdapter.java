package com.ess.anime.wallpaper.adapter;

import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.bean.CollectionBean;
import com.ess.anime.wallpaper.ui.activity.FullscreenActivity;
import com.ess.anime.wallpaper.ui.view.MultipleMediaLayout;

import java.util.List;

public class ViewPagerFullscreenAdapter extends ReusedPagerAdapter<ViewPagerFullscreenAdapter.MyViewHolder> {

    private FullscreenActivity mActivity;
    private List<CollectionBean> mCollectionList;

    public ViewPagerFullscreenAdapter(FullscreenActivity activity, @NonNull List<CollectionBean> collectionList) {
        mActivity = activity;
        mCollectionList = collectionList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mActivity)
                .inflate(R.layout.layout_multiple_media, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String url = mCollectionList.get(position).url;

        MultipleMediaLayout mediaLayout = (MultipleMediaLayout) holder.itemView;
        mediaLayout.setMediaPath(url, false);
    }

    @Override
    public int getItemCount() {
        return mCollectionList.size();
    }

    class MyViewHolder extends ReusedPagerAdapter.PagerViewHolder {

        public MyViewHolder(View view) {
            super(view);
        }
    }
}
