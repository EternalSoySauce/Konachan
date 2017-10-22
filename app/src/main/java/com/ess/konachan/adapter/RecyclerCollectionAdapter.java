package com.ess.konachan.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ess.konachan.R;
import com.ess.konachan.bean.CollectionBean;
import com.ess.konachan.global.GlideConfig;
import com.ess.konachan.utils.UIUtils;
import com.ess.konachan.utils.VibratorUtils;
import com.mixiaoxiao.smoothcompoundbutton.SmoothCheckBox;

import java.util.ArrayList;

public class RecyclerCollectionAdapter extends RecyclerView.Adapter<RecyclerCollectionAdapter.MyViewHolder> {

    private Context mContext;
    private ArrayList<CollectionBean> mCollectionList;
    private ArrayList<CollectionBean> mChooseList;
    private boolean mEditing;
    private OnActionListener mActionListener;

    public RecyclerCollectionAdapter(Context context, ArrayList<CollectionBean> collectionList) {
        mContext = context;
        mCollectionList = collectionList;
        mChooseList = new ArrayList<>();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.recyclerview_item_collection, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final CollectionBean collectionBean = mCollectionList.get(position);

        if (mEditing) {
            holder.cbChoose.setVisibility(View.VISIBLE);
            holder.cbChoose.setChecked(collectionBean.isChecked);
            holder.cbChoose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isChecked = holder.cbChoose.isChecked();
                    setChooseState(collectionBean, isChecked);
                }
            });
        } else {
            holder.cbChoose.setVisibility(View.GONE);
            holder.cbChoose.setChecked(false);
            setChooseState(collectionBean, false);
        }

        int slideLength = (int) ((UIUtils.getWindowSize(mContext)[0] - UIUtils.dp2px(mContext, 6)) / 3f);
        holder.ivCollection.getLayoutParams().width = slideLength;
        holder.ivCollection.getLayoutParams().height = slideLength;
        GlideConfig.getInstance().loadImage(mContext, collectionBean.url, holder.ivCollection);
        holder.ivCollection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEditing) {
                    boolean isChecked = !holder.cbChoose.isChecked();
                    holder.cbChoose.setChecked(isChecked);
                    setChooseState(collectionBean, isChecked);
                } else {
                    if (mActionListener != null) {
                        mActionListener.onFullScreen(holder.ivCollection, holder.getAdapterPosition());
                    }
                }
            }
        });
        holder.ivCollection.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!mEditing) {
                    mEditing = true;
                    setChooseState(collectionBean, true);
                    notifyDataSetChanged();
                    VibratorUtils.Vibrate(mContext, 12);
                    if (mActionListener != null) {
                        mActionListener.onEdit();
                    }
                }
                return false;
            }
        });
        holder.ivCollection.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        holder.itemView.setScaleX(0.96f);
                        holder.itemView.setScaleY(0.96f);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        holder.itemView.setScaleX(1f);
                        holder.itemView.setScaleY(1f);
                        break;
                }
                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return mCollectionList == null ? 0 : mCollectionList.size();
    }

    public void removeDatas(ArrayList<CollectionBean> deleteList) {
        for (CollectionBean collectionBean : deleteList) {
            int position = mCollectionList.indexOf(collectionBean);
            if (position != -1) {
                mCollectionList.remove(position);
                notifyItemRemoved(position);
            }
        }
        notifyItemRangeChanged(0, mCollectionList.size());
    }

    private void setChooseState(CollectionBean collectionBean, boolean isChecked) {
        collectionBean.isChecked = isChecked;
        if (isChecked) {
            mChooseList.add(collectionBean);
        } else {
            mChooseList.remove(collectionBean);
        }
    }

    public ArrayList<CollectionBean> getCollectionList() {
        return mCollectionList;
    }

    public ArrayList<CollectionBean> getChooseList() {
        return mChooseList;
    }

    public void resetChooseList() {
        mChooseList.clear();
    }

    public void setEditing(boolean editing) {
        this.mEditing = editing;
    }

    public void beginEdit(){

    }

    public void cancelEdit() {

    }

    public boolean isEditing() {
        return mEditing;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivCollection;
        private SmoothCheckBox cbChoose;

        public MyViewHolder(View itemView) {
            super(itemView);
            ivCollection = (ImageView) itemView.findViewById(R.id.iv_collection);
            cbChoose = (SmoothCheckBox) itemView.findViewById(R.id.cb_choose);
        }
    }

    public interface OnActionListener {
        //收藏界面adapter中点击后通知进入全屏查看模式
        void onFullScreen(ImageView imageView, int position);

        //收藏界面adapter中长按后通知toolbar切换编辑模式
        void onEdit();
    }

    public void setOnActionListener(OnActionListener listener) {
        mActionListener = listener;
    }
}
