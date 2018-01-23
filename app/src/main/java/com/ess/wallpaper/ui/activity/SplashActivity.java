package com.ess.wallpaper.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.ess.wallpaper.http.OkHttp;
import com.ess.wallpaper.R;
import com.ess.wallpaper.global.Constants;
import com.ess.wallpaper.other.Sound;
import com.ess.wallpaper.utils.FileUtils;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getWindow().setBackgroundDrawable(null);

        long delay = Constants.sRestart && Constants.sAllowPlaySound ? 3000 : 1500;
        Sound.getInstance().playSplashWelcomeSound(this);

        for (String url : Constants.TAG_JSON_URLS) {
            getTagJson(url);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        }, delay);
    }

    // 获取存储着K站所有tag的Json，用于搜索提示
    private void getTagJson(final String url) {
        OkHttp.getInstance().connect(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (OkHttp.isNetworkProblem(e)) {
                    OkHttp.getInstance().connect(url, this);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
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
    public void onBackPressed() {
    }
}
