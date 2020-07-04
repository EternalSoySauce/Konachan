package com.ess.anime.wallpaper.adapter;

import android.text.TextUtils;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.glide.GlideApp;
import com.ess.anime.wallpaper.glide.MyGlideModule;
import com.ess.anime.wallpaper.pixiv.gif.IPixivDlListener;
import com.ess.anime.wallpaper.pixiv.gif.PixivGifBean;
import com.ess.anime.wallpaper.pixiv.gif.PixivGifDlManager;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerPixivGifDlAdapter extends BaseQuickAdapter<PixivGifBean, BaseViewHolder> implements IPixivDlListener {

    public RecyclerPixivGifDlAdapter(@Nullable List<PixivGifBean> data) {
        super(R.layout.recycler_item_pixiv_gif_dl, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder holder, PixivGifBean pixivGifBean) {
        if (!TextUtils.isEmpty(pixivGifBean.thumbUrl)) {
            GlideApp.with(mContext)
                    .load(MyGlideModule.makeGlideUrlWithReferer(pixivGifBean.thumbUrl, pixivGifBean.getRefererUrl()))
                    .into((ImageView) holder.getView(R.id.iv_thumb));
        } else {
            holder.setImageDrawable(R.id.iv_thumb, null);
        }
        holder.setText(R.id.tv_id, "#" + pixivGifBean.id);

        String state = null;
        switch (pixivGifBean.state) {
            case CONNECT_PIXIV:
                state = pixivGifBean.isError ? "P站访问失败" : "正在连接P站";
                break;
            case DOWNLOAD_ZIP:
                state = pixivGifBean.isError ? "下载失败" : "正在下载压缩包: " + pixivGifBean.progress;
                break;
            case EXTRACT_ZIP:
                state = pixivGifBean.isError ? "解压失败" : "正在解压缩";
                break;
            case MAKE_GIF:
                state = pixivGifBean.isError ? "合成GIF失败" : "正在合成GIF：" + pixivGifBean.progress;
                break;
            case FINISH:
                state = "合成GIF完毕，已保存到我的收藏";
                break;
            case CANCEL:
                state = "任务已取消";
                break;
            case NOT_GIF:
                state = "不是gif";
                break;
            case NEED_LOGIN:
                state = "作品不存在";
                break;
        }
        holder.setText(R.id.tv_state, state);
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
            refreshNotifyItemChanged(pos);
        }
    }
}
