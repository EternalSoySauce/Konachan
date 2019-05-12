package com.ess.anime.wallpaper.adapter;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Priority;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.bean.MsgBean;
import com.ess.anime.wallpaper.bean.ThumbBean;
import com.ess.anime.wallpaper.glide.GlideApp;
import com.ess.anime.wallpaper.glide.MyGlideModule;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.http.OkHttp;
import com.ess.anime.wallpaper.http.parser.HtmlParserFactory;
import com.ess.anime.wallpaper.model.holder.ImageDataHolder;
import com.ess.anime.wallpaper.ui.activity.ImageDetailActivity;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class RecyclerPostAdapter extends BaseQuickAdapter<ThumbBean, BaseViewHolder> {

    private ArrayList<Call> mCallList = new ArrayList<>();
    private OnItemClickListener mItemClickListener;

    public RecyclerPostAdapter() {
        super(R.layout.recyclerview_item_post);
    }

    @Override
    protected void convert(final BaseViewHolder holder, final ThumbBean thumbBean) {
        //缩略图
        GlideApp.with(mContext)
                .load(MyGlideModule.makeGlideUrl(thumbBean.thumbUrl))
                .placeholder(R.drawable.ic_placeholder_post)
                .priority(Priority.HIGH)
                .into((ImageView) holder.getView(R.id.iv_post_thumb));

        //尺寸
        holder.setText(R.id.tv_size, thumbBean.realSize);

        //点击进入详细页面
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = holder.getLayoutPosition() - getHeaderLayoutCount();
                ImageDataHolder.setThumbList(mData, index);
                Intent intent = new Intent(mContext, ImageDetailActivity.class);
                intent.putExtra(Constants.THUMB_BEAN, thumbBean);
                mContext.startActivity(intent);

                if (mItemClickListener != null) {
                    mItemClickListener.onViewDetails();
                }
            }
        });
    }

    public boolean loadMoreDatas(List<ThumbBean> imageList) {
        return addDatas(mData.size(), imageList);
    }

    public boolean refreshDatas(List<ThumbBean> imageList) {
        return addDatas(0, imageList);
    }

    private boolean addDatas(int position, List<ThumbBean> thumbList) {
        synchronized (this) {
            //删掉更新时因网站新增图片导致thumbList出现的重复项
            thumbList.removeAll(mData);
            if (!thumbList.isEmpty()) {
                addData(position, thumbList);
                getImageDetail(thumbList);
                preloadThumbnail(thumbList);
                return true;
            }
            return false;
        }
    }

    private void getImageDetail(List<ThumbBean> thumbList) {
        for (final ThumbBean thumbBean : thumbList) {
            if (thumbBean.imageBean == null) {
                Call call = OkHttp.getInstance().connect(thumbBean.linkToShow, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        if (OkHttp.isNetworkProblem(e)) {
                            Call newCall = OkHttp.getInstance().connect(thumbBean.linkToShow, this);
                            mCallList.add(newCall);
                        }
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String html = response.body().string();
                            String json = HtmlParserFactory.createParser(mContext, html).getImageDetailJson();
                            // 发送通知到PostFragment, PoolFragment, ImageFragment, DetailFragment
                            EventBus.getDefault().post(new MsgBean(Constants.GET_IMAGE_DETAIL, json));
                        } else {
                            Call newCall = OkHttp.getInstance().connect(thumbBean.linkToShow, this);
                            mCallList.add(newCall);
                        }
                        response.close();
                    }
                });
                mCallList.add(call);
            }
        }
    }

    private void preloadThumbnail(List<ThumbBean> thumbList) {
        try {
            for (ThumbBean thumbBean : thumbList) {
                GlideApp.with(mContext)
                        .load(MyGlideModule.makeGlideUrl(thumbBean.thumbUrl))
                        .submit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cancelAll() {
        for (Call call : mCallList) {
            call.cancel();
        }
    }

    public interface OnItemClickListener {
        //进入图片详细界面时收起fab
        void onViewDetails();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }

}
