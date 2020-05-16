package com.ess.anime.wallpaper.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.ess.anime.wallpaper.MyApp;
import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.adapter.RecyclerPixivGifDlAdapter;
import com.ess.anime.wallpaper.model.helper.PermissionHelper;
import com.ess.anime.wallpaper.pixiv.gif.PixivGifBean;
import com.ess.anime.wallpaper.pixiv.gif.PixivGifDlManager;
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
            mRvPixivGif.setAdapter(null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
