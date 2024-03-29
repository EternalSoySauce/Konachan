package com.ess.anime.wallpaper.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.adapter.RecyclerDownloadImageAdapter;
import com.ess.anime.wallpaper.download.image.DownloadBean;
import com.ess.anime.wallpaper.download.image.DownloadImageManager;
import com.ess.anime.wallpaper.ui.view.CustomDialog;
import com.ess.anime.wallpaper.ui.view.GeneralRecyclerView;
import com.ess.anime.wallpaper.ui.view.GridDividerItemDecoration;
import com.ess.anime.wallpaper.utils.UIUtils;

import java.util.Collections;
import java.util.List;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import butterknife.BindView;
import butterknife.OnClick;

public class DownloadImageManagerActivity extends BaseActivity {

    @BindView(R.id.tool_bar)
    Toolbar mToolbar;
    @BindView(R.id.rv_download)
    GeneralRecyclerView mRvDownload;

    private GridLayoutManager mLayoutManager;

    @Override
    protected int layoutRes() {
        return R.layout.activity_download_image_manager;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        initToolBarLayout();
        initRecyclerDownload();
    }

    @Override
    protected void updateUI() {
        super.updateUI();
        updateRecyclerViewSpanCount();
    }

    private void initToolBarLayout() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(v -> finish());
    }

    @OnClick(R.id.iv_clear_all)
    void clearAllFinished() {
        CustomDialog.showClearAllDownloadFinishedDialog(this, new CustomDialog.SimpleDialogActionListener() {
            @Override
            public void onPositive() {
                super.onPositive();
                DownloadImageManager.getInstance().clearAllFinished();
            }
        });
    }

    @OnClick(R.id.iv_goto_collection)
    void gotoCollection() {
        startActivity(new Intent(this, CollectionActivity.class));
    }

    private void initRecyclerDownload() {
        List<DownloadBean> downloadList = DownloadImageManager.getInstance().getDownloadList();
        Collections.reverse(downloadList);

        mLayoutManager = new GridLayoutManager(this, 1);
        mRvDownload.setLayoutManager(mLayoutManager);
        RecyclerDownloadImageAdapter adapter = new RecyclerDownloadImageAdapter(downloadList);
        adapter.bindToRecyclerView(mRvDownload);
        adapter.setEmptyView(R.layout.layout_empty_download, mRvDownload);
    }

    private void updateRecyclerViewSpanCount() {
        if (mLayoutManager != null && mRvDownload != null) {
            int span = UIUtils.isLandscape(this) ? 2 : 1;
            mLayoutManager.setSpanCount(span);

            int spaceHor = UIUtils.dp2px(this, 5);
            int spaceVer = UIUtils.dp2px(this, 10);
            mRvDownload.clearItemDecorations();
            mRvDownload.addItemDecoration(new GridDividerItemDecoration(
                    span, GridDividerItemDecoration.VERTICAL, spaceHor, spaceVer, true));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isFinishing()) {
            mRvDownload.setAdapter(null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRvDownload.setAdapter(null);
    }

}
