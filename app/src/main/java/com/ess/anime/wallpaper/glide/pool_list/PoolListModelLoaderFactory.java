package com.ess.anime.wallpaper.glide.pool_list;

import android.content.Context;
import android.graphics.Bitmap;

import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.ess.anime.wallpaper.bean.PoolListBean;

import androidx.annotation.NonNull;

public class PoolListModelLoaderFactory implements ModelLoaderFactory<PoolListBean, Bitmap> {

    private Context mContext;

    public PoolListModelLoaderFactory(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public ModelLoader<PoolListBean, Bitmap> build(@NonNull MultiModelLoaderFactory multiFactory) {
        return new PoolListModelLoader(mContext);
    }

    @Override
    public void teardown() {

    }

}
