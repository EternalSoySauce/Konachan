package com.ess.anime.wallpaper.adapter;

import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Priority;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.glide.GlideApp;
import com.ess.anime.wallpaper.glide.MyGlideModule;
import com.ess.anime.wallpaper.pixiv.gif.IPixivDlListener;
import com.ess.anime.wallpaper.pixiv.gif.PixivGifBean;
import com.ess.anime.wallpaper.pixiv.gif.PixivGifDlManager;
import com.ess.anime.wallpaper.ui.view.CustomDialog;
import com.ess.anime.wallpaper.ui.view.image.PixivGifDlProgressView;

import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerPixivGifDlAdapter extends BaseQuickAdapter<PixivGifBean, BaseViewHolder> implements IPixivDlListener {

    private final static int UPDATE_DL_STATE = 1;

    public RecyclerPixivGifDlAdapter(@Nullable List<PixivGifBean> data) {
        super(R.layout.recycler_item_pixiv_gif_dl, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder holder, PixivGifBean pixivGifBean) {
        updateItemState(holder, pixivGifBean);
    }

    @Override
    protected void convertPayloads(@NonNull BaseViewHolder holder, PixivGifBean pixivGifBean, @NonNull List<Object> payloads) {
        for (Object payload : payloads) {
            if (payload.equals(UPDATE_DL_STATE)) {
                updateItemState(holder, pixivGifBean);
            }
        }
    }

    private void updateItemState(@NonNull BaseViewHolder holder, PixivGifBean pixivGifBean) {
        // 预览图
        Object url = TextUtils.isEmpty(pixivGifBean.thumbUrl) ? null
                : MyGlideModule.makeGlideUrlWithReferer(pixivGifBean.thumbUrl, pixivGifBean.getRefererUrl());
        GlideApp.with(mContext)
                .load(url)
                .placeholder(R.drawable.ic_placeholder_pixiv_gif_thumb)
                .priority(Priority.IMMEDIATE)
                .into((ImageView) holder.getView(R.id.iv_thumb));

        // id
        holder.setText(R.id.tv_id, "#" + pixivGifBean.id);

        // 下载状态
        PixivGifDlProgressView progressView = holder.getView(R.id.progress_view);
        String state = null;
        switch (pixivGifBean.state) {
            case CONNECT_PIXIV:
                state = pixivGifBean.isError ? "P站访问失败" : "正在连接P站";
                progressView.updateProgress(0, 0.8f);
                break;
            case DOWNLOAD_ZIP:
                String progress = String.format(Locale.getDefault(), "%.1f%%", pixivGifBean.progress * 100f);
                state = pixivGifBean.isError ? "下载失败" : "正在下载压缩包: " + progress;
                progressView.updateProgress(1, pixivGifBean.progress);
                break;
            case EXTRACT_ZIP:
                state = pixivGifBean.isError ? "解压失败" : "正在解压缩";
                progressView.updateProgress(2, 0.8f);
                break;
            case MAKE_GIF:
                state = pixivGifBean.isError ? "合成GIF失败" : "正在合成GIF";
                progressView.updateProgress(3, pixivGifBean.progress);
                break;
            case FINISH:
                state = "合成GIF完毕，已保存到我的收藏";
                progressView.updateProgress(4, 1);
                break;
            case CANCEL:
                state = "任务已取消";
                progressView.updateProgress(-1, 0);
                break;
            case NOT_GIF:
                state = "不是gif";
                progressView.updateProgress(1, 0);
                break;
            case NEED_LOGIN:
                state = "作品不存在";
                progressView.updateProgress(1, 0);
                break;
        }
        holder.setText(R.id.tv_state, state);

        // loading图标
        boolean isLoading = !pixivGifBean.isError && pixivGifBean.state != PixivGifBean.PixivDlState.FINISH;
        holder.setGone(R.id.loading_view, isLoading);

        // 重新下载按钮
        holder.setGone(R.id.btn_restart, pixivGifBean.isError);
        holder.getView(R.id.btn_restart).setOnClickListener(v -> {
            PixivGifDlManager.getInstance().execute(pixivGifBean.id);
        });

        // 删除按钮
        holder.getView(R.id.iv_delete).setOnClickListener(v -> {
            if (isLoading) {
                CustomDialog.showDeleteWhenDownloadingItemDialog(mContext, new CustomDialog.SimpleDialogActionListener() {
                    @Override
                    public void onPositive() {
                        super.onPositive();
                        PixivGifDlManager.getInstance().delete(pixivGifBean.id);
                    }
                });
            } else {
                PixivGifDlManager.getInstance().delete(pixivGifBean.id);
            }
        });
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        PixivGifDlManager.getInstance().addListener(this);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        PixivGifDlManager.getInstance().removeListener(this);
    }

    @Override
    public void onDataAdded(PixivGifBean pixivGifBean) {
        if (!mData.contains(pixivGifBean)) {
            addData(0, pixivGifBean);
            RecyclerView recyclerView = getRecyclerView();
            if (recyclerView != null) {
                recyclerView.scrollToPosition(0);
            }
        }
    }

    @Override
    public void onDataRemoved(PixivGifBean pixivGifBean) {
        int pos = mData.indexOf(pixivGifBean);
        if (pos != -1) {
            remove(pos);
        }
    }

    @Override
    public void onDataChanged(PixivGifBean pixivGifBean) {
        int pos = mData.indexOf(pixivGifBean);
        if (pos != -1) {
            mData.set(pos, pixivGifBean);
            refreshNotifyItemChanged(pos, UPDATE_DL_STATE);
        }
    }
}
