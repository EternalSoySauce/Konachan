package com.ess.anime.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ess.anime.R;

public class RecyclerSearchModePopupAdapter extends RecyclerView.Adapter<RecyclerSearchModePopupAdapter.MyViewHolder> {

    private Context mContext;
    private String[] mSearchModeArray;
    private int mSelectedPos;
    private OnItemClickListener mItemClickListener;

    public RecyclerSearchModePopupAdapter(Context context, @NonNull String[] searchModeArray) {
        mContext = context;
        mSearchModeArray = searchModeArray;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.recyclerview_item_popup_search_mode, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        // 搜索模式文字
        int colorRes = position == mSelectedPos ? R.color.color_text_selected : R.color.color_text_unselected;
        holder.tvSearchMode.setText(mSearchModeArray[position]);
        holder.tvSearchMode.setTextColor(mContext.getResources().getColor(colorRes));

        // 点击事件
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mSearchModeArray.length;
    }

    public String getItem(int position) {
        return mSearchModeArray[position];
    }

    public void setSelection(int position) {
        mSelectedPos = position;
        notifyDataSetChanged();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView tvSearchMode;

        public MyViewHolder(View itemView) {
            super(itemView);
            tvSearchMode = (TextView) itemView.findViewById(R.id.tv_search_mode);
        }
    }

    public interface OnItemClickListener {
        //加载图集里的图片列表
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }
}
