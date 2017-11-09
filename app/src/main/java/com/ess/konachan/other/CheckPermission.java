package com.ess.konachan.other;

import android.app.Activity;

import com.ess.konachan.global.Constants;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import com.yanzhenjie.permission.PermissionNo;
import com.yanzhenjie.permission.PermissionYes;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;

import java.util.Arrays;
import java.util.List;

public class CheckPermission {

    private Activity mActivity;
    private OnPermissionListener mListener;

    public CheckPermission(Activity activity, OnPermissionListener listener) {
        mActivity = activity;
        mListener = listener;
    }

    public void checkStoragePermission() {
        AndPermission.with(mActivity)
                .requestCode(Constants.STORAGE_PERMISSION_CODE)
                .permission(Permission.STORAGE)
                .rationale(new RationaleListener() {
                    @Override
                    public void showRequestPermissionRationale(int requestCode, Rationale rationale) {
                        AndPermission.rationaleDialog(mActivity, rationale).show();
                    }
                })
                .callback(this)
                .start();
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
            // 使用AndPermission提供的默认设置dialog，用户点击确定后会打开App的设置页面让用户授权。
            AndPermission.defaultSettingDialog(mActivity, Constants.STORAGE_PERMISSION_CODE).show();
        }
    }

    public interface OnPermissionListener {
        void onGranted();
    }
}
