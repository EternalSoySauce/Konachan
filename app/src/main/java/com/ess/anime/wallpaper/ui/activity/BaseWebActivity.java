package com.ess.anime.wallpaper.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.adapter.RecyclerWebviewMoreAdapter;
import com.ess.anime.wallpaper.glide.GlideApp;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.model.helper.PermissionHelper;
import com.ess.anime.wallpaper.utils.ComponentUtils;
import com.ess.anime.wallpaper.utils.FileUtils;
import com.ess.anime.wallpaper.utils.UIUtils;
import com.jiang.android.indicatordialog.IndicatorBuilder;
import com.jiang.android.indicatordialog.IndicatorDialog;
import com.just.agentweb.AgentWeb;
import com.yanzhenjie.permission.runtime.Permission;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import butterknife.OnClick;

public abstract class BaseWebActivity extends BaseActivity {

    AgentWeb mAgentWeb;
    private IndicatorDialog mPopup;

    abstract int titleRes();

    abstract String webUrl();

    @OnClick(R.id.iv_help)
    abstract void showHelpDialog();

    @Override
    int layoutRes() {
        return R.layout.activity_web;
    }

    @Override
    void init(Bundle savedInstanceState) {
        initToolBarLayout();
        initWebView();
        initListPopupWindow();
        PermissionHelper.checkStoragePermissions(this, new PermissionHelper.SimpleRequestListener() {
            @Override
            public void onDenied() {
                finish();
            }
        });
    }

    private void initToolBarLayout() {
        Toolbar toolbar = findViewById(R.id.tool_bar);
        toolbar.setTitle(titleRes());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    void initWebView() {
        mAgentWeb = AgentWeb.with(this)
                .setAgentWebParent(findViewById(R.id.layout_web_view),
                        new FrameLayout.LayoutParams(-1, -1))
                .useDefaultIndicator(ResourcesCompat.getColor(getResources(), R.color.color_text_selected, null))
                .setMainFrameErrorView(View.inflate(this, R.layout.layout_webview_error, null))
                .interceptUnkownUrl()
                .createAgentWeb()
                .ready()
                .go(webUrl());

        WebSettings settings = mAgentWeb.getWebCreator().getWebView().getSettings();
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);

        mAgentWeb.getWebCreator().getWebView().setOnLongClickListener(v -> {
            // TODO sauceNAO长按下载图片
            WebView.HitTestResult result = ((WebView) v).getHitTestResult();
            int type = result.getType();
            if (type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE
                    || type == WebView.HitTestResult.IMAGE_TYPE) {
                String imgUrl = result.getExtra();
                if (imgUrl == null) {
                    return true;
                }

                if (imgUrl.startsWith("http")) {
                    try {
                        URL url = new URL(imgUrl);
                        String fileName = url.getFile();
                        int index = fileName.lastIndexOf("/");
                        if (index != -1) {
                            fileName = fileName.substring(index + 1);
                        }
                        Log.i("rrr", "" + type + "   " + imgUrl + "   " + fileName);
                        String finalFileName = fileName;
                        GlideApp.with(getApplicationContext())
                                .asFile()
                                .load(imgUrl)
                                .listener(new RequestListener<File>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<File> target, boolean isFirstResource) {
                                        Toast.makeText(BaseWebActivity.this, "下载失败", Toast.LENGTH_SHORT).show();
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(File resource, Object model, Target<File> target, DataSource dataSource, boolean isFirstResource) {
                                        Log.i("rrr","fileName "+resource.getName());
                                        FileUtils.copyFile(resource, new File(Constants.IMAGE_DIR, finalFileName));
                                        Toast.makeText(BaseWebActivity.this, "下载成功", Toast.LENGTH_SHORT).show();
                                        return false;
                                    }
                                }).submit();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "下载失败", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            return true;
        });
    }

    private void initListPopupWindow() {
        // 选择搜索模式弹窗
        List<String> searchModeList = Arrays.asList(getResources().getStringArray(R.array.spinner_list_item_webview_more));
        RecyclerWebviewMoreAdapter adapter = new RecyclerWebviewMoreAdapter(searchModeList);
        adapter.setOnItemClickListener((adapter1, view, position) -> {
            String url = mAgentWeb.getWebCreator().getWebView().getUrl();
            switch (position) {
                case 0: // 复制链接
                    ComponentUtils.setClipString(this, url);
                    Toast.makeText(this, R.string.copied_link, Toast.LENGTH_SHORT).show();
                    break;

                case 1: // 分享链接
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_TEXT, url);
                    shareIntent.setType("text/plain");
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.share_title)));
                    break;

                case 2: // 用浏览器打开
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(Intent.createChooser(intent, getString(R.string.share_title)));
                    break;
            }
            mPopup.dismiss();
        });

        mPopup = new IndicatorBuilder(this)
                .width(computePopupItemMaxWidth(adapter))
                .height(-1)
                .bgColor(getResources().getColor(R.color.colorPrimary))
                .dimEnabled(false)
                .gravity(IndicatorBuilder.GRAVITY_RIGHT)
                .ArrowDirection(IndicatorBuilder.TOP)
                .ArrowRectage(0.86f)
                .radius(8)
                .layoutManager(new LinearLayoutManager(this))
                .adapter(adapter)
                .create();
        mPopup.setCanceledOnTouchOutside(true);
        mPopup.getDialog().setOnShowListener(dialog -> UIUtils.setBackgroundAlpha(this, 0.4f));
        mPopup.getDialog().setOnDismissListener(dialog -> UIUtils.setBackgroundAlpha(this, 1f));
    }

    // 使弹窗自适应文字宽度
    private int computePopupItemMaxWidth(RecyclerWebviewMoreAdapter adapter) {
        float maxWidth = 0;
        View layout = View.inflate(this, R.layout.recyclerview_item_popup_webview_more, null);
        TextView tv = layout.findViewById(R.id.tv_function);
        TextPaint paint = tv.getPaint();
        for (int i = 0; i < adapter.getItemCount(); i++) {
            String item = adapter.getItem(i);
            maxWidth = Math.max(maxWidth, paint.measureText(item));
        }
        maxWidth += tv.getPaddingStart() + tv.getPaddingEnd();
        return (int) maxWidth;
    }

    @OnClick(R.id.iv_more)
    void showMore(View view) {
        mPopup.show(view);
    }

    @Override
    protected void onPause() {
        if (mAgentWeb != null) {
            mAgentWeb.getWebLifeCycle().onPause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (mAgentWeb != null) {
            mAgentWeb.getWebLifeCycle().onResume();
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (mAgentWeb != null) {
            mAgentWeb.getWebLifeCycle().onDestroy();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mAgentWeb == null || !mAgentWeb.back()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PermissionHelper.REQ_CODE_PERMISSION) {
            // 进入系统设置界面请求权限后的回调
            if (!PermissionHelper.hasPermissions(this, Permission.Group.STORAGE)) {
                finish();
            }
        }
    }

}
