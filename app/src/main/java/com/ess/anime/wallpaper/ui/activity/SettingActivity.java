package com.ess.anime.wallpaper.ui.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.bean.ApkBean;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.http.FireBase;
import com.ess.anime.wallpaper.model.helper.SoundHelper;
import com.ess.anime.wallpaper.ui.view.CustomDialog;
import com.ess.anime.wallpaper.utils.ComponentUtils;
import com.ess.anime.wallpaper.utils.FileUtils;
import com.mixiaoxiao.smoothcompoundbutton.SmoothCheckBox;

import java.io.File;

import androidx.appcompat.widget.Toolbar;
import butterknife.BindView;
import butterknife.OnClick;

public class SettingActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.tool_bar)
    Toolbar mToolbar;
    @BindView(R.id.cb_setting_allow_play_sound)
    SmoothCheckBox mCbAllowPlaySound;
    @BindView(R.id.tv_current_version)
    TextView mTvCurrentVersion;

    private SharedPreferences mPreferences;

    @Override
    int layoutRes() {
        return R.layout.activity_setting;
    }

    @Override
    void init(Bundle savedInstanceState) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        initToolBarLayout();
        initViews();
    }

    private void initToolBarLayout() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViews() {
        mCbAllowPlaySound.setChecked(Constants.sAllowPlaySound, false, false);
        mCbAllowPlaySound.setOnCheckedChangeListener((smoothCompoundButton, isChecked) -> {

        });

        String version = ComponentUtils.getVersionName(this);
        version = getString(R.string.setting_current_version, version);
        mTvCurrentVersion.setText(version);
    }

    @OnClick({R.id.layout_setting_allow_play_sound})
    void togglePlaySound() {
        mCbAllowPlaySound.toggle();
        boolean isChecked = mCbAllowPlaySound.isChecked();
        Constants.sAllowPlaySound = isChecked;
        mPreferences.edit().putBoolean(Constants.ALLOW_PLAY_SOUND, isChecked).apply();
        if (isChecked) {
            SoundHelper.getInstance().playSoundEnabled(SettingActivity.this);
        } else {
            SoundHelper.getInstance().playSoundDisabled();
        }
    }

    @OnClick({R.id.tv_help_tag_type, R.id.tv_help_advanced_search})
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_help_tag_type:
                CustomDialog.showTagTypeHelpDialog(SettingActivity.this);
                break;
            case R.id.tv_help_advanced_search:
                CustomDialog.showAdvancedSearchHelpDialog(SettingActivity.this);
                break;
        }
    }

    @OnClick(R.id.layout_check_update)
    void checkUpdate() {
        File file = new File(getExternalFilesDir(null), FireBase.UPDATE_FILE_NAME);
        if (file.exists()) {
            String json = FileUtils.fileToString(file);
            ApkBean apkBean = ApkBean.getApkDetailFromJson(this, json);
            if (apkBean.versionCode > ComponentUtils.getVersionCode(this)) {
                CustomDialog.showUpdateDialog(this, apkBean);
            } else {
                showNoNewVersionToast();
            }
        } else {
            showNoNewVersionToast();
        }
    }

    private void showNoNewVersionToast() {
        Toast.makeText(this, R.string.check_no_new_version, Toast.LENGTH_SHORT).show();
    }
}
