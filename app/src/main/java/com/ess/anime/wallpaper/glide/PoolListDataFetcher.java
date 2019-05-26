package com.ess.anime.wallpaper.glide;

import android.content.Context;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import com.ess.anime.wallpaper.bean.PoolListBean;
import com.ess.anime.wallpaper.bean.ThumbBean;
import com.ess.anime.wallpaper.http.OkHttp;
import com.ess.anime.wallpaper.http.parser.HtmlParserFactory;
import com.yanzhenjie.kalle.Kalle;
import com.yanzhenjie.kalle.simple.SimpleResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import androidx.annotation.NonNull;

public class PoolListDataFetcher implements DataFetcher<InputStream> {

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
    public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super InputStream> callback) {
        try {
            String url = OkHttp.getPoolPostUrl(mContext, mPoolListBean.linkToShow, 1);
            SimpleResponse<String, String> response = OkHttp.execute(url, mHttpTag);
            if (mIsCancelled) {
                return;
            } else if (response.isSucceed()) {
                String html = response.succeed();
                List<ThumbBean> thumbList = HtmlParserFactory.createParser(mContext, html).getThumbListOfPool();
                if (thumbList.isEmpty()) {
                    return;
                }
                SimpleResponse<String, String> thumbResponse = OkHttp.execute(thumbList.get(0).thumbUrl, mHttpTag);
                if (mIsCancelled) {
                    return;
                } else if (thumbResponse.isSucceed()) {
                    // todo response
//                    callback.onDataReady(thumbResponse.succeed());
                } else {
                    onResponseFailed(callback, thumbResponse);
                }
            } else {
                onResponseFailed(callback, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            callback.onLoadFailed(e);
        }
    }

    private void onResponseFailed(DataCallback callback, SimpleResponse<String, String> response) {
        callback.onLoadFailed(new IOException("Request failed with code: " + response.code()));
    }

    @Override
    public void cleanup() {
        Kalle.cancel(mPoolListBean.linkToShow);
    }

    @Override
    public void cancel() {
        mIsCancelled = true;
        Kalle.cancel(mHttpTag);
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
