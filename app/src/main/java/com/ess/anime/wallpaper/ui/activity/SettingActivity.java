package com.ess.anime.wallpaper.ui.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.bean.ApkBean;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.http.FireBase;
import com.ess.anime.wallpaper.other.Sound;
import com.ess.anime.wallpaper.utils.ComponentUtils;
import com.ess.anime.wallpaper.utils.FileUtils;
import com.ess.anime.wallpaper.view.CustomDialog;
import com.mixiaoxiao.smoothcompoundbutton.SmoothCheckBox;
import com.mixiaoxiao.smoothcompoundbutton.SmoothCompoundButton;

import java.io.File;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {

    private SharedPreferences mPreferences;

    private SmoothCheckBox mCbAllowPlaySound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        initToolBarLayout();
        initViews();
    }

    private void initToolBarLayout() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
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

    private void initViews() {
        mCbAllowPlaySound = (SmoothCheckBox) findViewById(R.id.cb_setting_allow_play_sound);
        mCbAllowPlaySound.setChecked(Constants.sAllowPlaySound, false, false);
        mCbAllowPlaySound.setOnCheckedChangeListener(new SmoothCompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SmoothCompoundButton smoothCompoundButton, boolean b) {
                Constants.sAllowPlaySound = b;
                mPreferences.edit().putBoolean(Constants.ALLOW_PLAY_SOUND, b).apply();
                if (b) {
                    Sound.getInstance().playSoundEnabled(SettingActivity.this);
                } else {
                    Sound.getInstance().playSoundDisabled();
                }
            }
        });

        TextView tvCurrentVersion = (TextView) findViewById(R.id.tv_current_version);
        tvCurrentVersion.setText(getString(R.string.setting_current_version, ComponentUtils.getVersionName(this)));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_setting_allow_play_sound:
                mCbAllowPlaySound.performClick();
                break;
            case R.id.tv_help_tag_type:
                CustomDialog.showTagTypeHelpDialog(SettingActivity.this);
                break;
            case R.id.tv_help_advanced_search:
                CustomDialog.showAdvancedSearchHelpDialog(SettingActivity.this);
                break;
            case R.id.layout_check_update:
                checkUpdate();
                break;
        }
    }

    private void checkUpdate() {
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
