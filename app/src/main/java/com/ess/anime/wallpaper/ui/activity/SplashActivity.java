package com.ess.anime.wallpaper.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.http.FireBase;
import com.ess.anime.wallpaper.http.OkHttp;
import com.ess.anime.wallpaper.model.helper.SoundHelper;
import com.ess.anime.wallpaper.utils.FileUtils;
import com.ess.anime.wallpaper.utils.UIUtils;

import java.io.File;
import java.io.IOException;

import androidx.annotation.NonNull;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

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

        for (String url : Constants.TAG_JSON_URLS) {
            getTagJson(url);
        }

        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }, delayMills);

        Constants.sRestart = false;
    }

    // 获取存储着K站所有tag的Json，用于搜索提示
    private void getTagJson(final String url) {
        OkHttp.getInstance().connect(url, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (OkHttp.isNetworkProblem(e)) {
                    OkHttp.getInstance().connect(url, this);
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String html = response.body().string();
                    String path = getFilesDir().getPath();
                    String name = FileUtils.encodeMD5String(url);
                    File file = new File(path, name);
                    FileUtils.stringToFile(html, file);
                } else {
                    OkHttp.getInstance().connect(url, this);
                }
                response.close();
            }
        });
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
