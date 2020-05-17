package com.ess.anime.wallpaper.glide;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.ess.anime.wallpaper.bean.PoolListBean;
import com.ess.anime.wallpaper.glide.glide_url.ProgressInterceptor;
import com.ess.anime.wallpaper.glide.pool_list.PoolListModelLoaderFactory;
import com.ess.anime.wallpaper.http.OkHttp;
import com.ess.anime.wallpaper.utils.ComponentUtils;
import com.ess.anime.wallpaper.utils.FileUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import okhttp3.OkHttpClient;

@GlideModule
public class MyGlideModule extends AppGlideModule {

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        super.registerComponents(context, glide, registry);
        // 自定义加载PoolListBean
        registry.prepend(PoolListBean.class, Bitmap.class, new PoolListModelLoaderFactory(context));

        // 普通Url进度监听
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new ProgressInterceptor())
                .build();
        // todo 自定义OkHttpClient会影响glide内置加载顺序
//        registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(okHttpClient));
    }


    public static GlideUrl makeGlideUrl(String imgUrl) {
        if (TextUtils.isEmpty(imgUrl)) {
            imgUrl = "https://konachan.com/images/guest.png";
        }
        LazyHeaders headers = new LazyHeaders.Builder()
                .addHeader("User-Agent", OkHttp.USER_AGENT)
                .build();
        return new GlideUrl(imgUrl, headers);
    }

    // P站等下载高清大图需要给服务器发送一个“Referer”参数，用来告诉服务器你是从哪个网址进入图片链接的
    public static GlideUrl makeGlideUrlWithReferer(String imgUrl, String webUrl) {
        LazyHeaders headers = new LazyHeaders.Builder()
                .addHeader("User-Agent", OkHttp.USER_AGENT)
                .addHeader("Referer", webUrl)
                .build();
        return new GlideUrl(imgUrl, headers);
    }

    public static void preloadImage(Context context, String oriUrl) {
        if (!FileUtils.isImageType(oriUrl)) {
            return;
        }
        GlideApp.with(context)
                .load(MyGlideModule.makeGlideUrl(oriUrl))
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        if (isFirstResource
                                && (!(context instanceof Activity)
                                || ComponentUtils.isActivityActive((Activity) context))) {
                            preloadImage(context, oriUrl);
                        }
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                }).submit();
    }
}
