package com.ess.anime.wallpaper.glide;

import android.content.Context;

import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.ess.anime.wallpaper.bean.PoolListBean;

import java.io.InputStream;

import androidx.annotation.NonNull;

public class PoolListModelLoaderFactory implements ModelLoaderFactory<PoolListBean, InputStream> {

    private Context mContext;

    PoolListModelLoaderFactory(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public ModelLoader<PoolListBean, InputStream> build(@NonNull MultiModelLoaderFactory multiFactory) {
        return new PoolListModelLoader(mContext);
    }

    @Override
    public void teardown() {

    }

}
