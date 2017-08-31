package com.ess.konachan.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.ess.konachan.R;
import com.ess.konachan.adapter.ViewPagerImageDetailAdapter;
import com.ess.konachan.bean.ImageBean;
import com.ess.konachan.bean.ThumbBean;
import com.ess.konachan.global.Constants;
import com.ess.konachan.http.OkHttp;
import com.ess.konachan.service.DownloadService;
import com.ess.konachan.ui.fragment.CommentFragment;
import com.ess.konachan.ui.fragment.DetailFragment;
import com.ess.konachan.ui.fragment.ImageFragment;
import com.ess.konachan.utils.FileUtils;
import com.ess.konachan.utils.UIUtils;
import com.ess.konachan.view.SlidingTabLayout;

import java.io.File;
import java.util.ArrayList;

public class ImageDetailActivity extends AppCompatActivity {

    private ThumbBean mThumbBean;
    private ImageBean mImageBean;

    private ViewPager mVpImageDetail;
    private ArrayList<Fragment> mFragmentList = new ArrayList<>();
    private FragmentManager mFragmentManager;

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

        FrameLayout flSaveImage = (FrameLayout) findViewById(R.id.fl_save_image);
        flSaveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mImageBean != null) {
                    final String url = mImageBean.posts[0].jpegUrl;
                    String bitmapName = Constants.IMAGE_HEAD + FileUtils.encodeMD5String(url)
                            + url.substring(url.lastIndexOf("."));
                    final File file = new File(Constants.IMAGE_DIR + "/" + bitmapName);
                    if (!file.exists() && !OkHttp.getInstance().isUrlInDownloadQueue(url)) {
                        downloadBitmap(url, file.getAbsolutePath());
                    } else if (file.exists()) {
                        new MaterialDialog.Builder(ImageDetailActivity.this)
                                .content(R.string.reload_msg)
                                .negativeText(R.string.reload_no)
                                .positiveText(R.string.reload_yes)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        if (!OkHttp.getInstance().isUrlInDownloadQueue(url)) {
                                            downloadBitmap(url, file.getAbsolutePath());
                                        }
                                    }
                                }).show();
                    }
                } else {
                    Toast.makeText(ImageDetailActivity.this, R.string.loading_image, Toast.LENGTH_SHORT).show();
                }
            }
        });
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
        SlidingTabLayout slidingTab = (SlidingTabLayout) findViewById(R.id.sliding_tab_layout);
        slidingTab.setDistributeEvenly(true);
        slidingTab.setTabViewTextSizeSp(16);
        slidingTab.setTitleTextColor(Color.WHITE, R.color.colorPrimary);
        slidingTab.setTabStripWidth(UIUtils.dp2px(this, 60));
        slidingTab.setTabStripHeight(2.5f);
        slidingTab.setTabViewPaddingDp(12);
        slidingTab.setSelectedIndicatorColors(Color.WHITE);
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

    private void downloadBitmap(String url, String bitmapPath) {
        Intent downloadIntent = new Intent(ImageDetailActivity.this, DownloadService.class);
        downloadIntent.putExtra(Constants.JPEG_URL, url);
        downloadIntent.putExtra(Constants.BITMAP_PATH, bitmapPath);
        downloadIntent.putExtra(Constants.THUMB_BEAN, mThumbBean);
        downloadIntent.putExtra(Constants.IMAGE_BEAN, mImageBean);
        startService(downloadIntent);
        OkHttp.getInstance().addUrlToDownloadQueue(url);
    }
}
