package com.ess.konachan.global;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.ess.konachan.R;

import java.io.File;

public class GlideConfig {

    private static class GlideHolder {
        private static final GlideConfig instance = new GlideConfig();
    }

    private RequestOptions mLoadOptions;

    private GlideConfig() {
        mLoadOptions = new RequestOptions()
                .placeholder(R.drawable.ic_loading)
                .error(R.drawable.ic_error)
                .priority(Priority.HIGH)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);
    }

    public static GlideConfig getInstance() {
        return GlideHolder.instance;
    }

    public void loadImage(Context context, String url, ImageView imageView) {
        Glide.with(context)
                .load(url)
                .apply(mLoadOptions)
                .transition(new DrawableTransitionOptions().dontTransition())
                .into(imageView);
    }

    public void loadImage(RequestBuilder<Drawable> listener, ImageView imageView) {
        listener.apply(mLoadOptions)
                .transition(new DrawableTransitionOptions().dontTransition())
                .into(imageView);
    }

    public void loadImage(Context context, int srcId, ImageView imageView) {
        Glide.with(context)
                .load(srcId)
                .into(imageView);
    }

    public FutureTarget<File> downloadImage(Context context, String url) {
        return Glide.with(context)
                .load(url)
                .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
    }

    public void loadGif(Context context, int srcId, ImageView imageView) {
        Glide.with(context)
                .asGif()
                .load(srcId)
                .into(imageView);
    }
}
