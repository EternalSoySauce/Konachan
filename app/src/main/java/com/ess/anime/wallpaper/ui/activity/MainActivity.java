package com.ess.anime.wallpaper.ui.activity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.download.apk.ApkBean;
import com.ess.anime.wallpaper.bean.MsgBean;
import com.ess.anime.wallpaper.glide.GlideApp;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.http.FireBase;
import com.ess.anime.wallpaper.model.helper.SoundHelper;
import com.ess.anime.wallpaper.ui.fragment.DonateFragment;
import com.ess.anime.wallpaper.ui.fragment.PoolFragment;
import com.ess.anime.wallpaper.ui.fragment.PostFragment;
import com.ess.anime.wallpaper.ui.view.CustomDialog;
import com.ess.anime.wallpaper.utils.UIUtils;
import com.google.android.material.internal.NavigationMenuView;
import com.google.android.material.navigation.NavigationView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import butterknife.BindView;

public class MainActivity extends BaseActivity {

    private final static String TAG_FRG_POST = PostFragment.TAG;
    private final static String TAG_FRG_POOL = PoolFragment.TAG;

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.nav_view)
    NavigationView mNavigation;

    private FragmentManager mFragmentManager;
    private PostFragment mFrgPost;
    private PoolFragment mFrgPool;

    private int mCurrentNavId;
    private Fragment mCurrentFragment;

    private boolean mIsForeground;

    @Override
    int layoutRes() {
        return R.layout.activity_main;
    }

    @Override
    void init(Bundle savedInstanceState) {
        mFragmentManager = getSupportFragmentManager();
        if (savedInstanceState != null) {
            restoreData(savedInstanceState);
        } else {
            mFrgPost = PostFragment.newInstance();
            mFrgPool = PoolFragment.newInstance();
        }

        initDrawerLayout();
        initNavMenuLayout();
        initNavHeaderLayout();

        if (savedInstanceState == null) {
            changeContentMainFragment(mFrgPost);
        }

        mIsForeground = true;
        EventBus.getDefault().register(this);

        checkToSearchTag(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkToSearchTag(intent);
    }

    private void checkToSearchTag(Intent intent) {
        if (intent != null && intent.hasExtra(Constants.SEARCH_MODE)) {
            mNavigation.setCheckedItem(R.id.nav_post);
            changeContentMainFragment(mFrgPost);
            Intent searchIntent = new Intent();
            searchIntent.putExtras(intent);
            mFrgPost.onActivityResult(Constants.SEARCH_CODE, Constants.SEARCH_CODE, searchIntent);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mFragmentManager.putFragment(outState, TAG_FRG_POST, mFrgPost);
        if (mFrgPool.isAdded()) {
            mFragmentManager.putFragment(outState, TAG_FRG_POOL, mFrgPool);
        }

        outState.putString(Constants.CURRENT_FRAGMENT, mCurrentFragment.getClass().getName());
    }

    private void restoreData(Bundle savedInstanceState) {
        mFrgPost = (PostFragment) mFragmentManager.getFragment(savedInstanceState, TAG_FRG_POST);
        mFrgPool = (PoolFragment) mFragmentManager.getFragment(savedInstanceState, TAG_FRG_POOL);
        if (mFrgPool == null) {
            mFrgPool = PoolFragment.newInstance();
        }

        String currentFrgName = savedInstanceState.getString(Constants.CURRENT_FRAGMENT, TAG_FRG_POST);
        if (TextUtils.equals(currentFrgName, TAG_FRG_POST)) {
            mCurrentNavId = R.id.nav_post;
            mCurrentFragment = mFrgPost;
        } else if (TextUtils.equals(currentFrgName, TAG_FRG_POOL)) {
            mCurrentNavId = R.id.nav_pool;
            mCurrentFragment = mFrgPool;
        }
    }

    private void initDrawerLayout() {
        mDrawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                switch (mCurrentNavId) {
                    case R.id.nav_post:
                        changeContentMainFragment(mFrgPost);
                        break;
                    case R.id.nav_pool:
                        changeContentMainFragment(mFrgPool);
                        break;
                    case R.id.nav_collection:
                        startActivity(new Intent(MainActivity.this, CollectionActivity.class));
                        break;
                    case R.id.nav_download_manager:
                        startActivity(new Intent(MainActivity.this, DownloadImageManagerActivity.class));
                        break;
                    case R.id.nav_pixiv_gif:
                        startActivity(new Intent(MainActivity.this, PixivGifActivity.class));
                        break;
                    case R.id.nav_sauce_nao:
                        startActivity(new Intent(MainActivity.this, SauceNaoActivity.class));
                        break;
                    case R.id.nav_trace_moe:
                        startActivity(new Intent(MainActivity.this, TraceMoeActivity.class));
                        break;
                    case R.id.nav_game:
                        startActivity(new Intent(MainActivity.this, GameActivity.class));
                        break;
                    case R.id.nav_github:
                        String url = "https://github.com/EternalSoySauce/Konachan";
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(Intent.createChooser(intent, getString(R.string.browse_title)));
                        break;
                    case R.id.nav_feedback:
                        CustomDialog.showFeedbackDialog(MainActivity.this);
                        break;
                    case R.id.nav_setting:
                        startActivity(new Intent(MainActivity.this, SettingActivity.class));
                        break;
                    case R.id.nav_donate:
                        FragmentManager manager = getSupportFragmentManager();
                        if (!manager.isDestroyed() && !manager.isStateSaved()) {
                            new DonateFragment().show(manager, null);
                        }
                        break;
                }
                mCurrentNavId = 0;
            }
        });
    }

    private void initNavMenuLayout() {
        mCurrentNavId = mCurrentNavId == 0 ? R.id.nav_post : mCurrentNavId;
        ColorStateList colorStateList = ResourcesCompat.getColorStateList(
                getResources(), R.color.nav_menu_text_color, null);
        mNavigation.setItemTextColor(colorStateList);
        mNavigation.setItemIconTintList(colorStateList);
        mNavigation.setItemBackgroundResource(R.drawable.bg_nav_menu);
        int lineColor = ResourcesCompat.getColor(getResources(), R.color.color_text_unselected, null);
        UIUtils.setNavigationMenuLineStyle(mNavigation, lineColor, UIUtils.dp2px(this, 0.5f));
        mNavigation.setCheckedItem(mCurrentNavId);
        mNavigation.setNavigationItemSelectedListener(item -> {
            mCurrentNavId = item.getItemId();
            toggleDrawerLayout();
            return mCurrentNavId == R.id.nav_post || mCurrentNavId == R.id.nav_pool;
        });

        NavigationMenuView navMenu = (NavigationMenuView) mNavigation.getChildAt(0);
        if (navMenu != null) {
            navMenu.setVerticalScrollBarEnabled(false);
        }

        // 临时解决切换fragment导致侧拉栏上移的问题
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            mNavigation.setOnApplyWindowInsetsListener(null);
        }
    }

    private void initNavHeaderLayout() {
        View navHeader = mNavigation.getHeaderView(0);

        // 切换搜图网站
        Button btnFunny = navHeader.findViewById(R.id.btn_funny);
        btnFunny.setOnClickListener(v -> {
            SoundHelper.getInstance().playToggleR18ModeSound(MainActivity.this);
            CustomDialog.showChangeBaseUrlDialog(MainActivity.this, new CustomDialog.SimpleDialogActionListener() {
                @Override
                public void onPositive() {
                    super.onPositive();
                    toggleDrawerLayout();
                }
            });
        });

        // 图片对应一周7天
        ImageView ivExtra = navHeader.findViewById(R.id.iv_extra);
        GlideApp.with(this).load(getExtraImageSrcId()).into(ivExtra);
    }

    private int getExtraImageSrcId() {
        Calendar calendar = Calendar.getInstance();
        int serial = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        String srcName = "ic_extra_" + serial;
        return getResources().getIdentifier(srcName, "drawable", getPackageName());
    }

    public void toggleDrawerLayout() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
    }

    public DrawerLayout getDrawerLayout() {
        return mDrawerLayout;
    }

    private void changeContentMainFragment(Fragment newFragment) {
        if (mCurrentFragment == newFragment) {
            return;
        }

        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        if (mCurrentFragment != null) {
            transaction.hide(mCurrentFragment);
        }

        if (newFragment.isAdded()) {
            transaction.show(newFragment);
        } else {
            transaction.add(R.id.fl_content_main, newFragment, newFragment.getClass().getName());
        }
        transaction.commitAllowingStateLoss();
        mCurrentFragment = newFragment;
    }

    private long lastClickTime = 0;

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            toggleDrawerLayout();
        } else if (mCurrentFragment == mFrgPool && mFrgPool.isPoolPostFragmentVisible()) {
            mFrgPool.removePoolPostFragment();
        } else if (mCurrentFragment != mFrgPost) {
            changeContentMainFragment(mFrgPost);
            mNavigation.setCheckedItem(R.id.nav_post);
        } else {
            if (lastClickTime <= 0) {
                Toast.makeText(this, R.string.back_again, Toast.LENGTH_SHORT).show();
                lastClickTime = System.currentTimeMillis();
            } else {
                long currentClickTime = System.currentTimeMillis();
                if (currentClickTime - lastClickTime < 2000) {
                    super.onBackPressed();
                } else {
                    Toast.makeText(this, R.string.back_again, Toast.LENGTH_SHORT).show();
                    lastClickTime = currentClickTime;
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            mIsForeground = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIsForeground = false;
        EventBus.getDefault().unregister(this);
        SoundHelper.getInstance().release();
        FireBase.getInstance().cancelAll();
    }

    // 检查到新版本后收到的通知, obj 为 ApkBean
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void showUpdateDialog(MsgBean msgBean) {
        if (msgBean.msg.equals(Constants.CHECK_UPDATE) && mIsForeground) {
            EventBus.getDefault().removeAllStickyEvents();
            ApkBean apkBean = (ApkBean) msgBean.obj;
            CustomDialog.showUpdateDialog(this, apkBean);
        }
    }
}
