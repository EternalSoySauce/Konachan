package com.ess.anime.wallpaper.model.helper;

import android.app.AppOpsManager;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.WindowManager;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.ui.view.CustomDialog;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

public class PermissionHelper {

    public final static int REQ_CODE_PERMISSION = 1000;

    /**
     * 检测组件是否拥有某权限
     *
     * @param context     上下文
     * @param permissions 需检测的权限
     * @return 是否拥有该权限
     */
    public static boolean hasPermissions(Context context, @NonNull String... permissions) {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;
//        for (String permission : permissions) {
//            int result = ContextCompat.checkSelfPermission(context, permission);
//            if (result == PackageManager.PERMISSION_DENIED) return false;
//
//            String op = AppOpsManagerCompat.permissionToOp(permission);
//            if (TextUtils.isEmpty(op)) continue;
//            result = AppOpsManagerCompat.noteProxyOp(context, op, context.getPackageName());
//            if (result != AppOpsManagerCompat.MODE_ALLOWED) return false;
//
//        }
//        return true;
        return AndPermission.hasPermissions(context, permissions);
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
     * 检查权限
     * 在Activity中重写onActivityResult，case REQ_CODE_PERMISSION，并重新调用hasPermissions()判断是否有权限
     *
     * @param context     上下文
     * @param dialogMsg   弹窗请求提示文字
     * @param listener    事件监听器
     * @param permissions 要检查的权限组
     */
    public static void checkPermissions(Context context, String dialogTitle, String dialogMsg, RequestListener listener, String... permissions) {
        if (AndPermission.hasPermissions(context, permissions)) {
            if (listener != null) {
                listener.onGranted();
            }
        } else {
            CustomDialog.showRequestPermissionDialog(context, dialogTitle, dialogMsg, new CustomDialog.SimpleDialogActionListener() {
                @Override
                public void onPositive() {
                    super.onPositive();
                    if (!hasAlwaysDeniedPermission(context, permissions)) {
                        AndPermission.with(context)
                                .runtime()
                                .permission(permissions)
                                .onGranted(data -> {
                                    if (listener != null) {
                                        listener.onGranted();
                                    }
                                })
                                .onDenied(data -> {
                                    if (listener != null) {
                                        listener.onDenied();
                                    }
                                }).start();
                    } else {
                        AndPermission.with(context)
                                .runtime()
                                .setting()
                                .start(REQ_CODE_PERMISSION);
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
     * 检查是否有悬浮窗权限
     *
     * @param context 上下文
     * @return 是否有悬浮窗权限
     */
    public static boolean hasOverlayPermission(Context context) {
        return canDrawOverlays(context) && tryDisplayDialog(context);
    }

    private static boolean tryDisplayDialog(Context context) {
        Dialog dialog = new Dialog(context, R.style.Permission_Theme);
        int overlay = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        int alertWindow = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        int windowType = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? overlay : alertWindow;
        dialog.getWindow().setType(windowType);
        try {
            dialog.show();
        } catch (Exception e) {
            return false;
        } finally {
            if (dialog.isShowing()) dialog.dismiss();
        }
        return true;
    }

    private static boolean canDrawOverlays(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.getApplicationInfo().targetSdkVersion >= Build.VERSION_CODES.M) {
                return Settings.canDrawOverlays(context);
            }
            return reflectionOps(context, "OP_SYSTEM_ALERT_WINDOW");
        }
        return true;
    }

    private static boolean reflectionOps(Context context, String opFieldName) {
        int uid = context.getApplicationInfo().uid;
        try {
            Class<AppOpsManager> appOpsClass = AppOpsManager.class;
            Method method = appOpsClass.getMethod("checkOpNoThrow", Integer.TYPE, Integer.TYPE, String.class);
            Field opField = appOpsClass.getDeclaredField(opFieldName);
            int opValue = (int) opField.get(Integer.class);
            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int result = (int) method.invoke(appOpsManager, opValue, uid, context.getApplicationContext().getPackageName());
            return result == AppOpsManager.MODE_ALLOWED || result == 4;
        } catch (Throwable e) {
            return true;
        }
    }

    public static void checkStoragePermissions(Context context, RequestListener listener) {
        String title = context.getString(R.string.dialog_permission_rationale_title);
        String msg = context.getString(R.string.dialog_permission_rationale_msg);
        checkPermissions(context, title, msg, listener, Permission.Group.STORAGE);
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
