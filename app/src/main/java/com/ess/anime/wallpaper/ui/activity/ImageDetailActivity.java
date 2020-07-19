package com.ess.anime.wallpaper.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.download.image.DownloadBean;
import com.ess.anime.wallpaper.bean.ImageBean;
import com.ess.anime.wallpaper.bean.PostBean;
import com.ess.anime.wallpaper.bean.ThumbBean;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.http.OkHttp;
import com.ess.anime.wallpaper.listener.OnTouchAlphaListener;
import com.ess.anime.wallpaper.model.helper.PermissionHelper;
import com.ess.anime.wallpaper.model.holder.ImageDataHolder;
import com.ess.anime.wallpaper.download.image.DownloadImageService;
import com.ess.anime.wallpaper.ui.fragment.CommentFragment;
import com.ess.anime.wallpaper.ui.fragment.DetailFragment;
import com.ess.anime.wallpaper.ui.fragment.ImageFragment;
import com.ess.anime.wallpaper.ui.view.CustomDialog;
import com.ess.anime.wallpaper.utils.FileUtils;
import com.ess.anime.wallpaper.website.WebsiteManager;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;
import com.yanzhenjie.permission.runtime.Permission;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.OnClick;

public class ImageDetailActivity extends BaseActivity {

    @BindView(R.id.tv_id)
    TextView mTvId;
    @BindView(R.id.smart_tab)
    SmartTabLayout mSmartTab;
    @BindView(R.id.vp_image_detail)
    ViewPager mVpImageDetail;

    private ThumbBean mThumbBean;
    private ImageBean mImageBean;
    private int mCurrentPage;

    private Handler mHandler = new Handler();

    @Override
    int layoutRes() {
        return R.layout.activity_image_detail;
    }

    @Override
    void init(Bundle savedInstanceState) {
        initData(savedInstanceState);
        initViews();
        initToolBarLayout();
        initViewPager();
        initSlidingTabLayout();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // 防止软件进入后台过久被系统回收导致切换回来时产生空指针异常
        outState.putParcelable(Constants.THUMB_BEAN, mThumbBean);
        outState.putParcelable(Constants.IMAGE_BEAN, mImageBean);
        outState.putInt(Constants.CURRENT_PAGE, mCurrentPage);
    }

