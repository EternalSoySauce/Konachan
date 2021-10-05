package com.ess.anime.wallpaper.ui.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.database.FavoriteTagBean;
import com.ess.anime.wallpaper.model.helper.TagOperationHelper;
import com.ess.anime.wallpaper.utils.UIUtils;
import com.zhengsr.tablib.view.adapter.LabelFlowAdapter;
import com.zhengsr.tablib.view.flow.LabelFlowLayout;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchFavoriteTagLayout extends FrameLayout {

    @BindView(R.id.layout_empty)
    ViewGroup mLayoutEmpty;
    @BindView(R.id.label_flow)
    LabelFlowLayout mLabelFlow;

    private boolean mHasInit;
    private LabelFlowAdapter<FavoriteTagBean> mLabelFlowAdapter;

    public SearchFavoriteTagLayout(@NonNull Context context) {
        super(context);
    }

    public SearchFavoriteTagLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SearchFavoriteTagLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        initLabelFlowLayout();
        checkToShowEmptyView();
    }

    private List<FavoriteTagBean> getData() {
        return TagOperationHelper.queryAllFavoriteTags();
    }

    private void initLabelFlowLayout() {
        mLabelFlowAdapter = new LabelFlowAdapter<FavoriteTagBean>(
                R.layout.recycler_item_search_fav_tag_label_flow, new ArrayList<>()) {
            @Override
            public void bindView(View view, FavoriteTagBean tagBean, int position) {
                // 显示标签
                TextView tvTag = view.findViewById(R.id.tv_tag);
                tvTag.setText(tagBean.getTag());
                tvTag.setOnClickListener(v -> {
                    search(tagBean);
                });

                // 备注按钮
                ImageView ivAnnotation = view.findViewById(R.id.iv_annotation);
                ivAnnotation.setOnClickListener(v -> {
                    CustomDialog.showEditTagAnnotationDialog(getContext(), tagBean.getTag(), false, null);
                });
            }
        };
        mLabelFlow.setAdapter(mLabelFlowAdapter);
    }

    private void search(FavoriteTagBean tagBean) {
        if (getContext() instanceof Activity) {
            Activity activity = (Activity) getContext();
            UIUtils.closeSoftInput(activity);
            TagOperationHelper.searchTag(activity, tagBean.getTag());
        }
    }

    private void checkToShowEmptyView() {
        mLayoutEmpty.setVisibility(mLabelFlowAdapter.getDatas().isEmpty() ? VISIBLE : GONE);
    }

    public void initData() {
        if (!mHasInit) {
            mHasInit = true;
            resetData();
        }
    }

    public void resetData() {
        mLabelFlowAdapter.getDatas().clear();
        mLabelFlowAdapter.getDatas().addAll(getData());
        mLabelFlowAdapter.notifyDataChanged();
        checkToShowEmptyView();
    }

}
