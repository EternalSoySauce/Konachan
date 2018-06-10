package com.ess.anime.wallpaper.ui.activity;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.adapter.ViewPagerFullscreenAdapter;
import com.ess.anime.wallpaper.bean.CollectionBean;
import com.ess.anime.wallpaper.bean.MsgBean;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.global.ImageDataHolder;
import com.ess.anime.wallpaper.utils.BitmapUtils;
import com.ess.anime.wallpaper.utils.UIUtils;
import com.ess.anime.wallpaper.view.MultipleMediaLayout;
import com.zjca.qqdialog.ActionSheetDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.List;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class FullscreenActivity extends AppCompatActivity implements View.OnClickListener,
        PhotoViewAttacher.OnViewTapListener, View.OnLongClickListener {

    private TextView mTvSerial;
    private ViewPager mVpFullScreen;
    private ActionSheetDialog mActionSheet;

    private List<CollectionBean> mCollectionList;
    private int mCurrentPos;
    private boolean mEnlarge;

    private boolean mForResult = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        mCollectionList = ImageDataHolder.getCollectionList();
        mCurrentPos = ImageDataHolder.getCollectionCurrentItem();
        mEnlarge = getIntent().getBooleanExtra(Constants.ENLARGE, false);

        initViews();
        initFullScreenViewPager();
        initActionSheetDialog();
        EventBus.getDefault().register(this);

//        ViewCompat.setTransitionName(mVpFullScreen, "s");
    }

    @Override
    protected void onResume() {
        super.onResume();
        UIUtils.hideStatusBar(this, true);
    }

    private void initViews() {
        mTvSerial = (TextView) findViewById(R.id.tv_serial);
        setSerial();
        if (mEnlarge) {
            findViewById(R.id.iv_menu).setVisibility(View.GONE);
        }
    }

    private void initFullScreenViewPager() {
        mVpFullScreen = findViewById(R.id.vp_full_screen);
        mVpFullScreen.setAdapter(new ViewPagerFullscreenAdapter(this, mCollectionList));
        mVpFullScreen.setCurrentItem(mCurrentPos, false);
        final ViewPager.OnPageChangeListener listener = new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                ImageDataHolder.setCollectionCurrentItem(position);
                mCurrentPos = position;
                setSerial();

                int childCount = mVpFullScreen.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    MultipleMediaLayout mediaLayout = (MultipleMediaLayout) mVpFullScreen.getChildAt(i);
                    mediaLayout.reset();
                    mediaLayout.setOnClickListener(FullscreenActivity.this);
                    mediaLayout.setOnLongClickListener(FullscreenActivity.this);

                    PhotoView photoView = mediaLayout.getPhotoView();
                    photoView.setOnViewTapListener(FullscreenActivity.this);
                    photoView.setOnLongClickListener(FullscreenActivity.this);
                }

                String url = mCollectionList.get(position).url;
                // 发送通知到MultipleMediaLayout
                EventBus.getDefault().post(new MsgBean(Constants.START_VIDEO, url));
            }
        };
        mVpFullScreen.addOnPageChangeListener(listener);
        mVpFullScreen.post(new Runnable() {
            @Override
            public void run() {
                listener.onPageSelected(mCurrentPos);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
            case R.id.layout_multiple_media:
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
        if (v instanceof PhotoView) {
            v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        }
        if (!mEnlarge) {
            mActionSheet.show();
        }
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
                UIUtils.hideStatusBar(FullscreenActivity.this, true);
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
            intent.setType("video/*;image/*");
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
        intent.setType("video/*;image/*");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(intent, getString(R.string.share_title)));
    }

    @Override
    public void finish() {
        if (mForResult) {
            Intent intent = new Intent();
            setResult(Constants.FULLSCREEN_CODE, intent);
        }
        super.finish();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mActionSheet.setDialogWidth(UIUtils.getScreenWidth(this));
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
            mForResult = false;
            onBackPressed();
        }
    }
}
