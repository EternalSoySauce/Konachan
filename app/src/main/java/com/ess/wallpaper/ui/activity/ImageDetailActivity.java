package com.ess.wallpaper.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ess.wallpaper.view.CustomDialog;
import com.ess.wallpaper.R;
import com.ess.wallpaper.adapter.ViewPagerImageDetailAdapter;
import com.ess.wallpaper.bean.ImageBean;
import com.ess.wallpaper.bean.ThumbBean;
import com.ess.wallpaper.global.Constants;
import com.ess.wallpaper.http.OkHttp;
import com.ess.wallpaper.service.DownloadService;
import com.ess.wallpaper.ui.fragment.CommentFragment;
import com.ess.wallpaper.ui.fragment.DetailFragment;
import com.ess.wallpaper.ui.fragment.ImageFragment;
import com.ess.wallpaper.utils.FileUtils;
import com.ess.wallpaper.utils.PermissionUtils;
import com.ess.wallpaper.utils.UIUtils;
import com.ess.wallpaper.view.SlidingTabLayout;

import java.io.File;
import java.util.ArrayList;

public class ImageDetailActivity extends AppCompatActivity {

    private ThumbBean mThumbBean;
    private ImageBean mImageBean;

    private ViewPager mVpImageDetail;
    private ArrayList<Fragment> mFragmentList = new ArrayList<>();
    private FragmentManager mFragmentManager;

    private PermissionUtils mPermissionUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);

        mFragmentManager = getSupportFragmentManager();
        if (savedInstanceState != null) {
            restoreData(savedInstanceState);
        }

        initData();
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

        for (Fragment fragment : mFragmentList) {
            mFragmentManager.putFragment(outState, fragment.getClass().getName(), fragment);
        }
    }

    private void restoreData(Bundle savedInstanceState) {
        mThumbBean = savedInstanceState.getParcelable(Constants.THUMB_BEAN);
        mImageBean = savedInstanceState.getParcelable(Constants.IMAGE_BEAN);

        mFragmentList.add(mFragmentManager.getFragment(savedInstanceState, ImageFragment.class.getName()));
        mFragmentList.add(mFragmentManager.getFragment(savedInstanceState, DetailFragment.class.getName()));
        mFragmentList.add(mFragmentManager.getFragment(savedInstanceState, CommentFragment.class.getName()));
    }

    private void initData() {
        if (mThumbBean == null) {
            mThumbBean = getIntent().getParcelableExtra(Constants.THUMB_BEAN);
            mImageBean = mThumbBean.imageBean;
        }
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

    private void showPromptToReloadImageDialog(final String imageUrl, final String filePath) {
        CustomDialog.showPromptToReloadImage(this, new CustomDialog.SimpleDialogActionListener() {
            @Override
            public void onPositive() {
                if (!OkHttp.getInstance().isUrlInDownloadQueue(imageUrl)) {
                    downloadBitmap(imageUrl, filePath);
                }
            }
        });
    }

    private void downloadBitmap(String url, String bitmapPath) {
        Intent downloadIntent = new Intent(ImageDetailActivity.this, DownloadService.class);
        downloadIntent.putExtra(Constants.JPEG_URL, url);
        downloadIntent.putExtra(Constants.BITMAP_PATH, bitmapPath);
        downloadIntent.putExtra(Constants.THUMB_BEAN, mThumbBean);
        downloadIntent.putExtra(Constants.IMAGE_BEAN, mImageBean);
        startService(downloadIntent);
        OkHttp.getInstance().addUrlToDownloadQueue(url);
    }

    private void saveImage() {
        if (mImageBean != null) {
            String url = mImageBean.posts[0].jpegUrl;
            String bitmapName = getImageHead() + FileUtils.encodeMD5String(url.replaceAll(".com|.net", ""))
                    + url.substring(url.lastIndexOf("."));
            File file = new File(Constants.IMAGE_DIR + "/" + bitmapName);
            if (!file.exists() && !OkHttp.getInstance().isUrlInDownloadQueue(url)) {
                downloadBitmap(url, file.getAbsolutePath());
            } else if (file.exists()) {
                showPromptToReloadImageDialog(url, file.getAbsolutePath());
            }
        } else {
            Toast.makeText(this, R.string.loading_image, Toast.LENGTH_SHORT).show();
        }
    }

    private String getImageHead() {
        String imgHead = "";
        String baseUrl = OkHttp.getBaseUrl(this);
        switch (baseUrl) {
            case Constants.BASE_URL_KONACHAN:
                imgHead = Constants.IMAGE_HEAD_KONACHAN;
                break;
            case Constants.BASE_URL_YANDE:
                imgHead = Constants.IMAGE_HEAD_YANDE;
                break;
        }
        return imgHead;
    }

    // fl_save_image点击事件
    public void saveImage(View view) {
        if (mPermissionUtil == null) {
            mPermissionUtil = new PermissionUtils(this, new PermissionUtils.SimplePermissionListener() {
                @Override
                public void onGranted() {
                    saveImage();
                }
            });
        }
        mPermissionUtil.checkStoragePermission();
    }
}
