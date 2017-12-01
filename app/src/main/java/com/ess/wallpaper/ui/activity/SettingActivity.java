package com.ess.wallpaper.ui.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.ess.wallpaper.view.CustomDialog;
import com.ess.wallpaper.R;
import com.ess.wallpaper.global.Constants;
import com.ess.wallpaper.other.Sound;
import com.mixiaoxiao.smoothcompoundbutton.SmoothCheckBox;
import com.mixiaoxiao.smoothcompoundbutton.SmoothCompoundButton;

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
                if (b){
                    Sound.getInstance().playSoundEnabled(SettingActivity.this);
                }else {
                    Sound.getInstance().playSoundDisabled();
                }
            }
        });
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
        }
    }
}
