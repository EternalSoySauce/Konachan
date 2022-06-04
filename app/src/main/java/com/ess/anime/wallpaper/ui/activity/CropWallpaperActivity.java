package com.ess.anime.wallpaper.ui.activity;

import android.annotation.TargetApi;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.model.helper.PermissionHelper;
import com.ess.anime.wallpaper.ui.view.CustomDialog;
import com.ess.anime.wallpaper.utils.BitmapUtils;
import com.ess.anime.wallpaper.utils.UIUtils;
import com.ess.anime.wallpaper.utils.WallpaperUtils;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropFragment;
import com.yalantis.ucrop.UCropFragmentCallback;
import com.yanzhenjie.permission.runtime.Permission;
import com.zjca.qqdialog.ActionSheetDialog;

import java.io.File;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import butterknife.BindView;
import butterknife.OnClick;

public class CropWallpaperActivity extends BaseActivity implements UCropFragmentCallback {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    public final static String FILE_URI = "FILE_URI";

    @BindView(R.id.tool_bar)
    Toolbar mToolbar;

    private UCropFragment mUCropFragment;
    private ActionSheetDialog mActionSheet;

    @Override
    protected int layoutRes() {
        return R.layout.activity_crop_wallpaper;
    }

    @Override
    protected int layoutOrientation() {
        if (UIUtils.isLandscape(this)) {
            if (UIUtils.isLandscapeReverse(this)) {
                return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
            } else {
                return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            }
        } else {
            if (UIUtils.isPortraitReverse(this)) {
                return ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
            } else {
                return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            }
        }
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        Uri sourceUri = getIntent().getParcelableExtra(FILE_URI);
        if (sourceUri == null || !PermissionHelper.hasPermissions(this, Permission.Group.STORAGE)) {
            finish();
            return;
        }

        initToolBarLayout();
        initCropFragment(sourceUri);
        CustomDialog.checkToShowCannotCustomLockscreenWallpaperDialog(this);
    }

    @Override
    void updateUI() {
        super.updateUI();
        updateActionSheetWidth();
    }

    private void initToolBarLayout() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initCropFragment(Uri sourceUri) {
        createUCropFragment(sourceUri);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.fl_content, mUCropFragment);
        fragmentTransaction.commitAllowingStateLoss();
    }

    private void createUCropFragment(Uri sourceUri) {
        Uri destinationUri = Uri.fromFile(new File(getFilesDir(), "crop_wallpaper"));
        int[] screenSize = QMUIDisplayHelper.getRealScreenSize(this);
        int screenWidth = screenSize[0];
        int screenHeight = screenSize[1];

        Bundle bundle = new Bundle();
        bundle.putParcelable(UCrop.EXTRA_INPUT_URI, sourceUri);         // 图片uri
        bundle.putParcelable(UCrop.EXTRA_OUTPUT_URI, destinationUri);
        bundle.putFloat(UCrop.EXTRA_ASPECT_RATIO_X, screenWidth);       // 裁剪比例
        bundle.putFloat(UCrop.EXTRA_ASPECT_RATIO_Y, screenHeight);
        bundle.putInt(UCrop.EXTRA_MAX_SIZE_X, screenWidth);             // 裁剪最大尺寸
        bundle.putInt(UCrop.EXTRA_MAX_SIZE_Y, screenHeight);
        bundle.putInt(UCrop.Options.EXTRA_UCROP_COLOR_WIDGET_ACTIVE, android.R.color.white); // 底部操作栏背景色
        mUCropFragment = UCropFragment.newInstance(bundle);
    }

    @OnClick(R.id.iv_crop)
    void cropImage() {
        mUCropFragment.cropAndSaveImage();
    }

    @Override
    public void loadingProgress(boolean showLoader) {
    }

    @Override
    public void onCropFinish(UCropFragment.UCropResult result) {
        if (result.mResultCode == RESULT_OK && result.mResultData != null) {
            try {
                Uri uri = UCrop.getOutput(result.mResultData);
                String imagePath = BitmapUtils.getImagePathFromUri(this, uri);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    chooseWallpaperFlag(imagePath);
                } else {
                    WallpaperUtils.setWallpaperDirectly(this, imagePath);
                    Toast.makeText(this, R.string.set_successfully, Toast.LENGTH_SHORT).show();
                    finish();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, R.string.crop_failed, Toast.LENGTH_SHORT).show();
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    private void chooseWallpaperFlag(String imagePath) {
        if (mActionSheet == null) {
            mActionSheet = new ActionSheetDialog(this);
            mActionSheet.builder()
                    .addSheetItem(getString(R.string.action_wallpaper_flag_desktop), null, which -> {
                        setWallpaperDirectly(imagePath, WallpaperUtils.FLAG_HOME_SCREEN);
                    })
                    .addSheetItem(getString(R.string.action_wallpaper_flag_lockscreen), null, which -> {
                        setWallpaperDirectly(imagePath, WallpaperUtils.FLAG_LOCK_SCREEN);
                    })
                    .addSheetItem(getString(R.string.action_wallpaper_flag_both), null, which -> {
                        setWallpaperDirectly(imagePath, WallpaperUtils.FLAG_BOTH);
                    });
            updateActionSheetWidth();
        }
        mActionSheet.show();
    }

    @TargetApi(Build.VERSION_CODES.N)
    private void setWallpaperDirectly(String imagePath, int flag) {
        boolean successful = WallpaperUtils.setWallpaperDirectly(this, imagePath, flag);
        int toastRes = successful ? R.string.set_successfully : R.string.set_failed;
        Toast.makeText(this, toastRes, Toast.LENGTH_SHORT).show();
        finish();
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

}
