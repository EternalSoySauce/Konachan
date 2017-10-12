package com.ess.konachan.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ess.konachan.R;

public class ListSearchModePopupAdapter extends BaseAdapter {

    private Context mContext;
    private String[] mSearchModeArray;
    private int mSelectedPos;

    public ListSearchModePopupAdapter(Context context, @NonNull String[] searchModeArray) {
        mContext = context;
        mSearchModeArray = searchModeArray;
    }

    @Override
    public int getCount() {
        return mSearchModeArray.length;
    }

    @Override
    public String getItem(int position) {
        return mSearchModeArray[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_popup_search_mode, parent, false);
        }
        int colorRes = position == mSelectedPos ? R.color.color_text_selected : R.color.color_text_unselected;
        TextView tv = (TextView) convertView.findViewById(R.id.tv_search_mode);
        tv.setText(mSearchModeArray[position]);
        tv.setTextColor(mContext.getResources().getColor(colorRes));
        return convertView;
    }

    public void setSelection(int position) {
        mSelectedPos = position;
        notifyDataSetChanged();
    }
}
