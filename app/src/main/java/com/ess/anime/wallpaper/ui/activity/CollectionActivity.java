package com.ess.anime.wallpaper.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.adapter.RecyclerCollectionAdapter;
import com.ess.anime.wallpaper.bean.CollectionBean;
import com.ess.anime.wallpaper.bean.MsgBean;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.listener.LocalCollectionsListener;
import com.ess.anime.wallpaper.model.helper.PermissionHelper;
import com.ess.anime.wallpaper.model.holder.ImageDataHolder;
import com.ess.anime.wallpaper.ui.view.CustomDialog;
import com.ess.anime.wallpaper.ui.view.GridDividerItemDecoration;
import com.ess.anime.wallpaper.utils.BitmapUtils;
import com.ess.anime.wallpaper.utils.FileUtils;
import com.ess.anime.wallpaper.utils.UIUtils;
import com.ess.anime.wallpaper.utils.VibratorUtils;
import com.mixiaoxiao.smoothcompoundbutton.SmoothCheckBox;
import com.yanzhenjie.permission.runtime.Permission;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.OnClick;

public class CollectionActivity extends BaseActivity implements View.OnClickListener, LocalCollectionsListener.OnFilesChangedListener {

    @BindView(R.id.tool_bar)
    Toolbar mToolbar;
    @BindView(R.id.layout_editing)
    RelativeLayout mLayoutEditing;
    @BindView(R.id.tv_edit)
    TextView mTvEdit;
    @BindView(R.id.tv_choose_count)
    TextView mTvChooseCount;
    @BindView(R.id.cb_choose_all)
    SmoothCheckBox mCbChooseAll;
    @BindView(R.id.rv_collection)
    RecyclerView mRvCollection;

    private RecyclerCollectionAdapter mCollectionAdapter;

    private LocalCollectionsListener mFilesListener;

    @Override
    int layoutRes() {
        return R.layout.activity_collection;
    }

    @Override
    void init(Bundle savedInstanceState) {
        initToolBarLayout();
        PermissionHelper.checkStoragePermissions(this, new PermissionHelper.RequestListener() {
            @Override
            public void onGranted() {
                initWhenPermissionGranted();
            }

            @Override
            public void onDenied() {
                finish();
            }
        });
    }

    private void initWhenPermissionGranted() {
        initRecyclerView();
        mFilesListener = new LocalCollectionsListener(CollectionActivity.this);
        mFilesListener.startWatching();
    }

    private void initToolBarLayout() {
        mToolbar.setTitle(R.string.nav_collection);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationIcon(R.drawable.ic_back);
        mToolbar.setNavigationOnClickListener(v -> finish());
    }


    @OnClick({R.id.layout_choose_all, R.id.cb_choose_all, R.id.tv_edit, R.id.tv_share, R.id.tv_delete})
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_choose_all:
                mCbChooseAll.toggle();
            case R.id.cb_choose_all:
                if (mCbChooseAll.isChecked()) {
                    mCollectionAdapter.selectAll();
                } else {
                    mCollectionAdapter.deselectAll();
                }
                break;

            case R.id.tv_edit:
                enterEditMode();
                break;

            case R.id.tv_share:
                shareImages();
                exitEditMode(true);
                break;

