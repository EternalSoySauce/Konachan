package com.ess.anime.wallpaper.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
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
import com.ess.anime.wallpaper.model.holder.ImageDataHolder;
import com.ess.anime.wallpaper.ui.activity.ImageDetailActivity;
import com.ess.anime.wallpaper.utils.ComponentUtils;
import com.ess.anime.wallpaper.website.WebsiteManager;

import org.greenrobot.eventbus.EventBus;
import org.jsoup.Jsoup;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

public class RecyclerPostAdapter extends BaseQuickAdapter<ThumbBean, BaseViewHolder> {

    private final static int REPLACE_DATA = 1;

    private String mHttpTag;
    private OnItemClickListener mItemClickListener;
    private boolean mIsRectangular = true;  // 缩略图是否为方格

    public RecyclerPostAdapter(String httpTag) {
        super(R.layout.recyclerview_item_post);
        mHttpTag = httpTag;
    }

    @Override
    protected void convert(@NonNull BaseViewHolder holder, @NonNull ThumbBean thumbBean) {
        //缩略图尺寸（方格/瀑布流）
        ImageView ivThumb = holder.getView(R.id.iv_post_thumb);
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) ivThumb.getLayoutParams();
        if (mIsRectangular) {
            layoutParams.dimensionRatio = "165:130";
        } else if (thumbBean.thumbHeight / thumbBean.thumbWidth >= 3) {
            layoutParams.dimensionRatio = "1:3";
        } else if (thumbBean.thumbWidth / thumbBean.thumbHeight >= 2) {
            layoutParams.dimensionRatio = "2:1";
        } else {
            layoutParams.dimensionRatio = thumbBean.thumbWidth + ":" + thumbBean.thumbHeight;
        }
        ivThumb.setLayoutParams(layoutParams);

        //缩略图
        ivThumb.setScaleType(ImageView.ScaleType.FIT_CENTER);
        GlideApp.with(mContext)
                .load(MyGlideModule.makeGlideUrl(thumbBean.thumbUrl))
                .placeholder(R.drawable.ic_placeholder_post)
                .priority(Priority.HIGH)
                .override(thumbBean.thumbWidth, thumbBean.thumbHeight)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        ivThumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        return false;
                    }
                })
                .into(ivThumb);

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

    @Override
    protected void convertPayloads(@NonNull BaseViewHolder holder, ThumbBean thumbBean, @NonNull List<Object> payloads) {
        super.convertPayloads(holder, thumbBean, payloads);
        for (Object payload : payloads) {
            if (payload.equals(REPLACE_DATA)) {
                //尺寸
                holder.setText(R.id.tv_size, thumbBean.realSize);
            }
        }
    }

    // 切换缩略图显示尺寸（方格/瀑布流）
    public void changeImageShownFormat(boolean isRectangular) {
        if (mIsRectangular != isRectangular) {
            mIsRectangular = isRectangular;
            notifyItemRangeChanged(getHeaderLayoutCount(), getData().size());
        }
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
        for (ThumbBean thumbBean : thumbList) {
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
                                    return WebsiteManager.getInstance()
                                            .getWebsiteConfig()
                                            .getHtmlParser()
                                            .getImageDetailJson(Jsoup.parse(body1));
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
                MyGlideModule.preloadImage(mContext, thumbBean.thumbUrl);
            }
        }
    }

    public void replaceData(ThumbBean newThumb) {
        int index = mData.indexOf(newThumb);
        if (index != -1) {
            mData.set(index, newThumb);
            notifyItemChanged(index, REPLACE_DATA);
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
