package com.ess.anime.wallpaper.model.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.AppOpsManagerCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.ess.anime.wallpaper.view.CustomDialog;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import com.yanzhenjie.permission.Setting;

import java.util.ArrayList;
import java.util.List;

public class PermissionHelper {

    /**
     * 检测组件是否拥有某权限
     *
     * @param context     上下文
     * @param permissions 需检测的权限
     * @return 是否拥有该权限
     */
    public static boolean hasPermissions(Context context, @NonNull String... permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;
        for (String permission : permissions) {
            int result = ContextCompat.checkSelfPermission(context, permission);
            if (result == PackageManager.PERMISSION_DENIED) return false;

            String op = AppOpsManagerCompat.permissionToOp(permission);
            if (TextUtils.isEmpty(op)) continue;
            result = AppOpsManagerCompat.noteProxyOp(context, op, context.getPackageName());
            if (result != AppOpsManagerCompat.MODE_ALLOWED) return false;

        }
        return true;
    }

    /**
     * 判断该应用程序是否缺少所需权限
     *
     * @param context     上下文
     * @param permissions 所需权限集合
     * @return 是否缺少所需权限
     */
    public static List<String> lackPermissions(Context context, String... permissions) {
        ArrayList<String> lacks = new ArrayList<>();
        for (String permission : permissions) {
            if (!hasPermissions(context, permission)) {
                lacks.add(permission);
            }
        }
        return lacks;
    }

    /**
     * 权限是否被永久拒绝
     *
     * @param context     上下文
     * @param permissions 权限组
     * @return 是否被永久拒绝
     */
    public static boolean hasAlwaysDeniedPermission(Context context, String... permissions) {
        List<String> lackList = lackPermissions(context, permissions);
        if (lackList.isEmpty()) {
            return false;
        }

        String permission = lackList.get(0);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean firstTime = preferences.getBoolean(permission, true);
        if (firstTime) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(permission, false);
            editor.apply();
            return false;
        }
        return AndPermission.hasAlwaysDeniedPermission(context, permission);
    }

    /**
     * 检查权限
     *
     * @param context  上下文
     * @param listener 事件监听器
     */
    public static void checkStoragePermissions(final Context context, final RequestListener listener) {
        final String[] permissions = Permission.Group.STORAGE;
        if (AndPermission.hasPermissions(context, permissions)) {
            if (listener != null) {
                listener.onGranted();
            }
        } else {
            CustomDialog.showRequestStoragePermissionDialog(context, new CustomDialog.SimpleDialogActionListener() {
                @Override
                public void onPositive() {
                    super.onPositive();
                    if (!hasAlwaysDeniedPermission(context, permissions)) {
                        AndPermission.with(context)
                                .runtime()
                                .permission(permissions)
                                .onGranted(new Action<List<String>>() {
                                    @Override
                                    public void onAction(List<String> data) {
                                        if (listener != null) {
                                            listener.onGranted();
                                        }
                                    }
                                })
                                .onDenied(new Action<List<String>>() {
                                    @Override
                                    public void onAction(List<String> data) {
                                        if (listener != null) {
                                            listener.onDenied();
                                        }
                                    }
                                }).start();
                    } else {
                        AndPermission.with(context)
                                .runtime()
                                .setting()
                                .onComeback(new Setting.Action() {
                                    @Override
                                    public void onAction() {
                                        if (listener != null) {
                                            if (AndPermission.hasPermissions(context, permissions)) {
                                                listener.onGranted();
                                            } else {
                                                listener.onDenied();
                                            }
                                        }
                                    }
                                }).start();
                    }
                }

                @Override
                public void onNegative() {
                    super.onNegative();
                    if (listener != null) {
                        listener.onDenied();
                    }
                }
            });
        }
    }

    public interface RequestListener {
        void onGranted();

        void onDenied();
    }

    public static class SimpleRequestListener implements RequestListener {

        @Override
        public void onGranted() {
        }

        @Override
        public void onDenied() {
        }
    }

}
