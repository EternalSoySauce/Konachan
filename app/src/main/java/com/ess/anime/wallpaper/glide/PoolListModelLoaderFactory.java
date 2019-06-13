package com.ess.anime.wallpaper.glide;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.ess.anime.wallpaper.bean.PoolListBean;

public class PoolListModelLoaderFactory implements ModelLoaderFactory<PoolListBean, Bitmap> {

    private Context mContext;

    PoolListModelLoaderFactory(Context context) {
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
