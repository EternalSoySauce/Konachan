package com.ess.wallpaper.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Priority;
import com.ess.wallpaper.bean.CommentBean;
import com.ess.wallpaper.R;
import com.ess.wallpaper.other.GlideApp;
import com.ess.wallpaper.other.MyGlideModule;

import java.util.ArrayList;

public class RecyclerCommentAdapter extends RecyclerView.Adapter<RecyclerCommentAdapter.MyViewHolder> {

    private Context mContext;
    private ArrayList<CommentBean> mCommentList;

    public RecyclerCommentAdapter(Context context, ArrayList<CommentBean> commentList) {
        mContext = context;
        mCommentList = commentList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.recyclerview_item_comment, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        CommentBean commentBean = mCommentList.get(position);

        //头像
        GlideApp.with(mContext)
                .load(MyGlideModule.makeGlideUrl(commentBean.headUrl))
                .placeholder(R.drawable.ic_placeholder_comment)
                .priority(Priority.NORMAL)
                .into(holder.ivHead);

        //作者
        holder.tvAuthor.setText(commentBean.author);

        //id
        holder.tvId.setText(commentBean.id);

        //时间
        holder.tvDate.setText(commentBean.date);

        //引用
        if (TextUtils.isEmpty(commentBean.quote)) {
            holder.tvQuote.setVisibility(View.GONE);
        } else {
            holder.tvQuote.setVisibility(View.VISIBLE);
            holder.tvQuote.setText(commentBean.quote);
            holder.tvQuote.setMovementMethod(LinkMovementMethod.getInstance());
        }

        //评论
        holder.tvComment.setText(commentBean.comment);
        holder.tvComment.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public int getItemCount() {
        return mCommentList == null ? 0 : mCommentList.size();
    }

    public ArrayList<CommentBean> getCommentList() {
        return mCommentList;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivHead;
        private TextView tvAuthor;
        private TextView tvId;
        private TextView tvDate;
        private TextView tvQuote;
        private TextView tvComment;

        public MyViewHolder(View itemView) {
            super(itemView);
            ivHead = (ImageView) itemView.findViewById(R.id.iv_head);
            tvAuthor = (TextView) itemView.findViewById(R.id.tv_author);
            tvId = (TextView) itemView.findViewById(R.id.tv_id);
            tvDate = (TextView) itemView.findViewById(R.id.tv_date);
            tvQuote = (TextView) itemView.findViewById(R.id.tv_quote);
            tvComment = (TextView) itemView.findViewById(R.id.tv_comment);
        }
    }
}
