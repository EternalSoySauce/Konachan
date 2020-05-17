package com.ess.anime.wallpaper.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.ess.anime.wallpaper.MyApp;
import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.adapter.RecyclerPixivGifDlAdapter;
import com.ess.anime.wallpaper.model.helper.PermissionHelper;
import com.ess.anime.wallpaper.pixiv.gif.PixivGifBean;
import com.ess.anime.wallpaper.pixiv.gif.PixivGifDlManager;
import com.ess.anime.wallpaper.pixiv.login.IPixivLoginCallback;
import com.ess.anime.wallpaper.pixiv.login.PixivLoginManager;
import com.yanzhenjie.permission.runtime.Permission;

import java.util.Collections;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.OnClick;
import nl.bravobit.ffmpeg.FFmpeg;

public class PixivGifActivity extends BaseActivity {

    public final static String TAG = PixivGifActivity.class.getName();

    @BindView(R.id.et_id)
    EditText mEtId;
    @BindView(R.id.rv_pixiv_gif)
    RecyclerView mRvPixivGif;

    @Override
    int layoutRes() {
        return R.layout.activity_pixiv_gif;
    }

    @Override
    void init(Bundle savedInstanceState) {
        if (!FFmpeg.getInstance(this).isSupported()) {
            // todo 翻译
            Toast.makeText(MyApp.getInstance(), "您的设备无法合成gif", Toast.LENGTH_SHORT).show();
            return;
        }

        PermissionHelper.checkStoragePermissions(this, new PermissionHelper.RequestListener() {
            @Override
            public void onGranted() {
                initWhenPermissionGranted();
            }

            @Override
            public void onDenied() {
                finish();
            }
        });
    }

    private void initWhenPermissionGranted() {
        if (!PixivLoginManager.getInstance().isLoggingIn()) {
            String account = "1018717197@qq.com";
            String password = "yu98674320";
            PixivLoginManager.getInstance().login(account, password, new IPixivLoginCallback() {
                @Override
                public void onLoginSuccess() {
                    Log.i("rrr", "onLoginSuccess");
                }

                @Override
                public void onLoginError() {
                    Log.i("rrr", "onLoginError");
                }

                @Override
                public void onConnectPixivFailed() {
                    Log.i("rrr", "onConnectPixivFailed");
                }
            });
        }else {
            Log.i("rrr", "isLoggingIn");
        }

        initRecyclerPixivGif();
    }

    private void initRecyclerPixivGif() {
        List<PixivGifBean> downloadList = PixivGifDlManager.getInstance().getDownloadList();
        Collections.reverse(downloadList);
        mRvPixivGif.setLayoutManager(new LinearLayoutManager(this));
        new RecyclerPixivGifDlAdapter(downloadList).bindToRecyclerView(mRvPixivGif);
    }

    @OnClick(R.id.btn_download)
    void startDownload() {
        String pixivId = mEtId.getText().toString();
        if (!TextUtils.isEmpty(pixivId)) {
            PixivGifDlManager.getInstance().execute(pixivId);
            mEtId.setText(null);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isFinishing()) {
            PixivLoginManager.getInstance().cancelLogin();
            mRvPixivGif.setAdapter(null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PixivLoginManager.getInstance().cancelLogin();
        mRvPixivGif.setAdapter(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PermissionHelper.REQ_CODE_PERMISSION) {
            // 进入系统设置界面请求权限后的回调
            if (PermissionHelper.hasPermissions(this, Permission.Group.STORAGE)) {
                initWhenPermissionGranted();
            } else {
                finish();
            }
        }
    }

}
