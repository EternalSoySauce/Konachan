package com.ess.anime.wallpaper.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.http.FireBase;
import com.ess.anime.wallpaper.model.helper.SoundHelper;
import com.ess.anime.wallpaper.utils.UIUtils;
import com.ess.anime.wallpaper.model.manager.WebsiteManager;

public class SplashActivity extends BaseActivity {

    @Override
    int layoutRes() {
        return R.layout.activity_splash;
    }

    @Override
    void init(Bundle savedInstanceState) {
        long delayMills = Constants.sRestart && Constants.sAllowPlaySound ? 3000 : 1500;
        SoundHelper.getInstance().playSplashWelcomeSound(this);

        FireBase.getInstance().checkToAddUser();
        FireBase.getInstance().checkUpdate();

        WebsiteManager.getInstance().updateCurrentTagJson();

        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }, delayMills);

        Constants.sRestart = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        UIUtils.hideNavigationBar(this);
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
