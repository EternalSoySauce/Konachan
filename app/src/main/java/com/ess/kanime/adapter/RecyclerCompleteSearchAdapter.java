package com.ess.kanime.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ess.kanime.R;

import java.util.ArrayList;
import java.util.Set;

public class RecyclerCompleteSearchAdapter extends RecyclerView.Adapter<RecyclerCompleteSearchAdapter.MyViewHolder> {

    private ArrayList<String> mTagList;
    private onItemClickListener mItemClickListener;

    public RecyclerCompleteSearchAdapter(onItemClickListener listener) {
        this(new ArrayList<String>(), listener);
    }

    public RecyclerCompleteSearchAdapter(@NonNull ArrayList<String> tagList, onItemClickListener listener) {
        mTagList = tagList;
        mItemClickListener = listener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.recyclerview_item_auto_complete_search, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final String tag = mTagList.get(position);

        // 搜索提示
        holder.tvAutoComplete.setText(tag);

        // 点击搜索
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mItemClickListener.onItemClick(tag);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mTagList.size();
    }

    public void addDatas(Set<String> tagSet) {
        int position = mTagList.size();
        mTagList.addAll(tagSet);
        notifyItemRangeInserted(position, tagSet.size());
        notifyItemRangeChanged(position, mTagList.size() - position);
    }

    public void clear() {
        mTagList.clear();
        notifyDataSetChanged();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView tvAutoComplete;

        public MyViewHolder(View itemView) {
            super(itemView);
            tvAutoComplete = (TextView) itemView.findViewById(R.id.tv_auto_complete);
        }
    }

    public interface onItemClickListener {
        void onItemClick(String tag);
    }
}
