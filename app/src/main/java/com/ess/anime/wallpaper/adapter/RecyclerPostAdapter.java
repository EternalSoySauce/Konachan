package com.ess.anime.wallpaper.adapter;

import android.app.Activity;
import android.content.Intent;
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
import com.ess.anime.wallpaper.http.HandlerFuture;
import com.ess.anime.wallpaper.http.OkHttp;
import com.ess.anime.wallpaper.http.parser.HtmlParserFactory;
import com.ess.anime.wallpaper.model.holder.ImageDataHolder;
import com.ess.anime.wallpaper.ui.activity.ImageDetailActivity;
import com.ess.anime.wallpaper.utils.ComponentUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class RecyclerPostAdapter extends BaseQuickAdapter<ThumbBean, BaseViewHolder> {

    private String mHttpTag;
    private OnItemClickListener mItemClickListener;

    public RecyclerPostAdapter(String httpTag) {
        super(R.layout.recyclerview_item_post);
        mHttpTag = httpTag;
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
        holder.itemView.setOnClickListener(v -> {
            int index = holder.getLayoutPosition() - getHeaderLayoutCount();
            ImageDataHolder.setThumbList(mData, index);
            Intent intent = new Intent(mContext, ImageDetailActivity.class);
            intent.putExtra(Constants.THUMB_BEAN, thumbBean);
            mContext.startActivity(intent);

            if (mItemClickListener != null) {
                mItemClickListener.onViewDetails();
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
                String url = thumbBean.linkToShow;
                OkHttp.connect(url, mHttpTag, new OkHttp.OkHttpCallback() {
                    @Override
                    public void onFailure() {
                        OkHttp.connect(url, mHttpTag, this);
                    }

                    @Override
                    public void onSuccessful(String body) {
                        HandlerFuture.ofWork(body)
                                .applyThen(body1 -> {
                                    return HtmlParserFactory.createParser(mContext, body1).getImageDetailJson();
                                })
                                .runOn(HandlerFuture.IO.UI)
                                .applyThen(json -> {
                                    // 发送通知到PostFragment, PoolFragment, ImageFragment, DetailFragment
                                    EventBus.getDefault().post(new MsgBean(Constants.GET_IMAGE_DETAIL, json));
                                });
                    }
                });
            }
        }
    }

    private void preloadThumbnail(List<ThumbBean> thumbList) {
        for (ThumbBean thumbBean : thumbList) {
            if (ComponentUtils.isActivityActive((Activity) mContext)) {
                GlideApp.with(mContext)
                        .load(MyGlideModule.makeGlideUrl(thumbBean.thumbUrl))
                        .submit();
            }
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
