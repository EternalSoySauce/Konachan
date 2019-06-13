package com.ess.anime.wallpaper.glide;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import com.ess.anime.wallpaper.bean.PoolListBean;
import com.ess.anime.wallpaper.bean.ThumbBean;
import com.ess.anime.wallpaper.http.OkHttp;
import com.ess.anime.wallpaper.http.parser.HtmlParserFactory;

import java.util.List;

public class PoolListDataFetcher implements DataFetcher<Bitmap> {

    private Context mContext;
    private PoolListBean mPoolListBean;
    private volatile boolean mIsCancelled;
    private String mHttpTag;

    public PoolListDataFetcher(Context context, PoolListBean poolListBean) {
        mContext = context;
        mPoolListBean = poolListBean;
        mHttpTag = mPoolListBean.linkToShow;
    }

    @Override
    public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super Bitmap> callback) {
        try {
            String url = OkHttp.getPoolPostUrl(mContext, mPoolListBean.linkToShow, 1);
            String html = OkHttp.executeHtml(url, mHttpTag);
            if (mIsCancelled) {
                return;
            } else {
                List<ThumbBean> thumbList = HtmlParserFactory.createParser(mContext, html).getThumbListOfPool();
                if (thumbList.isEmpty()) {
                    return;
                }
                Bitmap bitmap = OkHttp.executeImage(thumbList.get(0).thumbUrl, mHttpTag);
                if (mIsCancelled) {
                    return;
                } else {
                    callback.onDataReady(bitmap);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            callback.onLoadFailed(e);
        }
    }

    @Override
    public void cleanup() {
        OkHttp.cancel(mPoolListBean.linkToShow);
    }

    @Override
    public void cancel() {
        mIsCancelled = true;
        OkHttp.cancel(mHttpTag);
    }

    @NonNull
    @Override
    public Class<Bitmap> getDataClass() {
        return Bitmap.class;
    }

    @NonNull
    @Override
    public DataSource getDataSource() {
        return DataSource.LOCAL;
    }

}
