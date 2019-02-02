package com.ess.anime.wallpaper.model.helper;

import android.app.Activity;

import com.ess.anime.wallpaper.view.CustomDialog;
import com.ess.anime.wallpaper.global.Constants;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import com.yanzhenjie.permission.PermissionNo;
import com.yanzhenjie.permission.PermissionYes;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;
import com.yanzhenjie.permission.SettingService;

import java.util.Arrays;
import java.util.List;

public class PermissionHelper implements RationaleListener {

    private Activity mActivity;
    private OnPermissionListener mListener;

    public PermissionHelper(Activity activity, OnPermissionListener listener) {
        mActivity = activity;
        mListener = listener;
    }

    public void checkStoragePermission() {
        AndPermission.with(mActivity)
                .requestCode(Constants.STORAGE_PERMISSION_CODE)
                .permission(Permission.STORAGE)
                .rationale(this)
                .callback(this)
                .start();
    }

    @Override
    public void showRequestPermissionRationale(int requestCode, final Rationale rationale) {
        CustomDialog.showNeedStoragePermissionDialog(mActivity, new CustomDialog.SimpleDialogActionListener() {
            @Override
            public void onPositive() {
                rationale.resume();
            }

            @Override
            public void onNegative() {
                rationale.cancel();
            }
        });
    }

    @PermissionYes(Constants.STORAGE_PERMISSION_CODE)
    private void getPermissionYes(List<String> grantedPermissions) {
        checkPermissionAfterResult();
    }

    @PermissionNo(Constants.STORAGE_PERMISSION_CODE)
    private void getPermissionNo(List<String> deniedPermissions) {
        checkPermissionAfterResult();
    }

    // 适配国产机各种坑爹问题，每次请求权限后主动检查是否权限，不依赖返回结果
    private void checkPermissionAfterResult() {
        if (AndPermission.hasPermission(mActivity, Arrays.asList(Permission.STORAGE))) {
            mListener.onGranted();
        } else {
            // 用户点击确定后会打开App的设置页面让用户授权。
            final SettingService settingService = AndPermission.defineSettingDialog(mActivity, Constants.STORAGE_PERMISSION_CODE);
            CustomDialog.showGoToSettingDialog(mActivity, new CustomDialog.SimpleDialogActionListener() {
                @Override
                public void onPositive() {
                    settingService.execute();
                }

                @Override
                public void onNegative() {
                    settingService.cancel();
                    mListener.onDenied();
                }
            });
        }
    }

    public interface OnPermissionListener {
        void onGranted();

        void onDenied();
    }

    public static class SimplePermissionListener implements OnPermissionListener {

        @Override
        public void onGranted() {
        }

        @Override
        public void onDenied() {
        }
    }
}
