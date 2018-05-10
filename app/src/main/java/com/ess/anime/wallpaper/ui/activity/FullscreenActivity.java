package com.ess.anime.wallpaper.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v13.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.adapter.ViewPagerFullscreenAdapter;
import com.ess.anime.wallpaper.bean.CollectionBean;
import com.ess.anime.wallpaper.bean.MsgBean;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.utils.UIUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class FullscreenActivity extends AppCompatActivity {

    private final static int PAGE_LIMIT = 1;

    private TextView mTvSerial;
    private ViewPager mVpFullScreen;
    private ViewPagerFullscreenAdapter mFullScreenAdapter;
    private ArrayList<PhotoView> mPhotoViewList;
    private ArrayList<CollectionBean> mCollectionList;
    private int mCurrentPos;
    private boolean mEnlarge;

    private boolean forResult = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        mEnlarge = getIntent().getBooleanExtra(Constants.ENLARGE, false);
        if (mEnlarge) {
            mCollectionList = getIntent().getParcelableArrayListExtra(Constants.COLLECTION_LIST);
        } else {
            mCollectionList = CollectionBean.getCollectionImages();
            mCurrentPos = getIntent().getIntExtra(Constants.FULLSCREEN_POSITION, 0);
        }

        initViews();
        initFullScreenViewPager();
        EventBus.getDefault().register(this);

//        ViewCompat.setTransitionName(mVpFullScreen, "s");
    }

    @Override
    protected void onResume() {
        super.onResume();
        UIUtils.hideBar(this, true);
    }

    private void initViews() {
        mTvSerial = (TextView) findViewById(R.id.tv_serial);
        setSerial();
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
        mFullScreenAdapter = new ViewPagerFullscreenAdapter(this, mPhotoViewList, mCollectionList);
        mVpFullScreen.setAdapter(mFullScreenAdapter);
        mVpFullScreen.setCurrentItem(mCurrentPos, false);
        mVpFullScreen.setOffscreenPageLimit(PAGE_LIMIT);
        mVpFullScreen.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                mCurrentPos = position;
                setSerial();
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

    private void setSerial() {
        if (!mEnlarge) {
            String serial = (mCurrentPos + 1) + "/" + mCollectionList.size();
            mTvSerial.setText(serial);
        }
    }

    @Override
    public void finish() {
        if (forResult) {
            Intent intent = new Intent();
            intent.putExtra(Constants.FULLSCREEN_POSITION, mCurrentPos);
            setResult(Constants.FULLSCREEN_CODE, intent);
        }
        super.finish();
    }

    @Override
    public void onBackPressed() {
        ActivityCompat.finishAfterTransition(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    // 收藏夹本地文件发生变动后收到的通知，obj 为 null
    @Subscribe
    public void localFilesChanged(MsgBean msgBean) {
        if (msgBean.msg.equals(Constants.LOCAL_FILES_CHANGED)) {
            forResult = false;
            finish();
        }
    }
}
