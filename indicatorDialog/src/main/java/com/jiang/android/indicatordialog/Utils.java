package com.jiang.android.indicatordialog;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by jiang on 2017/5/18.
 */

public class Utils {

    public static int[] getLocationInWindow(Activity context) {
        Resources resources = context.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        return new int[]{dm.widthPixels, dm.heightPixels};
    }

    public static int[] getLocationInContent(Activity context) {
        int[] location = getLocationInWindow(context);
        int height = location[1] - getStatusBarHeight(context);
        return new int[]{location[0], height};
    }

    public static int getStatusBarHeight(Activity context) {
        return new SystemBarConfig(context).getStatusBarHeight();
    }

    public static int getNavigationBarHeight(Activity context) {
        return new SystemBarConfig(context).getNavigationBarHeight();
    }

    /**
     * 获取屏幕实际尺寸
     *
     * @param activity activity
     * @return 屏幕实际尺寸 Point （宽：point.x, 高：point.y）
     */
    public static Point getRealScreenSize(Activity activity) {
        WindowManager windowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point point = new Point();

        if (Build.VERSION.SDK_INT >= 17) {
            display.getRealSize(point);
        } else if (Build.VERSION.SDK_INT >= 16) {
            View decorView = activity.getWindow().getDecorView();
            point.x = decorView.getWidth();
            point.y = decorView.getHeight();
        }
        return point;
    }

}
