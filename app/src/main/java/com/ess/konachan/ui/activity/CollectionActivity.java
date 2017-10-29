package com.ess.konachan.ui.activity;

import android.content.Intent;
import android.media.ExifInterface;
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

import com.ess.konachan.R;
import com.ess.konachan.adapter.RecyclerCollectionAdapter;
import com.ess.konachan.bean.CollectionBean;
import com.ess.konachan.global.Constants;
import com.ess.konachan.utils.BitmapUtils;
import com.ess.konachan.utils.FileUtils;
import com.ess.konachan.utils.UIUtils;
import com.ess.konachan.view.CustomDialog;
import com.ess.konachan.view.GridDividerItemDecoration;
import com.mixiaoxiao.smoothcompoundbutton.SmoothCheckBox;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CollectionActivity extends AppCompatActivity implements View.OnClickListener {

    private Toolbar mToolbar;
    private TextView mTvEdit;
    private RelativeLayout mLayoutEditing;
    private SmoothCheckBox mCbChooseAll;
    private TextView mTvChooseCount;

    private RecyclerView mRvCollection;
    private RecyclerCollectionAdapter mCollectionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);

        initToolBarLayout();
        initEditView();
        initRecyclerView();
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
                ArrayList<CollectionBean> deleteList = new ArrayList<>();
                deleteList.addAll(mCollectionAdapter.getChooseList());
                String msg = getString(R.string.dialog_delete_msg1)
                        + deleteList.size() + getString(R.string.dialog_delete_msg2);
                showDeleteCollectionDialog(msg, deleteList);
                break;
        }
    }

    private void initRecyclerView() {
        mRvCollection = (RecyclerView) findViewById(R.id.rv_collection);
        mRvCollection.setLayoutManager(new GridLayoutManager(this, 3));
        mCollectionAdapter = new RecyclerCollectionAdapter(this, getCollectionImages());
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
                // TODO 点击全屏查看图片缩放动画
                ActivityOptionsCompat compat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        CollectionActivity.this, new Pair<View, String>(imageView, "s"));
                Intent intent = new Intent(CollectionActivity.this, FullscreenActivity.class);
                intent.putExtra(Constants.FULLSCREEN_POSITION, position);
                intent.putExtra(Constants.COLLECTION_LIST, mCollectionAdapter.getCollectionList());
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
                ArrayList<CollectionBean> enlargeList = new ArrayList<>();
                enlargeList.add(mCollectionAdapter.getCollectionList().get(position));
                Intent intent = new Intent(CollectionActivity.this, FullscreenActivity.class);
                intent.putExtra(Constants.COLLECTION_LIST, enlargeList);
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
        CustomDialog.showDeleteCollectionDialog(this, msg, new CustomDialog.OnDialogActionListener() {
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

    private ArrayList<CollectionBean> getCollectionImages() {
        ArrayList<CollectionBean> collectionList = new ArrayList<>();
        File folder = new File(Constants.IMAGE_DIR);
        if (folder.exists() && !folder.isFile()) {
            List<File> imageFiles = Arrays.asList(folder.listFiles());
            Collections.sort(imageFiles, new FileOrderComparator());
            for (File file : imageFiles) {
                String imagePath = file.getAbsolutePath();
                String imageUrl = "file://" + imagePath;
                try {
                    ExifInterface exifInterface = new ExifInterface(imagePath);
                    String width = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
                    String height = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
                    collectionList.add(new CollectionBean(imageUrl, width, height));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return collectionList;
    }

    public void shareImages() {
        ArrayList<Uri> uriList = new ArrayList<>();
        for (CollectionBean collectionBean : mCollectionAdapter.getChooseList()) {
            Uri uri = Uri.parse(collectionBean.url);
            uriList.add(uri);
        }
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("image/*");
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Intent.createChooser(intent, getString(R.string.share_title)));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Constants.FULLSCREEN_CODE && data != null) {
            int position = data.getIntExtra(Constants.FULLSCREEN_POSITION, 0);
            mRvCollection.scrollToPosition(position);
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

    class FileOrderComparator implements Comparator<File> {

        @Override
        public int compare(File lhs, File rhs) {
            return lhs.lastModified() < rhs.lastModified() ? 1
                    : (lhs.lastModified() > rhs.lastModified() ? -1 : 0);
        }
    }
}
