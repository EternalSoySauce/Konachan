package com.ess.anime.wallpaper.ui.activity;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v13.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.adapter.ViewPagerFullscreenAdapter;
import com.ess.anime.wallpaper.bean.CollectionBean;
import com.ess.anime.wallpaper.bean.MsgBean;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.utils.BitmapUtils;
import com.ess.anime.wallpaper.utils.UIUtils;
import com.zjca.qqdialog.ActionSheetDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.ArrayList;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class FullscreenActivity extends AppCompatActivity implements View.OnClickListener,
        PhotoViewAttacher.OnViewTapListener, View.OnLongClickListener {

    private final static int PAGE_LIMIT = 1;

    private TextView mTvSerial;
    private ViewPager mVpFullScreen;
    private ActionSheetDialog mActionSheet;
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
        initActionSheetDialog();
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
            photoView.setOnViewTapListener(this);
            photoView.setOnLongClickListener(this);
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
                        photoView.setOnViewTapListener(FullscreenActivity.this);
                        photoView.setOnLongClickListener(FullscreenActivity.this);
                    }
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;

            case R.id.iv_menu:
                mActionSheet.show();
                break;
        }
    }

    @Override
    public void onViewTap(View view, float x, float y) {
        onBackPressed();
    }

    @Override
    public boolean onLongClick(View v) {
        v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        mActionSheet.show();
        return true;
    }

    private void initActionSheetDialog() {
        mActionSheet = new ActionSheetDialog(this);
        mActionSheet.builder().addSheetItem(getString(R.string.action_wallpaper), null, new ActionSheetDialog.OnSheetItemClickListener() {
            @Override
            public void onClick(int which) {
                setAsWallpaper();
            }
        }).addSheetItem(getString(R.string.action_share), null, new ActionSheetDialog.OnSheetItemClickListener() {
            @Override
            public void onClick(int which) {
                shareImage();
            }
        }).setDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                UIUtils.hideBar(FullscreenActivity.this, true);
            }
        });
    }

    private void setSerial() {
        if (!mEnlarge) {
            String serial = (mCurrentPos + 1) + "/" + mCollectionList.size();
            mTvSerial.setText(serial);
        }
    }

    private void setAsWallpaper() {
        try {
            Uri uri = BitmapUtils.getContentUriFromFile(this,
                    new File(mCollectionList.get(mCurrentPos).filePath));
            Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setType("image/*");
            intent.setData(uri);
            startActivity(Intent.createChooser(intent, getString(R.string.action_wallpaper)));
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.cannot_set_as_wallpaper, Toast.LENGTH_SHORT).show();
        }
    }

    private void shareImage() {
        Uri uri = Uri.parse(mCollectionList.get(mCurrentPos).url);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(intent, getString(R.string.share_title)));
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
            onBackPressed();
        }
    }
}
