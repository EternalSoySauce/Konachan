package com.ess.anime.wallpaper.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.design.internal.NavigationMenuPresenter;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.NavigationView;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import java.lang.reflect.Field;

/**
 * 获取手机窗口属性
 *
 * @author Zero
 */
public class UIUtils {

    /**
     * 获取屏幕尺寸
     *
     * @param context 上下文
     * @return 整形数组[宽度，高度]
     */
    public static int[] getWindowSize(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int width = dm.widthPixels;
        int height = dm.heightPixels;

        return new int[]{width, height};
    }

    /**
     * 获取状态栏高度
     *
     * @param context 上下文
     * @return 状态栏高度
     */
    public static int getStatusBarHeight(Context context) {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0;
        int statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusBarHeight;
    }

    /**
     * 获取底部导航栏尺寸
     *
     * @param activity 上下文
     * @return 导航栏高度 Point （宽：point.x, 高：point.y）
     */
    public static Point getNavigationBarSize(Activity activity) {
        Point appUsablePoint = getAppUsableScreenSize(activity);
        Point realScreenPoint = getRealScreenSize(activity);

        // navigation bar on the right
        if (appUsablePoint.x < realScreenPoint.x) {
            return new Point(realScreenPoint.x - appUsablePoint.x, appUsablePoint.y);
        }

        // navigation bar at the bottom
        if (appUsablePoint.y < realScreenPoint.y) {
            return new Point(appUsablePoint.x, realScreenPoint.y - appUsablePoint.y);
        }

        // navigation bar is not present
        return new Point();
    }

    /**
     * 获取用户正在使用的屏幕尺寸
     *
     * @param activity activity
     * @return 屏幕使用尺寸 Point （宽：point.x, 高：point.y）
     */
    public static Point getAppUsableScreenSize(Activity activity) {
        WindowManager windowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        return point;
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

    /**
     * 隐藏状态栏/导航栏
     *
     * @param activity 上下文
     * @param applyNav 是否隐藏导航栏
     */
    public static void hideBar(Activity activity, boolean applyNav) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            View decorView = activity.getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            if (applyNav) {
                option = option | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            }
            decorView.setSystemUiVisibility(option);
        }
    }

    public static int dp2px(Context context, float dp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static int sp2px(Context context, float sp) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (sp * fontScale + 0.5f);
    }

    public static int px2dp(Context context, float px) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    public static int px2sp(Context context, float px) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (px / fontScale + 0.5f);
    }

    /**
     * 设置背景透明度
     *
     * @param context 上下文
     * @param alpha   透明度, 1.0为完全不透明，0.0为完全透明
     */
    public static void setBackgroundAlpha(Activity context, float alpha) {
        WindowManager.LayoutParams lp = context.getWindow().getAttributes();
        lp.alpha = alpha;
        context.getWindow().setAttributes(lp);
    }

    /**
     * 获得软键盘高度
     *
     * @param view 当前view层
     * @return 软键盘的高度，int值
     */
    public static int getSoftInputHeight(View view) {
        Rect r = new Rect();
        view.getWindowVisibleDisplayFrame(r);
        int screenHeight = view.getRootView().getHeight();
        return screenHeight - (r.bottom - r.top);
    }

    /**
     * 显示软键盘
     *
     * @param context 上下文
     * @param view    EditText
     */
    public static void showSoftInput(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.
                getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm.isActive()) {
            imm.showSoftInput(view, 0);
        }
    }

    /**
     * 关闭软键盘
     *
     * @param context 上下文
     * @param view    EditText
     */
    public static void closeSoftInput(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.
                getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * 切换软键盘开关状态
     *
     * @param context 上下文
     */
    public static void toggleSoftInput(Context context) {
        InputMethodManager imm = (InputMethodManager) context.
                getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm.isActive()) {
            imm.toggleSoftInput(0, 0);
        }
    }

    /**
     * 改变侧拉栏分割线高度、颜色
     * @param navigationView 侧拉栏的navigationView
     * @param color 颜色
     * @param height 高度
     */
    public static void setNavigationMenuLineStyle(NavigationView navigationView, @ColorInt final int color, final int height) {
        try {
            Field fieldByPresenter = navigationView.getClass().getDeclaredField("mPresenter");
            fieldByPresenter.setAccessible(true);
            NavigationMenuPresenter menuPresenter = (NavigationMenuPresenter) fieldByPresenter.get(navigationView);
            Field fieldByMenuView = menuPresenter.getClass().getDeclaredField("mMenuView");
            fieldByMenuView.setAccessible(true);
            final NavigationMenuView menuView = (NavigationMenuView) fieldByMenuView.get(menuPresenter);
            menuView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
                @Override
                public void onChildViewAttachedToWindow(View view) {
                    RecyclerView.ViewHolder viewHolder = menuView.getChildViewHolder(view);
                    if (viewHolder != null && "SeparatorViewHolder".equals(viewHolder.getClass().getSimpleName()) && viewHolder.itemView != null) {
                        if (viewHolder.itemView instanceof FrameLayout) {
                            FrameLayout frameLayout = (FrameLayout) viewHolder.itemView;
                            View line = frameLayout.getChildAt(0);
                            line.setBackgroundColor(color);
                            line.getLayoutParams().height = height;
                        }
                    }
                }

                @Override
                public void onChildViewDetachedFromWindow(View view) {
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
