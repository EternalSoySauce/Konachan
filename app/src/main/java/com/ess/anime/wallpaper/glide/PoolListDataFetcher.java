package com.ess.anime.wallpaper.glide;

import android.content.Context;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import com.ess.anime.wallpaper.bean.PoolListBean;
import com.ess.anime.wallpaper.bean.ThumbBean;
import com.ess.anime.wallpaper.http.OkHttp;
import com.ess.anime.wallpaper.http.parser.HtmlParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import androidx.annotation.NonNull;
import okhttp3.Response;

public class PoolListDataFetcher implements DataFetcher<InputStream> {

    private Context mContext;
    private PoolListBean mPoolListBean;
    private volatile boolean mIsCancelled;

    public PoolListDataFetcher(Context context, PoolListBean poolListBean) {
        mContext = context;
        mPoolListBean = poolListBean;
    }

    @Override
    public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super InputStream> callback) {
        try {
            String url = OkHttp.getPoolPostUrl(mContext, mPoolListBean.linkToShow, 1);
            Response response = OkHttp.getInstance().execute(url);
            if (mIsCancelled) {
                response.close();
                return;
            } else if (response.isSuccessful()) {
                String html = response.body().string();
                response.close();
                List<ThumbBean> thumbList = HtmlParserFactory.createParser(mContext, html).getThumbListOfPool();
                if (thumbList.isEmpty()) {
                    return;
                }
                Response thumbResponse = OkHttp.getInstance().execute(thumbList.get(0).thumbUrl);
                if (mIsCancelled) {
                    thumbResponse.close();
                    return;
                } else if (thumbResponse.isSuccessful()) {
                    callback.onDataReady(thumbResponse.body().byteStream());
                } else {
                    onResponseFailed(callback, thumbResponse);
                }
                thumbResponse.close();
            } else {
                response.close();
                onResponseFailed(callback, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            callback.onLoadFailed(e);
        }
    }

    private void onResponseFailed(DataCallback callback, Response response) {
        callback.onLoadFailed(new IOException("Request failed with code: " + response.code()));
    }

    @Override
    public void cleanup() {
    }

    @Override
    public void cancel() {
        mIsCancelled = true;
    }

    @NonNull
    @Override
    public Class<InputStream> getDataClass() {
        return InputStream.class;
    }

    @NonNull
    @Override
    public DataSource getDataSource() {
        return DataSource.LOCAL;
    }

}
