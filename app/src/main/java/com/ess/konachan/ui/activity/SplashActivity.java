package com.ess.konachan.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.ess.konachan.R;
import com.ess.konachan.global.Constants;
import com.ess.konachan.http.OkHttp;
import com.ess.konachan.utils.FileUtils;

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

        getTagJson(Constants.SAFE_MODE_TAG_JSON_URL);
        getTagJson(Constants.R18_MODE_TAG_JSON_URL);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        }, 1500);
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
                String html = response.body().string();
                if (html.startsWith("{\"version\"")) {
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
}
