package com.ess.anime.wallpaper.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.model.helper.PermissionHelper;
import com.just.agentweb.AgentWeb;

public abstract class BaseWebActivity extends AppCompatActivity {

    AgentWeb mAgentWeb;

    abstract int titleRes();

    abstract String webUrl();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        initToolBarLayout();
        PermissionHelper.checkStoragePermissions(this, new PermissionHelper.RequestListener() {
            @Override
            public void onGranted() {
                initWebView();
            }

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
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    void initWebView() {
        mAgentWeb = AgentWeb.with(this)
                .setAgentWebParent((ViewGroup) findViewById(R.id.layout_web_view),
                        new LinearLayout.LayoutParams(-1, -1))
                .useDefaultIndicator(getResources().getColor(R.color.color_text_selected))
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

        mAgentWeb.getWebCreator().getWebView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // TODO sauceNAO长按下载图片
                WebView.HitTestResult result = ((WebView) v).getHitTestResult();
                int type = result.getType();
                if (type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE
                        || type == WebView.HitTestResult.IMAGE_TYPE) {
                    String imgUrl = result.getExtra();
                    Log.i("rrr", "" + type + "   " + imgUrl);
                }
                return true;
            }
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
}
