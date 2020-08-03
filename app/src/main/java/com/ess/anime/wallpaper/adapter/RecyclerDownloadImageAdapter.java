package com.ess.anime.wallpaper.adapter;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Priority;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.download.image.DownloadBean;
import com.ess.anime.wallpaper.download.image.DownloadImageManager;
import com.ess.anime.wallpaper.download.image.DownloadImageService;
import com.ess.anime.wallpaper.download.image.IDownloadImageListener;
import com.ess.anime.wallpaper.glide.GlideApp;
import com.ess.anime.wallpaper.glide.MyGlideModule;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.http.OkHttp;
import com.ess.anime.wallpaper.ui.view.CustomDialog;
import com.ess.anime.wallpaper.utils.FileUtils;
import com.lzy.okgo.model.Progress;
import com.lzy.okserver.OkDownload;
import com.lzy.okserver.download.DownloadTask;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import at.grabner.circleprogress.CircleProgressView;

public class RecyclerDownloadImageAdapter extends BaseQuickAdapter<DownloadBean, BaseViewHolder> implements IDownloadImageListener {

    private final static int UPDATE_DL_STATE = 1;

    private OnDataSizeChangedListener mDataSizeChangedListener;

    public RecyclerDownloadImageAdapter(@Nullable List<DownloadBean> data) {
        super(R.layout.recycler_item_download_manager, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder holder, DownloadBean downloadBean) {
        updateItemState(holder, downloadBean);
    }

    @Override
    protected void convertPayloads(@NonNull BaseViewHolder holder, DownloadBean downloadBean, @NonNull List<Object> payloads) {
        for (Object payload : payloads) {
            if (payload.equals(UPDATE_DL_STATE)) {
                updateItemState(holder, downloadBean);
            }
        }
    }

    private void updateItemState(@NonNull BaseViewHolder holder, DownloadBean downloadBean) {
        // 预览图
        GlideApp.with(mContext)
                .load(MyGlideModule.makeGlideUrl(downloadBean.thumbUrl))
                .placeholder(R.drawable.ic_placeholder_download_thumb)
                .priority(Priority.IMMEDIATE)
                .into((ImageView) holder.getView(R.id.iv_thumb));

        // id
        holder.setText(R.id.tv_id, downloadBean.downloadTitle);

        // 下载状态
        CircleProgressView progressView = holder.getView(R.id.progress_view);
        String tag = downloadBean.downloadUrl;
        DownloadTask task = OkDownload.getInstance().getTask(tag);
        progressView.setVisibility((task == null || task.progress.status == Progress.ERROR) ? View.GONE : View.VISIBLE);
        if (task == null) {
            progressView.setValue(0);
            holder.setText(R.id.tv_state, R.string.download_waiting);
        } else {
            switch (task.progress.status) {
                case Progress.NONE:
                case Progress.WAITING:
                    progressView.setValue((task.progress.fraction * 100));
                    holder.setText(R.id.tv_state, R.string.download_waiting);
                    break;
                case Progress.LOADING:
                case Progress.PAUSE:
                    progressView.setValue((task.progress.fraction * 100));
                    holder.setText(R.id.tv_state, FileUtils.computeFileSize(task.progress.currentSize) + " / " + FileUtils.computeFileSize(task.progress.totalSize));
                    break;
                case Progress.ERROR:
                    holder.setText(R.id.tv_state, R.string.download_failed);
                    break;
                case Progress.FINISH:
                    progressView.setValue(100);
                    holder.setText(R.id.tv_state, R.string.download_finish);
                    break;
            }
        }

        // 重新下载按钮
        holder.setGone(R.id.btn_restart, task != null && task.progress.status == Progress.ERROR);
        holder.getView(R.id.btn_restart).setOnClickListener(v -> {
            if (!OkHttp.isUrlInDownloadQueue(downloadBean.downloadUrl)) {
                Intent downloadIntent = new Intent(mContext, DownloadImageService.class);
                downloadIntent.putExtra(Constants.DOWNLOAD_BEAN, downloadBean);
                mContext.startService(downloadIntent);
                OkHttp.addUrlToDownloadQueue(downloadBean.downloadUrl);
            }
        });

        // 删除按钮
        boolean isLoading = task == null || (task.progress.status != Progress.ERROR && task.progress.status != Progress.FINISH);
        holder.getView(R.id.iv_delete).setOnClickListener(v -> {
            if (isLoading) {
                CustomDialog.showDeleteWhenDownloadingItemDialog(mContext, new CustomDialog.SimpleDialogActionListener() {
                    @Override
                    public void onPositive() {
                        super.onPositive();
                        OkHttp.cancelDownloadFile(tag);
                    }
                });
            } else {
                OkHttp.cancelDownloadFile(tag);
            }
        });
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        DownloadImageManager.getInstance().addListener(this);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        DownloadImageManager.getInstance().removeListener(this);
    }

    @Override
    public void onDataAdded(DownloadBean downloadBean) {
        if (!mData.contains(downloadBean)) {
            addData(0, downloadBean);
            RecyclerView recyclerView = getRecyclerView();
            if (recyclerView != null) {
                recyclerView.scrollToPosition(0);
            }
            notifyDataSizeChanged();
        }
    }

    @Override
    public void onDataRemoved(DownloadBean downloadBean) {
        int pos = mData.indexOf(downloadBean);
        if (pos != -1) {
            remove(pos);
            notifyDataSizeChanged();
        }
    }

    @Override
    public void onDataChanged(DownloadBean downloadBean) {
        int pos = mData.indexOf(downloadBean);
        if (pos != -1) {
            mData.set(pos, downloadBean);
            refreshNotifyItemChanged(pos, UPDATE_DL_STATE);
        }
    }

    private void notifyDataSizeChanged() {
        if (mDataSizeChangedListener != null) {
            mDataSizeChangedListener.onDataChanged(mData.isEmpty());
        }
    }

    public void setOnDataSizeChangedListener(OnDataSizeChangedListener listener) {
        mDataSizeChangedListener = listener;
        notifyDataSizeChanged();
    }

    public interface OnDataSizeChangedListener {
        void onDataChanged(boolean isEmpty);
    }

}
