package com.lss.anime.wallpaper.glide;

import android.content.Context;
import android.text.TextUtils;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.module.AppGlideModule;

@GlideModule
public class MyGlideModule extends AppGlideModule {

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        super.applyOptions(context, builder);
    }

    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }

    public static GlideUrl makeGlideUrl(String imgUrl) {
        if (TextUtils.isEmpty(imgUrl)) {
            imgUrl = "https://konachan.com/images/guest.png";
        }
        return new GlideUrl(imgUrl, new LazyHeaders.Builder()
                .addHeader("User-Agent",
                        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_2) AppleWebKit / 537.36(KHTML, like Gecko) Chrome  47.0.2526.106 Safari / 537.36")
                .build());
    }
}
