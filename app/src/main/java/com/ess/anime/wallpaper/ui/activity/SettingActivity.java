package com.ess.anime.wallpaper.ui.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.adapter.RecyclerCommonSettingAdapter;
import com.ess.anime.wallpaper.download.apk.ApkBean;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.http.FireBase;
import com.ess.anime.wallpaper.model.entity.CommonSettingItem;
import com.ess.anime.wallpaper.model.helper.SoundHelper;
import com.ess.anime.wallpaper.ui.view.CustomDialog;
import com.ess.anime.wallpaper.utils.FileUtils;
import com.ess.anime.wallpaper.utils.SystemUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;

public class SettingActivity extends BaseActivity {

    @BindView(R.id.tool_bar)
    Toolbar mToolbar;
    @BindView(R.id.rv_setting)
    RecyclerView mRvSetting;

    private SharedPreferences mPreferences;
    private RecyclerCommonSettingAdapter mSettingAdapter;

    private long mCacheSize;
    private CommonSettingItem mClearCacheItem;
    private ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected int layoutRes() {
        return R.layout.activity_setting;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        initToolBarLayout();
        initRecyclerSetting();
        resetData();
    }

    private void initToolBarLayout() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initRecyclerSetting() {
        mRvSetting.setLayoutManager(new LinearLayoutManager(this));
        mSettingAdapter = new RecyclerCommonSettingAdapter();
        mSettingAdapter.bindToRecyclerView(mRvSetting);
    }

    private void resetData() {
        mSettingAdapter.setNewData(getSettingItems());
        getCacheSize();
    }

    private void updateUI() {
        if (SystemUtils.isActivityActive(this)) {
            hideLoadingSafely();
            if (mClearCacheItem != null) {
                mClearCacheItem.setTips(FileUtils.computeFileSize(mCacheSize));
            }
            mSettingAdapter.notifyDataSetChanged();
        }
    }

    private List<CommonSettingItem> getSettingItems() {
        List<CommonSettingItem> items = new ArrayList<>();
        items.add(getAllowPlaySoundItem());
        items.add(getPreloadImageOnlyWifiItem());
        items.add(getHelpTagTypeItem());
        items.add(getHelpAdvancedSearchItem());
        items.add(mClearCacheItem = getClearCacheItem());
        items.add(getCheckUpdateItem());
        return items;
    }

    private CommonSettingItem getAllowPlaySoundItem() {
        return new CommonSettingItem()
                .setTitle(R.string.setting_allow_play_sound)
                .setRippleDelayClick(false)
                .setCheckboxShown(true)
                .setCheckboxChecked(Constants.sAllowPlaySound)
                .setOnCheckedChangeListener((smoothCompoundButton, isChecked) -> {
                    Constants.sAllowPlaySound = isChecked;
                    mPreferences.edit().putBoolean(Constants.ALLOW_PLAY_SOUND, isChecked).apply();
                    if (isChecked) {
                        SoundHelper.getInstance().playSoundEnabled(SettingActivity.this);
                    } else {
                        SoundHelper.getInstance().playSoundDisabled();
                    }
                });
    }

    private CommonSettingItem getPreloadImageOnlyWifiItem() {
        boolean preloadOnlyWifi = mPreferences.getBoolean(Constants.PRELOAD_IMAGE_ONLY_WIFI, false);
        return new CommonSettingItem()
                .setTitle(R.string.setting_preload_image_only_wifi_title)
                .setDesc(R.string.setting_preload_image_only_wifi_desc)
                .setCheckboxShown(true)
                .setCheckboxChecked(preloadOnlyWifi)
                .setOnCheckedChangeListener((smoothCompoundButton, isChecked) -> {
                    mPreferences.edit().putBoolean(Constants.PRELOAD_IMAGE_ONLY_WIFI, isChecked).apply();
                });
    }

    private CommonSettingItem getHelpTagTypeItem() {
        return new CommonSettingItem()
                .setTitle(R.string.setting_help_tag_type)
                .setOnClickListener(v -> {
                    CustomDialog.showTagTypeHelpDialog(this);
                });
    }

    private CommonSettingItem getHelpAdvancedSearchItem() {
        return new CommonSettingItem()
                .setTitle(R.string.setting_help_advanced_search)
                .setOnClickListener(v -> {
                    CustomDialog.showAdvancedSearchHelpDialog(this);
                });
    }

    private CommonSettingItem getClearCacheItem() {
        return new CommonSettingItem()
                .setTitle(R.string.setting_clear_cache_title)
                .setDesc(R.string.setting_clear_cache_desc)
                .setOnClickListener(v -> {
                    if (SystemUtils.isActivityActive(this)) {
                        showLoadingSafely();
                        deleteCacheFile();
                    }
                });
    }

    private void deleteCacheFile() {
        if (!mExecutor.isTerminated() && !mExecutor.isShutdown()) {
            mExecutor.execute(() -> {
                for (File dir : getCacheDirs()) {
                    FileUtils.deleteFile(dir);
                }
                getCacheSize();
            });
        }
    }

    private void getCacheSize() {
        if (!mExecutor.isTerminated() && !mExecutor.isShutdown()) {
            mExecutor.execute(() -> {
                File[] cacheDirs = getCacheDirs();
                long cacheSize = 0;
                for (File dir : cacheDirs) {
                    cacheSize += FileUtils.getFileLength(dir);
                }
                long finalCacheSize = cacheSize;
                runOnUiThread(() -> {
                    mCacheSize = finalCacheSize;
                    updateUI();
                });
            });
        }
    }

    private File[] getCacheDirs() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            return new File[]{getCacheDir(), getCodeCacheDir(), getExternalCacheDir()};
        } else {
            return new File[]{getCacheDir(), getExternalCacheDir()};
        }
    }

    private CommonSettingItem getCheckUpdateItem() {
        String version = SystemUtils.getVersionName(this);
        return new CommonSettingItem()
                .setTitle(R.string.setting_check_update)
                .setDesc(getString(R.string.setting_current_version, version))
                .setOnClickListener(v -> {
                    File file = new File(getExternalFilesDir(null), FireBase.UPDATE_FILE_NAME);
                    if (file.exists()) {
                        String json = FileUtils.fileToString(file);
                        ApkBean apkBean = ApkBean.getApkDetailFromJson(this, json);
                        if (apkBean.versionCode > SystemUtils.getVersionCode(this)) {
                            CustomDialog.showUpdateDialog(this, apkBean);
                        } else {
                            showNoNewVersionToast();
                        }
                    } else {
                        showNoNewVersionToast();
                    }
                });
    }

    private void showNoNewVersionToast() {
        Toast.makeText(this, R.string.check_no_new_version, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        mExecutor.shutdownNow();
        super.onDestroy();
    }
}
