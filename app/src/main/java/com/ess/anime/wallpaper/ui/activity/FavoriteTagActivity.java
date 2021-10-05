package com.ess.anime.wallpaper.ui.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.adapter.RecyclerFavoriteTagAdapter;
import com.ess.anime.wallpaper.database.FavoriteTagBean;
import com.ess.anime.wallpaper.database.GreenDaoUtils;
import com.ess.anime.wallpaper.listener.DoubleTapEffector;
import com.ess.anime.wallpaper.model.helper.TagOperationHelper;
import com.ess.anime.wallpaper.ui.view.CustomDialog;
import com.ess.anime.wallpaper.ui.view.GridDividerItemDecoration;
import com.ess.anime.wallpaper.utils.UIUtils;
import com.ess.anime.wallpaper.utils.VibratorUtils;
import com.mixiaoxiao.smoothcompoundbutton.SmoothCheckBox;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.OnClick;

public class FavoriteTagActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.tool_bar)
    Toolbar mToolbar;
    @BindView(R.id.layout_normal)
    ViewGroup mLayoutNormal;
    @BindView(R.id.layout_editing)
    ViewGroup mLayoutEditing;
    @BindView(R.id.tv_choose_count)
    TextView mTvChooseCount;
    @BindView(R.id.layout_choose_all)
    ViewGroup mLayoutChooseAll;
    @BindView(R.id.cb_choose_all)
    SmoothCheckBox mCbChooseAll;
    @BindView(R.id.rv_tag)
    RecyclerView mRvTag;

    private LinearLayoutManager mLayoutManager;
    private RecyclerFavoriteTagAdapter mTagAdapter;

    @Override
    protected int layoutRes() {
        return R.layout.activity_favorite_tag;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        initToolBarLayout();
        initRecyclerWebsite();
        resetTagData();
    }

    private void initToolBarLayout() {
        mToolbar.setTitle(R.string.nav_favorite_tag);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationIcon(R.drawable.ic_back);
        mToolbar.setNavigationOnClickListener(v -> finish());
        DoubleTapEffector.addDoubleTapEffect(mToolbar, () -> scrollToTop(true));
    }

    @OnClick({R.id.layout_choose_all, R.id.cb_choose_all, R.id.tv_sort, R.id.tv_edit, R.id.tv_delete})
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_choose_all:
                mCbChooseAll.toggle();
            case R.id.cb_choose_all:
                if (mCbChooseAll.isChecked()) {
                    mTagAdapter.selectAll();
                } else {
                    mTagAdapter.deselectAll();
                }
                break;

            case R.id.tv_sort:
                CustomDialog.showSortFavoriteTagsDialog(this, new CustomDialog.SimpleDialogActionListener() {
                    @Override
                    public void onPositive() {
                        resetTagData();
                    }
                });
                break;

            case R.id.tv_edit:
                enterEditMode();
                break;

            case R.id.tv_delete:
                List<FavoriteTagBean> deleteList = new ArrayList<>(mTagAdapter.getSelectList());
                CustomDialog.showDeleteFavoriteTagsDialog(this, new CustomDialog.SimpleDialogActionListener() {
                    @Override
                    public void onPositive() {
                        exitEditMode(false);
                        mTagAdapter.removeDatas(deleteList);
                        List<FavoriteTagBean> hasAnnotationTags = new ArrayList<>();
                        List<FavoriteTagBean> emptyAnnotationTags = new ArrayList<>();
                        for (FavoriteTagBean tagBean : deleteList) {
                            tagBean.setIsFavorite(false);
                            if (TextUtils.isEmpty(tagBean.getAnnotation())) {
                                emptyAnnotationTags.add(tagBean);
                            } else {
                                hasAnnotationTags.add(tagBean);
                            }
                        }
                        GreenDaoUtils.updateFavoriteTags(hasAnnotationTags);
                        GreenDaoUtils.deleteFavoriteTags(emptyAnnotationTags);
                    }
                });
                break;
        }
    }

    private void initRecyclerWebsite() {
        mLayoutManager = new LinearLayoutManager(this);
        mRvTag.setLayoutManager(mLayoutManager);

        mTagAdapter = new RecyclerFavoriteTagAdapter();
        mTagAdapter.setEmptyView(R.layout.layout_empty_favorite_tag, mRvTag);
        mTagAdapter.bindToRecyclerView(mRvTag);
        int spaceHor = UIUtils.dp2px(this, 6);
        int spaceVer = UIUtils.dp2px(this, 12);
        mRvTag.addItemDecoration(new GridDividerItemDecoration(
                1, GridDividerItemDecoration.VERTICAL, spaceHor, spaceVer, true));

        // 长按进入编辑模式
        mTagAdapter.setOnItemLongClickListener((baseQuickAdapter, view, i) -> {
            if (!mTagAdapter.isEditMode()) {
                FavoriteTagBean tagBean = mTagAdapter.getItem(i);
                mTagAdapter.select(tagBean);
                mTagAdapter.enterEditMode();
                VibratorUtils.Vibrate(view.getContext(), 12);
                toggleEditView(true);
                return true;
            }
            return false;
        });

        // 切换选中/非选中监听器
        mTagAdapter.setOnSelectChangedListener((selectCount, allSelected) -> {
            mTvChooseCount.setText(String.valueOf(selectCount));
            mCbChooseAll.setChecked(allSelected);
        });
    }

    private void resetTagData() {
        mTagAdapter.setNewData(TagOperationHelper.queryAllFavoriteTags());
        scrollToTop(false);
    }

    private void scrollToTop(boolean smooth) {
        if (smooth) {
            int smoothPos = 10;
            if (mLayoutManager.findLastVisibleItemPosition() > smoothPos) {
                mRvTag.scrollToPosition(smoothPos);
            }
            mRvTag.smoothScrollToPosition(0);
        } else {
            mRvTag.scrollToPosition(0);
        }
    }

    private void toggleEditView(boolean editing) {
        mCbChooseAll.setChecked(false, false, false);
        if (editing) {
            mLayoutChooseAll.animate().cancel();
            mLayoutChooseAll.setTranslationX(-UIUtils.dp2px(this, 30));
            mLayoutChooseAll.animate()
                    .translationX(0)
                    .setDuration(mRvTag.getItemAnimator().getChangeDuration())
                    .start();
            mLayoutNormal.setVisibility(View.GONE);
            mLayoutEditing.setVisibility(View.VISIBLE);
            mToolbar.setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        } else {
            mLayoutChooseAll.animate().cancel();
            mLayoutNormal.setVisibility(View.VISIBLE);
            mLayoutEditing.setVisibility(View.GONE);
            initToolBarLayout();
        }
    }

    private void enterEditMode() {
        toggleEditView(true);
        mTagAdapter.enterEditMode();
    }

    private void exitEditMode(boolean notify) {
        toggleEditView(false);
        mTagAdapter.exitEditMode(notify);
    }

    @Override
    public void onBackPressed() {
        if (mTagAdapter.isEditMode()) {
            exitEditMode(true);
        } else {
            finish();
        }
    }

}
