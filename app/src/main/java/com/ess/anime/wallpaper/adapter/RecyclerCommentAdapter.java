package com.ess.anime.wallpaper.adapter;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Priority;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.bean.CommentBean;
import com.ess.anime.wallpaper.glide.GlideApp;
import com.ess.anime.wallpaper.glide.MyGlideModule;
import com.ess.anime.wallpaper.ui.activity.web.HyperlinkActivity;
import com.ess.anime.wallpaper.utils.WebLinkMethod;
import com.ess.anime.wallpaper.website.WebsiteManager;

import java.util.Map;

public class RecyclerCommentAdapter extends BaseQuickAdapter<CommentBean, BaseViewHolder> {

    public RecyclerCommentAdapter() {
        super(R.layout.recyclerview_item_comment, null);
    }

    @Override
    protected void convert(BaseViewHolder holder, CommentBean commentBean) {
        //头像
        Map<String, String> headerMap = WebsiteManager.getInstance().getRequestHeaders();
        GlideApp.with(mContext)
                .load(MyGlideModule.makeGlideUrl(commentBean.avatar, headerMap))
                .placeholder(R.drawable.ic_placeholder_comment)
                .circleCrop()
                .priority(Priority.NORMAL)
                .into((ImageView) holder.getView(R.id.iv_head));

        //作者
        holder.setText(R.id.tv_author, commentBean.author);

        //id
        holder.setText(R.id.tv_id, commentBean.id);

        //时间
        holder.setText(R.id.tv_date, commentBean.date);

        //引用
        boolean hasQuote = !TextUtils.isEmpty(commentBean.quote);
        TextView tvQuote = holder.getView(R.id.tv_quote);
        tvQuote.setVisibility(hasQuote ? View.VISIBLE : View.GONE);
        tvQuote.setText(commentBean.quote);
        tvQuote.setMovementMethod(WebLinkMethod.getInstance().setOnHyperlinkListener(url -> HyperlinkActivity.launch(mContext, url)));

        //评论
        TextView tvComment = holder.getView(R.id.tv_comment);
        tvComment.setText(commentBean.comment);
        tvComment.setMovementMethod(WebLinkMethod.getInstance().setOnHyperlinkListener(url -> HyperlinkActivity.launch(mContext, url)));
    }

}
