package com.ess.anime.wallpaper.adapter;

import android.text.TextUtils;
import android.view.View;
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
import com.ess.anime.wallpaper.utils.FileUtils;
import com.lzy.okserver.OkDownload;
import com.lzy.okserver.download.DownloadTask;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import at.grabner.circleprogress.CircleProgressView;

public class RecyclerPixivGifDlAdapter extends BaseQuickAdapter<PixivGifBean, BaseViewHolder> implements IPixivDlListener {

    private final static int UPDATE_DL_STATE = 1;

    public RecyclerPixivGifDlAdapter(@Nullable List<PixivGifBean> data) {
        super(R.layout.recycler_item_download_manager, data);
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
                .placeholder(R.drawable.ic_placeholder_download_thumb)
                .priority(Priority.IMMEDIATE)
                .into((ImageView) holder.getView(R.id.iv_thumb));

        // id
        holder.setText(R.id.tv_id, "#" + pixivGifBean.id);

        // 下载状态
        CircleProgressView progressView = holder.getView(R.id.progress_view);
        progressView.setVisibility((pixivGifBean.isError || pixivGifBean.state == PixivGifBean.PixivDlState.CONNECT_PIXIV) ? View.GONE : View.VISIBLE);
        progressView.setMaxValue(104);
        String state = null;
        switch (pixivGifBean.state) {
            case CONNECT_PIXIV:
                state = pixivGifBean.isError
                        ? mContext.getString(R.string.pixiv_dl_state_connect_error)
                        : mContext.getString(R.string.pixiv_dl_state_connect);
                break;
            case DOWNLOAD_ZIP:
                if (pixivGifBean.isError) {
                    state = mContext.getString(R.string.pixiv_dl_state_download_zip_error);
                } else {
                    String tag = PixivGifDlManager.TAG + pixivGifBean.id;
                    DownloadTask task = OkDownload.getInstance().getTask(tag);
                    if (task == null || task.progress == null || task.progress.totalSize <= 0) {
                        state = mContext.getString(R.string.pixiv_dl_state_download_zip);
                    } else {
                        state = mContext.getString(R.string.pixiv_dl_state_download_zip_progress,
                                FileUtils.computeFileSize(task.progress.currentSize) + " / " + FileUtils.computeFileSize(task.progress.totalSize));
                    }
                    progressView.setValue(pixivGifBean.progress * 100f);
                }
                break;
            case EXTRACT_ZIP:
                state = pixivGifBean.isError
                        ? mContext.getString(R.string.pixiv_dl_state_unzip_error)
                        : mContext.getString(R.string.pixiv_dl_state_unzip);
                progressView.setValue(100);
                break;
            case MAKE_GIF:
                state = pixivGifBean.isError
                        ? mContext.getString(R.string.pixiv_dl_state_make_gif_error)
                        : mContext.getString(R.string.pixiv_dl_state_make_gif);
                progressView.setValue(102);
                break;
            case FINISH:
                state = mContext.getString(R.string.pixiv_dl_state_finish);
                progressView.setValue(104);
                break;
            case CANCEL:
                state = mContext.getString(R.string.pixiv_dl_state_cancelled);
                break;
            case NOT_GIF:
                state = mContext.getString(R.string.pixiv_dl_state_not_gif);
                break;
            case NEED_LOGIN:
                state = mContext.getString(R.string.pixiv_dl_state_no_artwork);
                break;
        }
        holder.setText(R.id.tv_state, state);

        // 重新下载按钮
        holder.setGone(R.id.btn_restart, pixivGifBean.isError
                && pixivGifBean.state != PixivGifBean.PixivDlState.NOT_GIF
                && pixivGifBean.state != PixivGifBean.PixivDlState.NEED_LOGIN);
        holder.getView(R.id.btn_restart).setOnClickListener(v -> {
            PixivGifDlManager.getInstance().execute(pixivGifBean.id);
        });

        // 删除按钮
        boolean isLoading = !pixivGifBean.isError && pixivGifBean.state != PixivGifBean.PixivDlState.FINISH;
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
