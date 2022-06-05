package com.ess.anime.wallpaper.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.download.image.DownloadImageManager;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.http.FireBase;
import com.ess.anime.wallpaper.model.helper.ReverseSearchWebsiteDataHelper;
import com.ess.anime.wallpaper.model.helper.SoundHelper;
import com.ess.anime.wallpaper.utils.UIUtils;
import com.ess.anime.wallpaper.website.WebsiteManager;

public class SplashActivity extends BaseActivity {

    private Handler mHandler = new Handler();
    private boolean mIsForeground;
    private boolean mCanGotoNextPage;

    @Override
    protected int layoutRes() {
        return R.layout.activity_splash;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        long delayMills = Constants.sRestart && Constants.sAllowPlaySound ? 3000 : 1500;
        SoundHelper.getInstance().playSplashWelcomeSound(this);

        FireBase.getInstance().checkToAddUser();
        FireBase.getInstance().checkUpdate();
        ReverseSearchWebsiteDataHelper.loadNewJsonFromServer(this);

        WebsiteManager.getInstance().updateCurrentTagJson();
        DownloadImageManager.getInstance();

        mHandler.postDelayed(() -> {
            mCanGotoNextPage = true;
            if (mIsForeground) {
                gotoNextPage();
            }
        }, delayMills);

        Constants.sRestart = false;
    }

    private void gotoNextPage() {
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onResume() {
        super.onResume();
        UIUtils.hideNavigationBar(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mIsForeground = true;
        if (mCanGotoNextPage) {
            mHandler.post(this::gotoNextPage);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mIsForeground = false;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        UIUtils.hideNavigationBar(this);
    }

    @Override
    public void onBackPressed() {
    }
}
