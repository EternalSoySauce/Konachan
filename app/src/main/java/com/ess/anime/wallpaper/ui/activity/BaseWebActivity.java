package com.ess.anime.wallpaper.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.model.helper.PermissionHelper;
import com.just.agentweb.AgentWeb;
import com.yanzhenjie.permission.runtime.Permission;

import butterknife.OnClick;

public abstract class BaseWebActivity extends BaseActivity {

    AgentWeb mAgentWeb;

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
                Log.i("rrr", "" + type + "   " + imgUrl);
            }
            return true;
        });
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