            case R.id.tv_delete:
                ArrayList<CollectionBean> deleteList = new ArrayList<>(mCollectionAdapter.getSelectList());
                String msg = getString(R.string.dialog_delete_msg, deleteList.size());
                showDeleteCollectionDialog(msg, deleteList);
                break;
        }
    }

    private void initRecyclerView() {
        mRvCollection.setLayoutManager(new GridLayoutManager(this, 3));
        mCollectionAdapter = new RecyclerCollectionAdapter(CollectionBean.getCollectionImages());
        mRvCollection.setAdapter(mCollectionAdapter);
        int spanHor = UIUtils.dp2px(this, 0.75f);
        int spanVer = UIUtils.dp2px(this, 1.5f);
        GridDividerItemDecoration itemDecoration = new GridDividerItemDecoration(
                3, GridDividerItemDecoration.VERTICAL, spanHor, spanVer, true);
        mRvCollection.addItemDecoration(itemDecoration);

        // 长按进入编辑模式监听器
        mCollectionAdapter.setOnItemChildLongClickListener((adapter, view, position) -> {
            if (view.getId() == R.id.iv_collection && !mCollectionAdapter.isEditMode()) {
                CollectionBean collectionBean = mCollectionAdapter.getItem(position);
                mCollectionAdapter.select(collectionBean);
                mCollectionAdapter.enterEditMode();
                VibratorUtils.Vibrate(view.getContext(), 12);
                toggleEditView(true);
            }
            return false;
        });

        // 切换选中/非选中监听器
        mCollectionAdapter.setOnSelectChangedListener((selectCount, allSelected) -> {
            mTvChooseCount.setText(String.valueOf(selectCount));
            mCbChooseAll.setChecked(allSelected);
        });
    }

    private void showDeleteCollectionDialog(String msg, final ArrayList<CollectionBean> deleteList) {
        CustomDialog.showDeleteCollectionDialog(this, msg, new CustomDialog.SimpleDialogActionListener() {
            @Override
            public void onPositive() {
                exitEditMode(false);
                mCollectionAdapter.removeDatas(deleteList);
                for (CollectionBean collectionBean : deleteList) {
                    String path = collectionBean.url.replace("file://", "");
                    FileUtils.deleteFile(path);
                    // 从媒体库删除图片（刷新相册）
                    BitmapUtils.deleteFromMediaStore(CollectionActivity.this, path);
                }
            }
        });
    }

    private void toggleEditView(boolean editing) {
        mCbChooseAll.setChecked(false, false, false);
        if (editing) {
            mTvEdit.setVisibility(View.GONE);
            mLayoutEditing.setVisibility(View.VISIBLE);
            mToolbar.setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        } else {
            mTvEdit.setVisibility(View.VISIBLE);
            mLayoutEditing.setVisibility(View.GONE);
            initToolBarLayout();
        }
    }

    private void enterEditMode() {
        toggleEditView(true);
        mCollectionAdapter.enterEditMode();
    }

    private void exitEditMode(boolean notify) {
        toggleEditView(false);
        mCollectionAdapter.exitEditMode(notify);
    }

    private void shareImages() {
        ArrayList<Uri> uriList = new ArrayList<>();
        for (CollectionBean collectionBean : mCollectionAdapter.getSelectList()) {
            Uri uri = Uri.parse(collectionBean.url);
            uriList.add(uri);
        }
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("*/*");
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList);
        startActivity(Intent.createChooser(intent, getString(R.string.share_title)));
    }

    @Override
    public void onFileAdded(final File file) {
        runOnUiThread(() -> {
            if (mCollectionAdapter.isEditMode()) {
                exitEditMode(false);
            }
            CollectionBean collectionBean = CollectionBean.createCollectionFromFile(file);
            if (!mCollectionAdapter.getData().contains(collectionBean)) {
                mCollectionAdapter.addData(0, collectionBean);
            }
            mRvCollection.scrollToPosition(0);
            // 发送通知到FullscreenActivity
            EventBus.getDefault().post(new MsgBean(Constants.LOCAL_FILES_CHANGED, null));
        });
    }

    @Override
    public void onFileRemoved(final File file) {
        runOnUiThread(() -> {
            if (mCollectionAdapter.isEditMode()) {
                exitEditMode(false);
            }
            CollectionBean collectionBean = CollectionBean.createCollectionFromFile(file);
            mCollectionAdapter.removeData(collectionBean);
            // 发送通知到FullscreenActivity
            EventBus.getDefault().post(new MsgBean(Constants.LOCAL_FILES_CHANGED, null));
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PermissionHelper.REQ_CODE_PERMISSION) {
            // 进入系统设置界面请求权限后的回调
            if (PermissionHelper.hasPermissions(this, Permission.Group.STORAGE)) {
                initWhenPermissionGranted();
            } else {
                finish();
            }
        } else if (resultCode == Constants.FULLSCREEN_CODE && data != null) {
            // 退出全屏回调
            int position = ImageDataHolder.getCollectionCurrentItem();
            mRvCollection.scrollToPosition(position);
        }
    }

    @Override
    public void onBackPressed() {
        if (mCollectionAdapter.isEditMode()) {
            exitEditMode(true);
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mFilesListener != null) {
            mFilesListener.stopWatching();
        }
    }

}
