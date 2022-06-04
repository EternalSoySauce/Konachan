package com.ess.anime.wallpaper.ui.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.adapter.RecyclerFullscreenAdapter;
import com.ess.anime.wallpaper.bean.CollectionBean;
import com.ess.anime.wallpaper.bean.MsgBean;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.model.helper.PermissionHelper;
import com.ess.anime.wallpaper.model.holder.ImageDataHolder;
import com.ess.anime.wallpaper.ui.view.MultipleMediaLayout;
import com.ess.anime.wallpaper.utils.BitmapUtils;
import com.ess.anime.wallpaper.utils.UIUtils;
import com.ess.anime.wallpaper.utils.WallpaperUtils;
import com.github.chrisbanes.photoview.OnOutsidePhotoTapListener;
import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.yanzhenjie.permission.runtime.Permission;
import com.zjca.qqdialog.ActionSheetDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;

import androidx.core.app.ActivityCompat;
import androidx.viewpager2.widget.ViewPager2;
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
    ViewPager2 mVpFullScreen;
    private ActionSheetDialog mActionSheet;

    private RecyclerFullscreenAdapter mFullscreenAdapter;
    private boolean mEnlarge;

    private boolean mForResult = true;

    @Override
    protected int layoutOrientation() {
        return ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR;
    }

    @Override
    protected int layoutRes() {
        return R.layout.activity_fullscreen;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        QMUIStatusBarHelper.translucent(this);
        QMUIDisplayHelper.cancelFullScreen(this);
        mEnlarge = getIntent().getBooleanExtra(Constants.ENLARGE, false);
        if (ImageDataHolder.getCollectionList().isEmpty() || !PermissionHelper.hasPermissions(this, Permission.Group.STORAGE)) {
            finish();
            return;
        }

        initFullScreenViewPager();
        initActionSheetDialog();
        initNormalViews();

//        ViewCompat.setTransitionName(mVpFullScreen, "s");
    }

    @Override
    protected void updateUI() {
        super.updateUI();
        updateActionSheetWidth();
    }

    @Override
    protected void onResume() {
        super.onResume();
        UIUtils.hideNavigationBar(this);

        CollectionBean collectionBean = getCurrentCollection();
        if (collectionBean != null) {
            // 发送通知到MultipleMediaLayout
            EventBus.getDefault().post(new MsgBean(Constants.RESUME_VIDEO, collectionBean.url));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        CollectionBean collectionBean = getCurrentCollection();
        if (collectionBean != null) {
            // 发送通知到MultipleMediaLayout
            EventBus.getDefault().post(new MsgBean(Constants.PAUSE_VIDEO, collectionBean.url));
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        UIUtils.hideNavigationBar(this);
    }

    private void initNormalViews() {
        setSerial();
        mIvMenu.setVisibility(mEnlarge ? View.GONE : View.VISIBLE);
    }

    private void initFullScreenViewPager() {
        mFullscreenAdapter = new RecyclerFullscreenAdapter(ImageDataHolder.getCollectionList());
        mVpFullScreen.setAdapter(mFullscreenAdapter);
        mVpFullScreen.setCurrentItem(ImageDataHolder.getCollectionCurrentItem(), false);
        ViewPager2.OnPageChangeCallback callback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                ImageDataHolder.setCollectionCurrentItem(position);
                setSerial();

                int childCount = mVpFullScreen.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    MultipleMediaLayout mediaLayout = mVpFullScreen.getChildAt(i).findViewById(R.id.layout_multiple_media);
                    if (mediaLayout != null) {
                        mediaLayout.reset();
                    }
                }

                CollectionBean collectionBean = mFullscreenAdapter.getItem(position);
                if (collectionBean != null) {
                    // 发送通知到MultipleMediaLayout
                    EventBus.getDefault().post(new MsgBean(Constants.START_VIDEO,
                            new Object[]{collectionBean.url, mLayoutOperate.getVisibility()}));
                }
            }
        };
        mVpFullScreen.registerOnPageChangeCallback(callback);
        mVpFullScreen.post(() -> callback.onPageSelected(mVpFullScreen.getCurrentItem()));
    }

    @Override
    public void onPhotoTap(ImageView view, float x, float y) {
        toggleOperateLayout();
    }

    @Override
    public void onOutsidePhotoTap(ImageView imageView) {
        toggleOperateLayout();
    }

    public void toggleOperateLayout() {
        int visibility = View.GONE - mLayoutOperate.getVisibility();
        mLayoutOperate.setVisibility(visibility);
        // 发送通知到MultipleMediaLayout
        EventBus.getDefault().post(new MsgBean(Constants.TOGGLE_VIDEO_CONTROLLER, visibility));
        if (visibility == View.VISIBLE) {
            QMUIDisplayHelper.cancelFullScreen(this);
        } else {
            QMUIDisplayHelper.setFullScreen(this);
        }
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

    private void updateActionSheetWidth() {
        if (mActionSheet != null) {
            if (UIUtils.isLandscape(this)) {
                mActionSheet.setDialogWidth(Math.min(UIUtils.getScreenWidth(this), UIUtils.dp2px(this, 500)));
            } else {
                mActionSheet.setDialogWidth(UIUtils.getScreenWidth(this));
            }
        }
    }

    @OnClick(R.id.iv_menu)
    void showMenu() {
        mActionSheet.show();
    }

    private void setSerial() {
        if (!mEnlarge) {
            String serial = (mVpFullScreen.getCurrentItem() + 1) + "/" + mFullscreenAdapter.getItemCount();
            mTvSerial.setText(serial);
        }
    }

    private void setAsWallpaper() {
        CollectionBean collectionBean = getCurrentCollection();
        if (collectionBean != null) {
            File file = new File(collectionBean.filePath);
            Uri uri = BitmapUtils.getContentUriFromFile(this, file);
            WallpaperUtils.setWallpaperBySystemApp(this, uri);
        }
    }

    private void customWallpaper() {
        CollectionBean collectionBean = getCurrentCollection();
        if (collectionBean != null) {
            File file = new File(collectionBean.filePath);
            Uri sourceUri = BitmapUtils.getContentUriFromFile(this, file);
            Intent intent = new Intent(this, CropWallpaperActivity.class);
            intent.putExtra(CropWallpaperActivity.FILE_URI, sourceUri);
            startActivity(intent);
        }
    }

    private void shareImage() {
        CollectionBean collectionBean = getCurrentCollection();
        if (collectionBean != null) {
            Uri uri = Uri.parse(collectionBean.url);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("video/*;image/*");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(Intent.createChooser(intent, getString(R.string.share_title)));
        }
    }

    private CollectionBean getCurrentCollection() {
        return mFullscreenAdapter == null ? null : mFullscreenAdapter.getItem(mVpFullScreen.getCurrentItem());
    }

    @Override
    public void finish() {
        if (mForResult) {
            Intent intent = new Intent();
            setResult(Constants.FULLSCREEN_CODE, intent);
        }
        super.finish();
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
