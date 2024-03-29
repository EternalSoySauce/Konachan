package com.ess.anime.wallpaper.utils;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import com.google.android.material.internal.NavigationMenuPresenter;
import com.google.android.material.internal.NavigationMenuView;
import com.google.android.material.navigation.NavigationView;

import java.lang.reflect.Field;

import androidx.annotation.ColorInt;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 获取手机窗口属性
 *
 * @author Zero
 */
public class UIUtils {

    /**
     * 获取屏幕宽度
     *
     * @param context 上下文
     * @return 屏幕宽度
     */
    public static int getScreenWidth(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    /**
     * 获取屏幕高度
     *
     * @param context 上下文
     * @return 屏幕高度
     */
    public static int getScreenHeight(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }

    /**
     * 获取状态栏高度
     *
     * @param context 上下文
     * @return 状态栏高度
     */
    public static int getStatusBarHeight(Context context) {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        return context.getResources().getDimensionPixelOffset(resourceId);
    }

    /**
     * 获取ActionBar高度
     *
     * @param context 上下文
     * @return ActionBar高度
     */
    public static int getActionBarHeight(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true);
        return TypedValue.complexToDimensionPixelSize(typedValue.data, context.getResources().getDisplayMetrics());
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
     * @param context context
     * @return 屏幕使用尺寸 Point （宽：point.x, 高：point.y）
     */
    public static Point getAppUsableScreenSize(Context context) {
        // 使用application才能真正获得正在使用部分的尺寸
        context = context.getApplicationContext();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
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
     * @param activity 上下文
     * @param alpha    透明度, 1.0为完全不透明，0.0为完全透明
     */
    public static void setBackgroundAlpha(Activity activity, float alpha) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = alpha;
        activity.getWindow().setAttributes(lp);
    }

    /**
     * 隐藏状态栏
     * 在onCreate和onWindowFocusChanged中都要调用
     *
     * @param activity 上下文
     */
    public static void hideStatusBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            View decorView = activity.getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(option);
        }
    }

    /**
     * 隐藏虚拟键
     * 在onCreate和onWindowFocusChanged中都要调用
     *
     * @param activity 上下文
     */
    public static void hideNavigationBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            View decorView = activity.getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(option);
        }
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
     * @param activity 上下文
     */
    public static void closeSoftInput(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.
                getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm.isActive() && activity.getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
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
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * 避免输入法导致内存泄露的问题，在onDestroy()中使用
     *
     * @param destContext 上下文
     */
    public static void fixInputMethodManagerLeak(Context destContext) {
        if (destContext == null) {
            return;
        }

        InputMethodManager imm = (InputMethodManager) destContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return;
        }

        String[] params = new String[]{"mCurRootView", "mServedView", "mNextServedView"};
        Field f = null;
        Object obj_get = null;
        for (String param : params) {
            try {
                f = imm.getClass().getDeclaredField(param);
                if (!f.isAccessible()) {
                    f.setAccessible(true);
                }
                obj_get = f.get(imm);
                if (obj_get != null && obj_get instanceof View) {
                    View v_get = (View) obj_get;
                    if (v_get.getContext() == destContext) { // 被InputMethodManager持有引用的context是想要目标销毁的
                        f.set(imm, null); // 置空，破坏掉path to gc节点
                    } else {
                        // 不是想要目标销毁的，即为又进了另一层界面了，不要处理，避免影响原逻辑,也就不用继续for循环了
                        break;
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    /**
     * 改变侧拉栏分割线高度、颜色
     *
     * @param navigationView 侧拉栏的navigationView
     * @param color          颜色
     * @param height         高度
     */
    public static void setNavigationMenuLineStyle(NavigationView navigationView, @ColorInt final int color, final int height) {
        try {
            Field fieldByPresenter = navigationView.getClass().getDeclaredField("presenter");
            fieldByPresenter.setAccessible(true);
            NavigationMenuPresenter menuPresenter = (NavigationMenuPresenter) fieldByPresenter.get(navigationView);
            Field fieldByMenuView = menuPresenter.getClass().getDeclaredField("menuView");
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

    /**
     * 获得View的Activity，兼容4.x机器
     *
     * @param view View
     * @return Activity
     */
    public static Activity getActivityFromView(View view) {
        Context context = view.getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    public static int[] getLocationInWindow(View view) {
        int[] location = new int[2];
        view.getLocationInWindow(location);
        return location;
    }

    public static boolean isLandscape(Activity activity) {
        return activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    public static boolean isLandscapeReverse(Activity activity) {
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        boolean landReverse = rotation == Surface.ROTATION_270;
        return isLandscape(activity) && landReverse;
    }

    public static boolean isPortrait(Activity activity) {
        return activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    public static boolean isPortraitReverse(Activity activity) {
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        boolean portraitReverse = rotation == Surface.ROTATION_180;
        return isPortrait(activity) && portraitReverse;
    }
}
