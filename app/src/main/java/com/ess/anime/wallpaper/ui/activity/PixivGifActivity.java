package com.ess.anime.wallpaper.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import com.ess.anime.wallpaper.MyApp;
import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.adapter.RecyclerPixivGifDlAdapter;
import com.ess.anime.wallpaper.model.helper.PermissionHelper;
import com.ess.anime.wallpaper.pixiv.gif.PixivGifBean;
import com.ess.anime.wallpaper.pixiv.gif.PixivGifDlManager;
import com.ess.anime.wallpaper.ui.view.GridDividerItemDecoration;
import com.ess.anime.wallpaper.utils.UIUtils;
import com.yanzhenjie.permission.runtime.Permission;

import java.util.Collections;
import java.util.List;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.OnClick;
import nl.bravobit.ffmpeg.FFmpeg;

public class PixivGifActivity extends BaseActivity {

    public final static String TAG = PixivGifActivity.class.getName();

    @BindView(R.id.tool_bar)
    Toolbar mToolbar;
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
        initToolBarLayout();
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

    private void initToolBarLayout() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initWhenPermissionGranted() {
        /*if (!PixivLoginManager.getInstance().isLoggingIn()) {
            String account = "";
            String password = "";
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
        }*/

        initViews();
        initRecyclerPixivGif();
    }

    private void initViews() {
        mEtId.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                startDownload();
                return true;
            }
            return false;
        });
    }

    private void initRecyclerPixivGif() {
        List<PixivGifBean> downloadList = PixivGifDlManager.getInstance().getDownloadList();
        Collections.reverse(downloadList);
        mRvPixivGif.setLayoutManager(new LinearLayoutManager(this));
        new RecyclerPixivGifDlAdapter(downloadList).bindToRecyclerView(mRvPixivGif);

        int spaceHor = UIUtils.dp2px(this, 5);
        int spaceVer = UIUtils.dp2px(this, 10);
        mRvPixivGif.addItemDecoration(new GridDividerItemDecoration(
                1, GridDividerItemDecoration.VERTICAL, spaceHor, spaceVer, true));
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
//            PixivLoginManager.getInstance().cancelLogin();
            mRvPixivGif.setAdapter(null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        PixivLoginManager.getInstance().cancelLogin();
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
