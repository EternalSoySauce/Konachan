package com.ess.anime.wallpaper.ui.view;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.glide.GlideApp;
import com.ess.anime.wallpaper.glide.MyGlideModule;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.ui.activity.CollectionActivity;
import com.ess.anime.wallpaper.utils.BitmapUtils;
import com.ess.anime.wallpaper.utils.FileUtils;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.zyyoona7.popup.EasyPopup;

import java.io.File;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;

import androidx.annotation.Nullable;

public class LongClickWebView extends WebView implements View.OnLongClickListener {

    private int mTouchX;
    private int mTouchY;
    private EasyPopup mPopupPage;
    private TextView mTvSave;

    public LongClickWebView(Context context) {
        super(context);
    }

    public LongClickWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LongClickWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        mTouchX = (int) event.getX();
        mTouchY = (int) event.getY() + QMUIStatusBarHelper.getStatusbarHeight(getContext());
        return super.onInterceptTouchEvent(event);
    }

    public void init() {
        WebSettings settings = getSettings();
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setGeolocationEnabled(false);

        initPopupPage();
        setOnLongClickListener(this);
    }

    private void initPopupPage() {
        mPopupPage = EasyPopup.create()
                .setContentView(getContext(), R.layout.layout_popup_webview_long_click)
                .setBackgroundDimEnable(true)
                .setDimValue(0.4f)
                .apply();

        mTvSave = mPopupPage.findViewById(R.id.tv_save);
    }

    @Override
    public boolean onLongClick(View v) {
        WebView.HitTestResult result = getHitTestResult();
        int type = result.getType();
        if (type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE
                || type == WebView.HitTestResult.IMAGE_TYPE) {
            String imgUrl = result.getExtra();
            if (!TextUtils.isEmpty(imgUrl)) {
                showPopup(imgUrl, getUrl());
                return true;
            }
        }
        return false;
    }

    private void showPopup(String imgUrl, String webUrl) {
        mPopupPage.showAtLocation(this, Gravity.START | Gravity.TOP, mTouchX, mTouchY);
        mTvSave.setOnClickListener(v -> {
            mPopupPage.dismiss();
            saveImage(imgUrl, webUrl);
        });
    }

    private void saveImage(String imgUrl, String webUrl) {
        Object objToLoad = null;
        String fileName = "";
        if (imgUrl.startsWith("http")) {
            // 普通http协议图片
            try {
                URL url = new URL(imgUrl);
                fileName = url.getPath();
                int index = fileName.lastIndexOf("/");
                if (index != -1) {
                    fileName = fileName.substring(index + 1);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            objToLoad = MyGlideModule.makeGlideUrlWithReferer(imgUrl, webUrl);
        } else if (imgUrl.startsWith("data:image/") && imgUrl.contains(";base64,")) {
            // base64图片
            // TODO 下载P站base64图片
            int index = imgUrl.indexOf(",");
            String base64 = imgUrl.substring(index + 1);
            objToLoad = Base64.decode(base64, Base64.DEFAULT);
        }

        final String[] finalFileName = {fileName};
        GlideApp.with(getContext().getApplicationContext())
                .asFile()
                .load(objToLoad)
                .priority(Priority.IMMEDIATE)
                .listener(new RequestListener<File>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<File> target, boolean isFirstResource) {
                        toastSaveFailed();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(File resource, Object model, Target<File> target, DataSource dataSource, boolean isFirstResource) {
                        if (!FileUtils.isMediaType(finalFileName[0])) {
                            finalFileName[0] = resource.getName() + ".jpg";
                        }
                        File file = new File(Constants.IMAGE_DIR, finalFileName[0]);
                        FileUtils.copyFile(resource, file);
                        BitmapUtils.insertToMediaStore(getContext(), file);
                        toastSaveSuccessfully();
                        return false;
                    }
                }).submit();
    }

    private void toastSaveSuccessfully() {
        post(() -> {
            Toast toast = new Toast(getContext());
            try {
                // 使Toast可接收点击事件
                Field field = toast.getClass().getDeclaredField("mTN");
                field.setAccessible(true);
                Object mTN = field.get(toast);
                field = mTN.getClass().getDeclaredField("mParams");
                field.setAccessible(true);
                WindowManager.LayoutParams mParams = (WindowManager.LayoutParams) field.get(mTN);
                mParams.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            } catch (Exception e) {
                e.printStackTrace();
            }

            View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_toast_save_image_successfully, null);
            TextView tvLink = view.findViewById(R.id.tv_link);
            tvLink.setMovementMethod(LinkMovementMethod.getInstance());
            tvLink.setOnClickListener(v -> {
                toast.cancel();
                getContext().startActivity(new Intent(getContext(), CollectionActivity.class));
            });

            toast.setView(view);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.show();
        });
    }

    private void toastSaveFailed() {
        post(() -> Toast.makeText(getContext(), R.string.save_failed, Toast.LENGTH_SHORT).show());
    }

}
