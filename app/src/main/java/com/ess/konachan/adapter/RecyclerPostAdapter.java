package com.ess.konachan.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Priority;
import com.ess.konachan.R;
import com.ess.konachan.bean.MsgBean;
import com.ess.konachan.bean.ThumbBean;
import com.ess.konachan.global.Constants;
import com.ess.konachan.http.OkHttp;
import com.ess.konachan.http.ParseHtml;
import com.ess.konachan.other.GlideApp;
import com.ess.konachan.ui.activity.ImageDetailActivity;
import com.wang.avi.AVLoadingIndicatorView;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class RecyclerPostAdapter extends RecyclerView.Adapter<RecyclerPostAdapter.MyViewHolder> {

    private Activity mActivity;
    private ArrayList<ThumbBean> mThumbList;
    private ArrayList<Call> mCallList;
    private OnItemClickListener mItemClickListener;
    private ViewState mCurrentState;

    public RecyclerPostAdapter(Activity activity, @NonNull ArrayList<ThumbBean> thumbList) {
        mActivity = activity;
        mThumbList = thumbList;
        mCallList = new ArrayList<>();
        getImageDetail(thumbList);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId = viewType == ViewState.NORMAL.ordinal() ? R.layout.recyclerview_item_post
                : R.layout.layout_load_more;
        View view = LayoutInflater.from(mActivity).inflate(layoutId, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1) {
            return ViewState.LOAD_MORE.ordinal();
        } else {
            return ViewState.NORMAL.ordinal();
        }
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        if (position == getItemCount() - 1) {
            ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
            params.height = mCurrentState == ViewState.LOAD_MORE ? ViewGroup.LayoutParams.WRAP_CONTENT : 0;
            holder.indicatorView.smoothToShow();
            return;
        }

        final ThumbBean thumbBean = mThumbList.get(position);

        //缩略图
        GlideApp.with(mActivity)
                .load(thumbBean.thumbUrl)
                .placeholder(R.drawable.ic_placeholder_post)
                .priority(Priority.HIGH)
                .into(holder.ivThumb);

        //尺寸
        holder.tvSize.setText(thumbBean.realSize);

        //点击进入详细页面
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, ImageDetailActivity.class);
                intent.putExtra(Constants.THUMB_BEAN, thumbBean);
                mActivity.startActivity(intent);

                if (mItemClickListener != null) {
                    mItemClickListener.onViewDetails();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mThumbList.size() + 1;
    }

    public ArrayList<ThumbBean> getThumbList() {
        return mThumbList;
    }

    public void loadMoreDatas(ArrayList<ThumbBean> imageList) {
        int position = getItemCount() - 1;
        addDatas(position, imageList);
    }

    public void refreshDatas(ArrayList<ThumbBean> imageList) {
        addDatas(0, imageList);
    }

    private void addDatas(int position, ArrayList<ThumbBean> thumbList) {
        synchronized (this) {
            //删掉更新时因网站新增图片导致thumbList出现的重复项
            Iterator<ThumbBean> iterator = thumbList.iterator();
            while (iterator.hasNext()) {
                ThumbBean newData = iterator.next();
                for (ThumbBean thumbBean : mThumbList) {
                    if (newData.thumbUrl.equals(thumbBean.thumbUrl)) {
                        iterator.remove();
                        break;
                    }
                }
            }

            mThumbList.addAll(position, thumbList);
            notifyDataSetChanged();
            getImageDetail(thumbList);
            preloadThumbnail(thumbList);
        }
    }

    private void getImageDetail(ArrayList<ThumbBean> thumbList) {
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
                            String json = ParseHtml.getImageDetailJson(html);
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

    private void preloadThumbnail(ArrayList<ThumbBean> thumbList) {
        for (ThumbBean thumbBean : thumbList) {
            if (!mActivity.isDestroyed()) {
                GlideApp.with(mActivity)
                        .load(thumbBean.thumbUrl)
                        .priority(Priority.NORMAL)
                        .submit();
            }
        }
    }

    public void changeToLoadMoreState() {
        mCurrentState = ViewState.LOAD_MORE;
        notifyDataSetChanged();
    }

    public void changeToNormalState() {
        mCurrentState = ViewState.NORMAL;
        notifyDataSetChanged();
    }

    public void clear() {
        mThumbList.clear();
        changeToNormalState();
    }

    public void cancelAll() {
        for (Call call : mCallList) {
            call.cancel();
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivThumb;
        private TextView tvSize;
        private AVLoadingIndicatorView indicatorView;

        public MyViewHolder(android.view.View itemView) {
            super(itemView);
            ivThumb = (ImageView) itemView.findViewById(R.id.iv_post_thumb);
            tvSize = (TextView) itemView.findViewById(R.id.tv_size);
            indicatorView = (AVLoadingIndicatorView) itemView.findViewById(R.id.view_load_more);
        }
    }

    private enum ViewState {
        NORMAL,
        LOAD_MORE
    }

    public interface OnItemClickListener {
        //进入图片详细界面时收起fab
        void onViewDetails();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }
}
