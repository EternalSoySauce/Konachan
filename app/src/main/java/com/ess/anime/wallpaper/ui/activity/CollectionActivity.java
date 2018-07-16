package com.ess.anime.wallpaper.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.adapter.RecyclerCollectionAdapter;
import com.ess.anime.wallpaper.bean.CollectionBean;
import com.ess.anime.wallpaper.bean.MsgBean;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.global.ImageDataHolder;
import com.ess.anime.wallpaper.listener.LocalCollectionsListener;
import com.ess.anime.wallpaper.utils.BitmapUtils;
import com.ess.anime.wallpaper.utils.FileUtils;
import com.ess.anime.wallpaper.helper.PermissionHelper;
import com.ess.anime.wallpaper.utils.UIUtils;
import com.ess.anime.wallpaper.view.CustomDialog;
import com.ess.anime.wallpaper.view.GridDividerItemDecoration;
import com.mixiaoxiao.smoothcompoundbutton.SmoothCheckBox;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CollectionActivity extends AppCompatActivity implements View.OnClickListener, LocalCollectionsListener.OnFilesChangedListener {

    private Toolbar mToolbar;
    private TextView mTvEdit;
    private RelativeLayout mLayoutEditing;
    private SmoothCheckBox mCbChooseAll;
    private TextView mTvChooseCount;

    private RecyclerView mRvCollection;
    private RecyclerCollectionAdapter mCollectionAdapter;

    private PermissionHelper mPermissionUtil;
    private LocalCollectionsListener mFilesListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);

        checkStoragePermission();
    }

    private void checkStoragePermission() {
        if (mPermissionUtil == null) {
            mPermissionUtil = new PermissionHelper(this, new PermissionHelper.OnPermissionListener() {
                @Override
                public void onGranted() {
                    initToolBarLayout();
                    initEditView();
                    initRecyclerView();
                    mFilesListener = new LocalCollectionsListener(CollectionActivity.this);
                    mFilesListener.startWatching();
                }

                @Override
                public void onDenied() {
                    finish();
                }
            });
        }
        mPermissionUtil.checkStoragePermission();
    }

    private void initToolBarLayout() {
        mToolbar = (Toolbar) findViewById(R.id.tool_bar);
        mToolbar.setTitle(R.string.nav_collection);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationIcon(R.drawable.ic_back);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initEditView() {
        mLayoutEditing = (RelativeLayout) findViewById(R.id.layout_editing);
        findViewById(R.id.layout_choose_all).setOnClickListener(this);
        mCbChooseAll = (SmoothCheckBox) findViewById(R.id.cb_choose_all);
        mCbChooseAll.setOnClickListener(this);
        mTvChooseCount = (TextView) findViewById(R.id.tv_choose_count);
        mTvEdit = (TextView) findViewById(R.id.tv_edit);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_choose_all:
                mCbChooseAll.performClick();
                break;
            case R.id.cb_choose_all:
                if (mCbChooseAll.isChecked()) {
                    mCollectionAdapter.chooseAll();
                } else {
                    mCollectionAdapter.cancelChooseAll();
                }
                mTvChooseCount.setText(String.valueOf(mCollectionAdapter.getChooseCount()));
                break;
            case R.id.tv_edit:
                beginEdit();
                break;
            case R.id.tv_share:
                shareImages();
                cancelEdit(true);
                break;
            case R.id.tv_delete:
                ArrayList<CollectionBean> deleteList = new ArrayList<>(mCollectionAdapter.getChooseList());
                String msg = getString(R.string.dialog_delete_msg, deleteList.size());
                showDeleteCollectionDialog(msg, deleteList);
                break;
        }
    }

    private void initRecyclerView() {
        mRvCollection = (RecyclerView) findViewById(R.id.rv_collection);
        mRvCollection.setLayoutManager(new GridLayoutManager(this, 3));
        mCollectionAdapter = new RecyclerCollectionAdapter(this, CollectionBean.getCollectionImages());
        mRvCollection.setAdapter(mCollectionAdapter);
        int spanHor = UIUtils.dp2px(this, 0.75f);
        int spanVer = UIUtils.dp2px(this, 1.5f);
        GridDividerItemDecoration itemDecoration = new GridDividerItemDecoration(
                3, GridDividerItemDecoration.VERTICAL, spanHor, spanVer, true);
        mRvCollection.addItemDecoration(itemDecoration);

        // adapter设置 点击全屏查看 和 长按进入编辑模式 监听器
        mCollectionAdapter.setOnActionListener(new RecyclerCollectionAdapter.OnActionListener() {
            @Override
            public void onFullScreen(ImageView imageView, int position) {
                ImageDataHolder.setCollectionList(mCollectionAdapter.getCollectionList());
                ImageDataHolder.setCollectionCurrentItem(position);

                // TODO 点击全屏查看图片缩放动画
                ActivityOptionsCompat compat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        CollectionActivity.this, new Pair<View, String>(imageView, "s"));
                Intent intent = new Intent(CollectionActivity.this, FullscreenActivity.class);
                startActivityForResult(intent, Constants.FULLSCREEN_CODE);
//                ActivityCompat.startActivityForResult(CollectionActivity.this, intent,
//                        Constants.FULLSCREEN_CODE, compat.toBundle());
            }

            @Override
            public void onEdit() {
                toggleEditView(true);
            }

            @Override
            public void onEnlarge(int position) {
                List<CollectionBean> enlargeList = new ArrayList<>();
                enlargeList.add(mCollectionAdapter.getCollectionList().get(position));
                ImageDataHolder.setCollectionList(enlargeList);
                ImageDataHolder.setCollectionCurrentItem(0);

                Intent intent = new Intent(CollectionActivity.this, FullscreenActivity.class);
                intent.putExtra(Constants.ENLARGE, true);
                startActivity(intent);
            }

            @Override
            public void onItemClick() {
                int chooseCount = mCollectionAdapter.getChooseCount();
                mTvChooseCount.setText(String.valueOf(chooseCount));
                mCbChooseAll.setChecked(chooseCount == mCollectionAdapter.getItemCount(), true, false);
            }
        });
    }

    private void showDeleteCollectionDialog(String msg, final ArrayList<CollectionBean> deleteList) {
        CustomDialog.showDeleteCollectionDialog(this, msg, new CustomDialog.SimpleDialogActionListener() {
            @Override
            public void onPositive() {
                cancelEdit(false);
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
        if (editing) {
            mTvEdit.setVisibility(View.GONE);
            mLayoutEditing.setVisibility(View.VISIBLE);
            mCbChooseAll.setChecked(false, false, false);
            mTvChooseCount.setText(String.valueOf(mCollectionAdapter.getChooseCount()));
            mToolbar.setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        } else {
            mTvEdit.setVisibility(View.VISIBLE);
            mLayoutEditing.setVisibility(View.GONE);
            initToolBarLayout();
        }
    }

    private void beginEdit() {
        toggleEditView(true);
        mCollectionAdapter.beginEdit();
    }

    private void cancelEdit(boolean notify) {
        toggleEditView(false);
        mCollectionAdapter.cancelEdit(notify);
    }

    private void shareImages() {
        ArrayList<Uri> uriList = new ArrayList<>();
        for (CollectionBean collectionBean : mCollectionAdapter.getChooseList()) {
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
                if (mCollectionAdapter.isEditing()) {
                    cancelEdit(false);
                }
                CollectionBean collectionBean = CollectionBean.createCollectionFromFile(file);
                if (!mCollectionAdapter.getCollectionList().contains(collectionBean)) {
                    mCollectionAdapter.addData(collectionBean);
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
                if (mCollectionAdapter.isEditing()) {
                    cancelEdit(false);
                }
                CollectionBean collectionBean = CollectionBean.createCollectionFromFile(file);
                if (mCollectionAdapter.getCollectionList().contains(collectionBean)) {
                    mCollectionAdapter.removeData(collectionBean);
                }
                // 发送通知到FullscreenActivity
                EventBus.getDefault().post(new MsgBean(Constants.LOCAL_FILES_CHANGED, null));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 退出全屏回调
        if (resultCode == Constants.FULLSCREEN_CODE && data != null) {
            int position = ImageDataHolder.getCollectionCurrentItem();
            mRvCollection.scrollToPosition(position);
        }

        // 检查权限回调
        if (requestCode == Constants.STORAGE_PERMISSION_CODE) {
            mPermissionUtil.checkStoragePermission();
        }
    }

    @Override
    public void onBackPressed() {
        if (mCollectionAdapter.isEditing()) {
            cancelEdit(true);
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
