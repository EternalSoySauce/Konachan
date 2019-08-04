package com.ess.anime.wallpaper.ui.activity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.model.helper.PermissionHelper;
import com.ess.anime.wallpaper.utils.WallpaperUtil;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropFragment;
import com.yalantis.ucrop.UCropFragmentCallback;
import com.yanzhenjie.permission.runtime.Permission;

import java.io.File;
import java.net.URI;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import butterknife.BindView;
import butterknife.OnClick;

public class CropWallpaperActivity extends BaseActivity implements UCropFragmentCallback {

    public final static String FILE_URI = "FILE_URI";

    @BindView(R.id.tool_bar)
    Toolbar mToolbar;

    private UCropFragment mUCropFragment;

    @Override
    int layoutRes() {
        return R.layout.activity_crop_wallpaper;
    }

    @Override
    void init(Bundle savedInstanceState) {
        Uri sourceUri = getIntent().getParcelableExtra(FILE_URI);
        if (sourceUri == null || !PermissionHelper.hasPermissions(this, Permission.Group.STORAGE)) {
            finish();
            return;
        }

        initToolBarLayout();
        initCropFragment(sourceUri);
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
                File file = new File(new URI(uri.toString()));
                WallpaperUtil.setWallpaper(this, file, WallpaperUtil.FLAG_DESKTOP);
                // todo toast
                Toast.makeText(this, "设置成功", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // todo toast
            Toast.makeText(this, "裁剪失败", Toast.LENGTH_SHORT).show();
        }
    }

}
