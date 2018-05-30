package com.ess.anime.wallpaper.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.adapter.ViewPagerImageDetailAdapter;
import com.ess.anime.wallpaper.bean.DownloadBean;
import com.ess.anime.wallpaper.bean.ImageBean;
import com.ess.anime.wallpaper.bean.PostBean;
import com.ess.anime.wallpaper.bean.ThumbBean;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.global.ImageDataHolder;
import com.ess.anime.wallpaper.http.OkHttp;
import com.ess.anime.wallpaper.listener.OnTouchAlphaListener;
import com.ess.anime.wallpaper.service.DownloadImageService;
import com.ess.anime.wallpaper.ui.fragment.CommentFragment;
import com.ess.anime.wallpaper.ui.fragment.DetailFragment;
import com.ess.anime.wallpaper.ui.fragment.ImageFragment;
import com.ess.anime.wallpaper.utils.FileUtils;
import com.ess.anime.wallpaper.utils.PermissionUtils;
import com.ess.anime.wallpaper.utils.UIUtils;
import com.ess.anime.wallpaper.view.CustomDialog;
import com.ess.anime.wallpaper.view.SlidingTabLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageDetailActivity extends AppCompatActivity {

    private ThumbBean mThumbBean;
    private ImageBean mImageBean;
    private int mCurrentPage;

    private ViewPager mVpImageDetail;
    private ArrayList<Fragment> mFragmentList = new ArrayList<>();
    private FragmentManager mFragmentManager;

    private PermissionUtils mPermissionUtil;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);
        getWindow().setBackgroundDrawable(null);

        mFragmentManager = getSupportFragmentManager();
        if (savedInstanceState != null) {
            restoreData(savedInstanceState);
        }

        initData();
        initViews();
        initToolBarLayout();
        initViewPager();
        initSlidingTabLayout();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // 防止软件进入后台过久被系统回收导致切换回来时产生空指针异常
        outState.putParcelable(Constants.THUMB_BEAN, mThumbBean);
        outState.putParcelable(Constants.IMAGE_BEAN, mImageBean);
        outState.putInt(Constants.CURRENT_PAGE, mCurrentPage);

        for (Fragment fragment : mFragmentList) {
            mFragmentManager.putFragment(outState, fragment.getClass().getName(), fragment);
        }
    }

    private void restoreData(Bundle savedInstanceState) {
        mThumbBean = savedInstanceState.getParcelable(Constants.THUMB_BEAN);
        mImageBean = savedInstanceState.getParcelable(Constants.IMAGE_BEAN);
        mCurrentPage = savedInstanceState.getInt(Constants.CURRENT_PAGE, 0);

        mFragmentList.add(mFragmentManager.getFragment(savedInstanceState, ImageFragment.class.getName()));
        mFragmentList.add(mFragmentManager.getFragment(savedInstanceState, DetailFragment.class.getName()));
        mFragmentList.add(mFragmentManager.getFragment(savedInstanceState, CommentFragment.class.getName()));
    }

    private void initData() {
        if (mThumbBean == null) {
            mCurrentPage = getIntent().getIntExtra(Constants.CURRENT_PAGE, 0);
            mThumbBean = getIntent().getParcelableExtra(Constants.THUMB_BEAN);
            mImageBean = mThumbBean.imageBean;
        }
    }

    private void initViews() {
        OnTouchAlphaListener listener = new OnTouchAlphaListener(1f, 0.7f);
        findViewById(R.id.iv_previous).setOnTouchListener(listener);
        findViewById(R.id.iv_next).setOnTouchListener(listener);
    }

    private void initToolBarLayout() {
        FrameLayout flBack = (FrameLayout) findViewById(R.id.fl_back);
        flBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (mImageBean != null) {
            setId(mImageBean);
        }
    }

    private void initViewPager() {
        if (mFragmentList.isEmpty()) {
            mFragmentList.add(ImageFragment.newInstance(getString(R.string.image_detail_image)));
            mFragmentList.add(DetailFragment.newInstance(getString(R.string.image_detail_detail)));
            mFragmentList.add(CommentFragment.newInstance(getString(R.string.image_detail_comment)));
        }

        mVpImageDetail = (ViewPager) findViewById(R.id.vp_image_detail);
        mVpImageDetail.setAdapter(new ViewPagerImageDetailAdapter(mFragmentManager, mFragmentList));
        mVpImageDetail.setOffscreenPageLimit(mFragmentList.size() - 1);
        mVpImageDetail.setCurrentItem(mCurrentPage);
    }

    private void initSlidingTabLayout() {
        int colorSelected = getResources().getColor(R.color.color_text_selected);
        int colorUnselected = getResources().getColor(R.color.color_text_unselected);

        SlidingTabLayout slidingTab = (SlidingTabLayout) findViewById(R.id.sliding_tab_layout);
        slidingTab.setDistributeEvenly(true);
        slidingTab.setTabViewTextSizeSp(16);
        slidingTab.setTitleTextColor(colorSelected, colorUnselected);
        slidingTab.setTabStripWidth(UIUtils.dp2px(this, 60));
        slidingTab.setTabStripHeight(2.5f);
        slidingTab.setTabViewPaddingDp(12);
        slidingTab.setSelectedIndicatorColors(colorSelected);
        slidingTab.setViewPager(mVpImageDetail);
    }

    public void setId(ImageBean imageBean) {
        String id = getString(R.string.image_id_symbol) + imageBean.posts[0].id;
        TextView tvId = (TextView) findViewById(R.id.tv_id);
        tvId.setText(id);
    }

    public ThumbBean getThumbBean() {
        return mThumbBean;
    }

    public void setImageBean(ImageBean imageBean) {
        mImageBean = imageBean;
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
        if (postBean.sampleFileSize != 0 && !postBean.fileUrl.equals(postBean.sampleUrl)) {
            desc = getString(R.string.dialog_download_sample,
                    postBean.sampleWidth, postBean.sampleHeight,
                    FileUtils.computeFileSize(postBean.sampleFileSize),
                    postBean.sampleUrl.substring(postBean.sampleUrl.lastIndexOf(".") + 1).toUpperCase());
            file = makeFileToSave(postBean.sampleUrl);
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
                postBean.fileUrl.substring(postBean.fileUrl.lastIndexOf(".") + 1).toUpperCase());
        file = makeFileToSave(postBean.fileUrl);
        exists = file.exists();
        if (exists) {
            desc = getString(R.string.dialog_download_already, desc);
        }
        downloadList.add(new DownloadBean(1, postBean.fileUrl, postBean.fileSize,
                getString(R.string.download_title_large, postBean.id), mThumbBean.thumbUrl,
                file.getAbsolutePath(), exists, desc));

        // 2.Origin size
        if (postBean.jpegFileSize != 0 && !postBean.fileUrl.equals(postBean.jpegUrl)) {
            desc = getString(R.string.dialog_download_origin,
                    postBean.jpegWidth, postBean.jpegHeight,
                    FileUtils.computeFileSize(postBean.jpegFileSize),
                    postBean.jpegUrl.substring(postBean.jpegUrl.lastIndexOf(".") + 1).toUpperCase());
            file = makeFileToSave(postBean.jpegUrl);
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
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!OkHttp.getInstance().isUrlInDownloadQueue(downloadBean.downloadUrl)) {
                        Intent downloadIntent = new Intent(ImageDetailActivity.this, DownloadImageService.class);
                        downloadIntent.putExtra(Constants.DOWNLOAD_BEAN, downloadBean);
                        startService(downloadIntent);
                        OkHttp.getInstance().addUrlToDownloadQueue(downloadBean.downloadUrl);
                    }
                }
            }, i * 100);
        }
    }

    private File makeFileToSave(String url) {
        String bitmapName = getImageHead() + FileUtils.encodeMD5String(url.replaceAll(".com|.net", ""))
                + url.substring(url.lastIndexOf("."));
        return new File(Constants.IMAGE_DIR, bitmapName);
    }

    private String getImageHead() {
        String imgHead = "";
        String baseUrl = OkHttp.getBaseUrl(this);
        switch (baseUrl) {
            case Constants.BASE_URL_KONACHAN_S:
            case Constants.BASE_URL_KONACHAN_E:
                imgHead = Constants.IMAGE_HEAD_KONACHAN;
                break;
            case Constants.BASE_URL_YANDE:
                imgHead = Constants.IMAGE_HEAD_YANDE;
                break;
            case Constants.BASE_URL_LOLIBOORU:
                imgHead = Constants.IMAGE_HEAD_LOLIBOORU;
                break;
            case Constants.BASE_URL_DANBOORU:
                imgHead = Constants.IMAGE_HEAD_DANBOORU;
                break;
        }
        return imgHead;
    }

    // 下载图片点击事件
    public void saveImage(View view) {
        if (mPermissionUtil == null) {
            mPermissionUtil = new PermissionUtils(this, new PermissionUtils.SimplePermissionListener() {
                @Override
                public void onGranted() {
                    showChooseToDownloadDialog();
                }
            });
        }
        mPermissionUtil.checkStoragePermission();
    }

    // 查看上一张图片点击事件
    public void previousImage(View view) {
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
    public void nextImage(View view) {
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

}
