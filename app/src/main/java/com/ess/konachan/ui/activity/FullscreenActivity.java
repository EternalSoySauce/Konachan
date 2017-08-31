package com.ess.konachan.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v13.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.ess.konachan.R;
import com.ess.konachan.adapter.FullscreenViewPagerAdapter;
import com.ess.konachan.bean.CollectionBean;
import com.ess.konachan.global.Constants;

import java.util.ArrayList;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class FullscreenActivity extends AppCompatActivity {

    private final static int PAGE_LIMIT = 1;

    private ViewPager mVpFullScreen;
    private FullscreenViewPagerAdapter mFullScreenAdapter;
    private ArrayList<PhotoView> mPhotoViewList;
    private ArrayList<CollectionBean> mCollectionList;
    private int mCurrentPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        mCollectionList = getIntent().getParcelableArrayListExtra(Constants.COLLECTION_LIST);
        mCurrentPos = getIntent().getIntExtra(Constants.FULLSCREEN_POSITION, 0);
        initFullScreenViewPager();

//        ViewCompat.setTransitionName(mVpFullScreen, "s");
    }

    private void initFullScreenViewPager() {
        mPhotoViewList = new ArrayList<>();
        for (int i = 0; i < PAGE_LIMIT * 2 + 1; i++) {
            PhotoView photoView = new PhotoView(this);
            photoView.setBackgroundColor(Color.BLACK);
            photoView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            photoView.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
                @Override
                public void onViewTap(View view, float x, float y) {
                    finish();
                }
            });
            mPhotoViewList.add(photoView);
        }

        mVpFullScreen = (ViewPager) findViewById(R.id.vp_full_screen);
        mFullScreenAdapter = new FullscreenViewPagerAdapter(this, mPhotoViewList, mCollectionList);
        mVpFullScreen.setAdapter(mFullScreenAdapter);
        mVpFullScreen.setCurrentItem(mCurrentPos, false);
        mVpFullScreen.setOffscreenPageLimit(PAGE_LIMIT);
        mVpFullScreen.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                mCurrentPos = position;
                int childCount = mVpFullScreen.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    PhotoView photoView = (PhotoView) mVpFullScreen.getChildAt(i);
                    if (photoView != null) {
                        photoView.setScale(1f);
                        photoView.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
                            @Override
                            public void onViewTap(View view, float x, float y) {
                                onBackPressed();
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    public void finish() {
        Intent intent = new Intent();
        intent.putExtra(Constants.FULLSCREEN_POSITION, mCurrentPos);
        setResult(Constants.FULLSCREEN_CODE, intent);
        super.finish();
    }

    @Override
    public void onBackPressed() {
        ActivityCompat.finishAfterTransition(this);
    }
}
