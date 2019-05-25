package com.ess.anime.wallpaper.glide;

import android.content.Context;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.module.AppGlideModule;
import com.ess.anime.wallpaper.bean.PoolListBean;

import java.io.InputStream;

import androidx.annotation.NonNull;

@GlideModule
public class MyGlideModule extends AppGlideModule {

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        super.registerComponents(context, glide, registry);
        registry.prepend(PoolListBean.class, InputStream.class, new PoolListModelLoaderFactory(context));
    }


    public static GlideUrl makeGlideUrl(String imgUrl) {
        if (TextUtils.isEmpty(imgUrl)) {
            imgUrl = "https://konachan.com/images/guest.png";
        }
        LazyHeaders headers = new LazyHeaders.Builder()
                .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_2) AppleWebKit / 537.36(KHTML, like Gecko) Chrome  47.0.2526.106 Safari / 537.36")
                .build();
        return new GlideUrl(imgUrl, headers);
    }
}
