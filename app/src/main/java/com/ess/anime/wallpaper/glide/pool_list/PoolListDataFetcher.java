package com.ess.anime.wallpaper.glide.pool_list;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import com.ess.anime.wallpaper.bean.PoolListBean;
import com.ess.anime.wallpaper.bean.ThumbBean;
import com.ess.anime.wallpaper.http.OkHttp;
import com.ess.anime.wallpaper.model.manager.WebsiteManager;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.List;

import androidx.annotation.NonNull;
import okhttp3.Response;

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
        Response response = null;
        Response thumbResponse = null;
        try {
            String url = WebsiteManager.getInstance().getWebsiteConfig()
                    .getPoolPostUrl(mPoolListBean.linkToShow, 1);
            response = OkHttp.execute(url, mHttpTag);
            if (mIsCancelled) {
                return;
            } else if (response.isSuccessful()) {
                String html = response.body().string();
                List<ThumbBean> thumbList = WebsiteManager.getInstance()
                        .getWebsiteConfig()
                        .getHtmlParser()
                        .getThumbListOfPool(Jsoup.parse(html));
                if (thumbList.isEmpty()) {
                    return;
                }
                thumbResponse = OkHttp.execute(thumbList.get(0).thumbUrl, mHttpTag);
                if (mIsCancelled) {
                    return;
                } else if (thumbResponse.isSuccessful()) {
                    callback.onDataReady(BitmapFactory.decodeStream(thumbResponse.body().byteStream()));
                } else {
                    onResponseFailed(callback, thumbResponse);
                }
            } else {
                onResponseFailed(callback, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            callback.onLoadFailed(e);
        } finally {
            if (response != null) {
                response.close();
            }
            if (thumbResponse != null) {
                thumbResponse.close();
            }
        }
    }

    private void onResponseFailed(DataCallback callback, Response response) {
        callback.onLoadFailed(new IOException("Request failed with code: " + response.code()));
    }

    @Override
    public void cleanup() {
        mIsCancelled = true;
        OkHttp.cancel(mHttpTag);
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
