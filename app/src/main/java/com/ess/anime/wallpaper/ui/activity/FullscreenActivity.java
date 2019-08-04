package com.ess.anime.wallpaper.ui.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.adapter.ViewPagerFullscreenAdapter;
import com.ess.anime.wallpaper.bean.CollectionBean;
import com.ess.anime.wallpaper.bean.MsgBean;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.model.helper.PermissionHelper;
import com.ess.anime.wallpaper.model.holder.ImageDataHolder;
import com.ess.anime.wallpaper.ui.view.MultipleMediaLayout;
import com.ess.anime.wallpaper.utils.BitmapUtils;
import com.ess.anime.wallpaper.utils.UIUtils;
import com.github.chrisbanes.photoview.OnOutsidePhotoTapListener;
import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;
import com.zjca.qqdialog.ActionSheetDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.OnClick;

public class FullscreenActivity extends BaseActivity implements OnPhotoTapListener,
        OnOutsidePhotoTapListener, View.OnLongClickListener {

    @BindView(R.id.layout_operate)
    ViewGroup mLayoutOperate;
    @BindView(R.id.tv_serial)
    TextView mTvSerial;
    @BindView(R.id.iv_menu)
    ImageView mIvMenu;
    @BindView(R.id.vp_full_screen)
    ViewPager mVpFullScreen;
    private ActionSheetDialog mActionSheet;

    private List<CollectionBean> mCollectionList;
    private int mCurrentPos;
    private boolean mEnlarge;

    private boolean mForResult = true;

    @Override
    int layoutRes() {
        return R.layout.activity_fullscreen;
    }

    @Override
    void init(Bundle savedInstanceState) {
        mCollectionList = ImageDataHolder.getCollectionList();
        mCurrentPos = ImageDataHolder.getCollectionCurrentItem();
        mEnlarge = getIntent().getBooleanExtra(Constants.ENLARGE, false);
        if (mCollectionList.isEmpty() || !PermissionHelper.hasPermissions(this, Permission.Group.STORAGE)) {
            finish();
            return;
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
        UIUtils.hideNavigationBar(this);

        String url = mCollectionList.get(mVpFullScreen.getCurrentItem()).url;
        // 发送通知到MultipleMediaLayout
        EventBus.getDefault().post(new MsgBean(Constants.RESUME_VIDEO, url));
    }

    @Override
    protected void onPause() {
        super.onPause();
        String url = mCollectionList.get(mVpFullScreen.getCurrentItem()).url;
        // 发送通知到MultipleMediaLayout
        EventBus.getDefault().post(new MsgBean(Constants.PAUSE_VIDEO, url));
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        UIUtils.hideNavigationBar(this);
    }

    private void initViews() {
        setSerial();
        mIvMenu.setVisibility(mEnlarge ? View.GONE : View.VISIBLE);
    }

    private void initFullScreenViewPager() {
        mVpFullScreen.setAdapter(new ViewPagerFullscreenAdapter(mCollectionList));
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
                    mediaLayout.setOnClickListener(v -> toggleOperateLayout());
                    mediaLayout.setOnLongClickListener(FullscreenActivity.this);

                    PhotoView photoView = mediaLayout.getPhotoView();
                    photoView.setOnPhotoTapListener(FullscreenActivity.this);
                    photoView.setOnOutsidePhotoTapListener(FullscreenActivity.this);
                    photoView.setOnLongClickListener(FullscreenActivity.this);
                }

                String url = mCollectionList.get(position).url;
                // 发送通知到MultipleMediaLayout
                EventBus.getDefault().post(new MsgBean(Constants.START_VIDEO, url));
            }
        };
        mVpFullScreen.addOnPageChangeListener(listener);
        mVpFullScreen.post(() -> listener.onPageSelected(mCurrentPos));
    }

    @Override
    public void onPhotoTap(ImageView view, float x, float y) {
        toggleOperateLayout();
    }

    @Override
    public void onOutsidePhotoTap(ImageView imageView) {
        toggleOperateLayout();
    }

    private void toggleOperateLayout() {
        mLayoutOperate.setVisibility(View.GONE - mLayoutOperate.getVisibility());
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
        mActionSheet.builder()
                .addSheetItem(getString(R.string.action_wallpaper), null, which -> setAsWallpaper())
                .addSheetItem(getString(R.string.action_custom_wallpaper), null, which -> customWallpaper())
                .addSheetItem(getString(R.string.action_share), null, which -> shareImage());
    }

    @OnClick(R.id.iv_menu)
    void showMenu() {
        mActionSheet.show();
    }

    private void setSerial() {
        if (!mEnlarge) {
            String serial = (mCurrentPos + 1) + "/" + mCollectionList.size();
            mTvSerial.setText(serial);
        }
    }

    private void setAsWallpaper() {
        try {
            // todo 小米手机只能跳转到设置联系人头像
            Uri uri = BitmapUtils.getContentUriFromFile(this,
                    new File(mCollectionList.get(mCurrentPos).filePath));
            Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra("mimeType", "video/*;image/*");
            intent.setData(uri);
            startActivity(Intent.createChooser(intent, getString(R.string.action_wallpaper)));
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.cannot_set_as_wallpaper, Toast.LENGTH_SHORT).show();
        }
    }

    private void customWallpaper() {
        Uri sourceUri = BitmapUtils.getContentUriFromFile(this,
                new File(mCollectionList.get(mCurrentPos).filePath));
        Uri destinationUri = AndPermission.getFileUri(this, new File(getFilesDir(), UUID.randomUUID().toString()));
        int[] screenSize = QMUIDisplayHelper.getRealScreenSize(this);
        int screenWidth = screenSize[0];
        int screenHeight = screenSize[1];
//        UCrop.of(sourceUri, destinationUri)
//                .withAspectRatio(screenWidth, screenHeight)
//                .withMaxResultSize(screenWidth, screenHeight)
//                .start(this);
        Intent intent = new Intent(this, CropWallpaperActivity.class);
        intent.putExtra(CropWallpaperActivity.FILE_URI, sourceUri);
        startActivity(intent);
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
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mActionSheet.setDialogWidth(UIUtils.getScreenWidth(this));
    }

    @OnClick(R.id.iv_back)
    @Override
    public void onBackPressed() {
        ActivityCompat.finishAfterTransition(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ImageDataHolder.clearCollectionList();
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
