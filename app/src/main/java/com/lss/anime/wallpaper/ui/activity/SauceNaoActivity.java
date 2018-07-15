package com.lss.anime.wallpaper.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.lss.anime.wallpaper.R;
import com.lss.anime.wallpaper.global.Constants;
import com.lss.anime.wallpaper.helper.PermissionHelper;
import com.just.agentweb.AgentWeb;

public class SauceNaoActivity extends AppCompatActivity {

    private AgentWeb mAgentWeb;
    private PermissionHelper mPermissionUtil;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sauce_nao);

        initToolBarLayout();
        checkStoragePermission();
    }

    private void checkStoragePermission() {
        if (mPermissionUtil == null) {
            mPermissionUtil = new PermissionHelper(this, new PermissionHelper.OnPermissionListener() {
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
        mPermissionUtil.checkStoragePermission();
    }

    private void initToolBarLayout() {
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initWebView() {
        mAgentWeb = AgentWeb.with(this)
                .setAgentWebParent((ViewGroup) findViewById(R.id.layout_web_view),
                        new LinearLayout.LayoutParams(-1, -1))
                .useDefaultIndicator(getResources().getColor(R.color.color_text_selected))
                .setMainFrameErrorView(View.inflate(this, R.layout.layout_webview_error, null))
                .createAgentWeb()
                .ready()
                .go("http://saucenao.com/");
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
        // 检查权限回调
        if (requestCode == Constants.STORAGE_PERMISSION_CODE) {
            mPermissionUtil.checkStoragePermission();
        }
    }
}
