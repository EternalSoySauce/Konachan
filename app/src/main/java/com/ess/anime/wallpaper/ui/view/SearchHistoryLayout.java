package com.ess.anime.wallpaper.ui.view;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.database.GreenDaoUtils;
import com.ess.anime.wallpaper.database.SearchTagBean;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.ui.activity.MainActivity;
import com.ess.anime.wallpaper.ui.activity.SearchActivity;
import com.ess.anime.wallpaper.utils.UIUtils;
import com.zhengsr.tablib.view.adapter.LabelFlowAdapter;
import com.zhengsr.tablib.view.flow.LabelFlowLayout;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchHistoryLayout extends FrameLayout {

    @BindView(R.id.layout_empty)
    ViewGroup mLayoutEmpty;
    @BindView(R.id.label_flow)
    LabelFlowLayout mLabelFlow;

    private LabelFlowAdapter<SearchTagBean> mLabelFlowAdapter;
    private boolean mIsEditing;

    public SearchHistoryLayout(@NonNull Context context) {
        super(context);
    }

    public SearchHistoryLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SearchHistoryLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        initLabelFlowLayout();
        checkToShowEmptyView();
    }

    private List<SearchTagBean> getHistoryData() {
        return GreenDaoUtils.queryAllSearchTags();
    }

    private void initLabelFlowLayout() {
        mLabelFlowAdapter = new LabelFlowAdapter<SearchTagBean>(
                R.layout.recycler_item_search_history_label_flow, getHistoryData()) {
            @Override
            public void bindView(View view, SearchTagBean searchTagBean, int position) {
                // 显示标签
                TextView tvTag = view.findViewById(R.id.tv_tag);
                tvTag.setText(searchTagBean.getTag());
                tvTag.setOnClickListener(v -> {
                    search(searchTagBean);
                });
                tvTag.setOnLongClickListener(v -> {
                    v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    if (isEditing()) {
                        cancelEdit();
                    } else {
                        startEdit();
                    }
                    return true;
                });

                // 删除按钮
                ImageView ivDelete = view.findViewById(R.id.iv_delete);
                ivDelete.setVisibility(isEditing() ? VISIBLE : GONE);
                ivDelete.setOnClickListener(v -> {
                    GreenDaoUtils.deleteSearchTag(searchTagBean);
                    getDatas().remove(searchTagBean);
                    notifyDataChanged();
                    checkToShowEmptyView();
                });
            }
        };
        mLabelFlow.setAdapter(mLabelFlowAdapter);
    }

    private void search(SearchTagBean searchTagBean) {
        searchTagBean.setUpdateTime(System.currentTimeMillis());
        GreenDaoUtils.updateSearchTag(searchTagBean);
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.putExtra(Constants.SEARCH_TAG, searchTagBean.getTag());
        intent.putExtra(Constants.SEARCH_MODE, searchTagBean.getMode());
        getContext().startActivity(intent);
        if (getContext() instanceof SearchActivity) {
            SearchActivity searchActivity = (SearchActivity) getContext();
            UIUtils.closeSoftInput(searchActivity);
            searchActivity.finish();
        }
    }

    private void checkToShowEmptyView() {
        mLayoutEmpty.setVisibility(mLabelFlowAdapter.getDatas().isEmpty() ? VISIBLE : GONE);
    }

    public void resetData() {
        mLabelFlowAdapter.getDatas().clear();
        mLabelFlowAdapter.getDatas().addAll(getHistoryData());
        mLabelFlowAdapter.notifyDataChanged();
        checkToShowEmptyView();
    }

    public synchronized void startEdit() {
        if (!isEditing()) {
            mIsEditing = true;
            mLabelFlowAdapter.notifyDataChanged();
        }
    }

    public synchronized void cancelEdit() {
        if (isEditing()) {
            mIsEditing = false;
            mLabelFlowAdapter.notifyDataChanged();
        }
    }

    public synchronized boolean isEditing() {
        if (mLabelFlowAdapter.getDatas().isEmpty()) {
            mIsEditing = false;
        }
        return mIsEditing;
    }

}
