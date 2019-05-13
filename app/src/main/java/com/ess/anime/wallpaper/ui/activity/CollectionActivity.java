package com.ess.anime.wallpaper.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
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
import java.util.List;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CollectionActivity extends BaseActivity implements View.OnClickListener, LocalCollectionsListener.OnFilesChangedListener {

    private Toolbar mToolbar;
    private TextView mTvEdit;
    private RelativeLayout mLayoutEditing;
    private SmoothCheckBox mCbChooseAll;
    private TextView mTvChooseCount;

    private RecyclerView mRvCollection;
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
        initEditView();
        initRecyclerView();
        mFilesListener = new LocalCollectionsListener(CollectionActivity.this);
        mFilesListener.startWatching();
    }

    private void initToolBarLayout() {
        mToolbar = findViewById(R.id.tool_bar);
        mToolbar.setTitle(R.string.nav_collection);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initEditView() {
        mLayoutEditing = findViewById(R.id.layout_editing);
        findViewById(R.id.layout_choose_all).setOnClickListener(this);
        mCbChooseAll = findViewById(R.id.cb_choose_all);
        mCbChooseAll.setOnClickListener(this);
        mTvChooseCount = findViewById(R.id.tv_choose_count);
        mTvEdit = findViewById(R.id.tv_edit);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_choose_all:
                mCbChooseAll.performClick();
                break;

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
        mRvCollection = findViewById(R.id.rv_collection);
        mRvCollection.setLayoutManager(new GridLayoutManager(this, 3));
        mCollectionAdapter = new RecyclerCollectionAdapter(CollectionBean.getCollectionImages());
        mRvCollection.setAdapter(mCollectionAdapter);
        int spanHor = UIUtils.dp2px(this, 0.75f);
        int spanVer = UIUtils.dp2px(this, 1.5f);
        GridDividerItemDecoration itemDecoration = new GridDividerItemDecoration(
                3, GridDividerItemDecoration.VERTICAL, spanHor, spanVer, true);
        mRvCollection.addItemDecoration(itemDecoration);

        // 点击、全屏查看监听器
        mCollectionAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                CollectionBean collectionBean = mCollectionAdapter.getItem(position);
                switch (view.getId()) {
                    case R.id.iv_collection:
                        if (mCollectionAdapter.isEditMode()) {
                            // 编辑模式下切换选中/非选中
                            boolean newChecked = !mCollectionAdapter.isSelected(collectionBean);
                            SmoothCheckBox cbChoose = (SmoothCheckBox) adapter.getViewByPosition(mRvCollection, position, R.id.cb_choose);
                            cbChoose.setChecked(newChecked);
                            if (newChecked) {
                                mCollectionAdapter.select(collectionBean);
                            } else {
                                mCollectionAdapter.deselect(collectionBean);
                            }
                        } else {
                            // 非编辑模式下全屏查看
                            ImageDataHolder.setCollectionList(mCollectionAdapter.getData());
                            ImageDataHolder.setCollectionCurrentItem(position);

                            // TODO 点击全屏查看图片缩放动画
                            ActivityOptionsCompat compat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                                    CollectionActivity.this, new Pair<>(view, "s"));
                            Intent intent = new Intent(CollectionActivity.this, FullscreenActivity.class);
                            startActivityForResult(intent, Constants.FULLSCREEN_CODE);
//                          ActivityCompat.startActivityForResult(CollectionActivity.this, intent,
//                          Constants.FULLSCREEN_CODE, compat.toBundle());
                        }
                        break;

                    case R.id.iv_enlarge:
                        // 编辑模式下全屏查看
                        List<CollectionBean> enlargeList = new ArrayList<>();
                        enlargeList.add(collectionBean);
                        ImageDataHolder.setCollectionList(enlargeList);
                        ImageDataHolder.setCollectionCurrentItem(0);

                        Intent intent = new Intent(CollectionActivity.this, FullscreenActivity.class);
                        intent.putExtra(Constants.ENLARGE, true);
                        startActivity(intent);
                        break;
                }
            }
        });

        // 长按进入编辑模式监听器
        mCollectionAdapter.setOnItemChildLongClickListener(new BaseQuickAdapter.OnItemChildLongClickListener() {
            @Override
            public boolean onItemChildLongClick(BaseQuickAdapter adapter, View view, int position) {
                if (view.getId() == R.id.iv_collection && !mCollectionAdapter.isEditMode()) {
                    CollectionBean collectionBean = mCollectionAdapter.getItem(position);
                    mCollectionAdapter.select(collectionBean);
                    mCollectionAdapter.enterEditMode();
                    VibratorUtils.Vibrate(view.getContext(), 12);
                    toggleEditView(true);
                }
                return false;
            }
        });

        // 切换选中/非选中监听器
        mCollectionAdapter.setOnSelectChangedListener(new RecyclerCollectionAdapter.OnSelectChangedListener() {
            @Override
            public void onSelectChanged(int selectCount, boolean allSelected) {
                mTvChooseCount.setText(String.valueOf(selectCount));
                mCbChooseAll.setChecked(allSelected);
            }
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
            }
        });
    }

    @Override
    public void onFileRemoved(final File file) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mCollectionAdapter.isEditMode()) {
                    exitEditMode(false);
                }
                CollectionBean collectionBean = CollectionBean.createCollectionFromFile(file);
                mCollectionAdapter.removeData(collectionBean);
                // 发送通知到FullscreenActivity
                EventBus.getDefault().post(new MsgBean(Constants.LOCAL_FILES_CHANGED, null));
            }
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