    private void initData(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            mCurrentPage = getIntent().getIntExtra(Constants.CURRENT_PAGE, 0);
            mThumbBean = getIntent().getParcelableExtra(Constants.THUMB_BEAN);
            setImageBean(mThumbBean.imageBean);
        } else {
            mThumbBean = savedInstanceState.getParcelable(Constants.THUMB_BEAN);
            setImageBean(savedInstanceState.getParcelable(Constants.IMAGE_BEAN));
            mCurrentPage = savedInstanceState.getInt(Constants.CURRENT_PAGE, 0);
        }
    }

    private void initViews() {
        OnTouchAlphaListener listener = new OnTouchAlphaListener(1f, 0.7f);
        findViewById(R.id.iv_previous).setOnTouchListener(listener);
        findViewById(R.id.iv_next).setOnTouchListener(listener);
    }

    private void initToolBarLayout() {
        if (mImageBean != null) {
            setId(mImageBean);
        }
    }

    private void initViewPager() {
        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add(R.string.image_detail_image, ImageFragment.class)
                .add(R.string.image_detail_detail, DetailFragment.class)
                .add(R.string.image_detail_comment, CommentFragment.class)
                .create());
        mVpImageDetail.setAdapter(adapter);
        mVpImageDetail.setOffscreenPageLimit(adapter.getCount());
        mVpImageDetail.setCurrentItem(mCurrentPage);
    }

    private void initSlidingTabLayout() {
        mSmartTab.setViewPager(mVpImageDetail);
    }

    public void setId(ImageBean imageBean) {
        String id = getString(R.string.image_id_symbol) + imageBean.posts[0].id;
        mTvId.setText(id);
    }

    public ThumbBean getThumbBean() {
        return mThumbBean;
    }

    public void setImageBean(ImageBean imageBean) {
        if (imageBean != null && imageBean.hasPostBean()) {
            mImageBean = imageBean;
        }
    }

    public ImageBean getImageBean() {
        return mImageBean;
    }

    private void showChooseToDownloadDialog() {
        if (mImageBean != null) {
            CustomDialog.showChooseToDownloadDialog(this, makeDownloadChosenList(), new CustomDialog.SimpleDialogActionListener() {
                @Override
                public void onDownloadChosen(List<DownloadBean> chosenList) {
                    saveImage(chosenList);
                }
            });
        } else {
            Toast.makeText(this, R.string.loading_image, Toast.LENGTH_SHORT).show();
        }
    }

    private List<DownloadBean> makeDownloadChosenList() {
        PostBean postBean = mImageBean.posts[0];
        List<DownloadBean> downloadList = new ArrayList<>();
        File file;
        String desc;
        boolean exists;
        // 0.Sample size
        if (postBean.sampleFileSize != 0 && !TextUtils.equals(postBean.fileUrl, postBean.sampleUrl)) {
            desc = getString(R.string.dialog_download_sample,
                    postBean.sampleWidth, postBean.sampleHeight,
                    FileUtils.computeFileSize(postBean.sampleFileSize),
                    FileUtils.getFileExtension(postBean.sampleUrl).toUpperCase());
            file = makeFileToSave(postBean.id, "-Sample", postBean.sampleUrl);
            exists = file.exists();
            if (exists) {
                desc = getString(R.string.dialog_download_already, desc);
            }
            downloadList.add(new DownloadBean(0, postBean.sampleUrl, postBean.sampleFileSize,
                    getString(R.string.download_title_sample, postBean.id), mThumbBean.thumbUrl,
                    file.getAbsolutePath(), exists, desc));
        }

        // 1.Large size
        desc = getString(R.string.dialog_download_large,
                postBean.jpegWidth, postBean.jpegHeight,
                FileUtils.computeFileSize(postBean.fileSize),
                FileUtils.getFileExtension(postBean.fileUrl).toUpperCase());
        file = makeFileToSave(postBean.id, "-Large", postBean.fileUrl);
        exists = file.exists();
        if (exists) {
            desc = getString(R.string.dialog_download_already, desc);
        }
        downloadList.add(new DownloadBean(1, postBean.fileUrl, postBean.fileSize,
                getString(R.string.download_title_large, postBean.id), mThumbBean.thumbUrl,
                file.getAbsolutePath(), exists, desc));

        // 2.Origin size
        if (postBean.jpegFileSize != 0 && !TextUtils.equals(postBean.fileUrl, postBean.jpegUrl)) {
            desc = getString(R.string.dialog_download_origin,
                    postBean.jpegWidth, postBean.jpegHeight,
                    FileUtils.computeFileSize(postBean.jpegFileSize),
                    FileUtils.getFileExtension(postBean.jpegUrl).toUpperCase());
            file = makeFileToSave(postBean.id, "-Origin", postBean.jpegUrl);
            exists = file.exists();
            if (exists) {
                desc = getString(R.string.dialog_download_already, desc);
            }
            downloadList.add(new DownloadBean(2, postBean.jpegUrl, postBean.jpegFileSize,
                    getString(R.string.download_title_origin, postBean.id), mThumbBean.thumbUrl,
                    file.getAbsolutePath(), exists, desc));
        }
        return downloadList;
    }

    private void saveImage(List<DownloadBean> downloadList) {
        for (DownloadBean downloadBean : downloadList) {
            if (downloadBean.fileExists) {
                showPromptToReloadImageDialog(downloadList);
                return;
            }
        }
        downloadBitmaps(downloadList);
    }

    private void showPromptToReloadImageDialog(final List<DownloadBean> downloadList) {
        CustomDialog.showPromptToReloadImage(this, new CustomDialog.SimpleDialogActionListener() {
            @Override
            public void onPositive() {
                downloadBitmaps(downloadList);
            }
        });
    }

    private void downloadBitmaps(List<DownloadBean> downloadList) {
        for (int i = 0; i < downloadList.size(); i++) {
            final DownloadBean downloadBean = downloadList.get(i);
            mHandler.postDelayed(() -> {
                if (!OkHttp.isUrlInDownloadQueue(downloadBean.downloadUrl)) {
                    Intent downloadIntent = new Intent(ImageDetailActivity.this, DownloadImageService.class);
                    downloadIntent.putExtra(Constants.DOWNLOAD_BEAN, downloadBean);
                    startService(downloadIntent);
                    OkHttp.addUrlToDownloadQueue(downloadBean.downloadUrl);
                }
            }, i * 100);
        }
        // TODO 翻译
        Toast.makeText(this, "已加入下载队列", Toast.LENGTH_SHORT).show();
    }

    private File makeFileToSave(String postId, String fileType, String url) {
        String extension = FileUtils.getFileExtensionWithDot(url);
//        url = url.substring(0, url.lastIndexOf(extension) + extension.length()).replaceAll(".com|.net", "");
//        String bitmapName = getImageHead() + FileUtils.encodeMD5String(url) + extension;
        // 图片命名方式改为"网站名-图片id-图片尺寸"样式，eg. Konachan-123456-Sample.jpg
        // 但这样无法识别此版本(v1.7)之前下载的图片是下载过的
        String imgHead = WebsiteManager.getInstance().getWebsiteConfig().getSavedImageHead();
        String bitmapName = imgHead + postId + fileType + extension;
        return new File(Constants.IMAGE_DIR, bitmapName);
    }

    // 下载图片点击事件
    @OnClick(R.id.tv_save)
    void saveImage() {
        PermissionHelper.checkStoragePermissions(this, new PermissionHelper.SimpleRequestListener() {
            @Override
            public void onGranted() {
                super.onGranted();
                showChooseToDownloadDialog();
            }
        });
    }

    // 查看上一张图片点击事件
    @OnClick(R.id.iv_previous)
    void previousImage() {
        ThumbBean thumbBean = ImageDataHolder.previousThumb();
        if (thumbBean != null) {
            Intent intent = new Intent(this, ImageDetailActivity.class);
            intent.putExtra(Constants.THUMB_BEAN, thumbBean);
            intent.putExtra(Constants.CURRENT_PAGE, mVpImageDetail.getCurrentItem());
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        } else {
            Toast.makeText(this, R.string.already_first_image, Toast.LENGTH_SHORT).show();
        }
    }

    // 查看下一张图片点击事件
    @OnClick(R.id.iv_next)
    void nextImage() {
        ThumbBean thumbBean = ImageDataHolder.nextThumb();
        if (thumbBean != null) {
            Intent intent = new Intent(this, ImageDetailActivity.class);
            intent.putExtra(Constants.THUMB_BEAN, thumbBean);
            intent.putExtra(Constants.CURRENT_PAGE, mVpImageDetail.getCurrentItem());
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else {
            Toast.makeText(this, R.string.already_last_image, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PermissionHelper.REQ_CODE_PERMISSION) {
            // 进入系统设置界面请求权限后的回调
            if (PermissionHelper.hasPermissions(this, Permission.Group.STORAGE)) {
                showChooseToDownloadDialog();
            }
        }
    }

    @OnClick(R.id.fl_back)
    @Override
    public void finish() {
        super.finish();
    }
}
