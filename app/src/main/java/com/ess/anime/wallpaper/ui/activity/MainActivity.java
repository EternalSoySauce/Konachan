package com.ess.anime.wallpaper.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.bean.ApkBean;
import com.ess.anime.wallpaper.bean.MsgBean;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.http.FireBase;
import com.ess.anime.wallpaper.http.OkHttp;
import com.ess.anime.wallpaper.other.GlideApp;
import com.ess.anime.wallpaper.other.Sound;
import com.ess.anime.wallpaper.ui.fragment.PoolFragment;
import com.ess.anime.wallpaper.ui.fragment.PostFragment;
import com.ess.anime.wallpaper.utils.UIUtils;
import com.ess.anime.wallpaper.view.CustomDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private final static String TAG_FRG_POST = PostFragment.class.getName();
    private final static String TAG_FRG_POOL = PoolFragment.class.getName();

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigation;

    private FragmentManager mFragmentManager;
    private PostFragment mFrgPost;
    private PoolFragment mFrgPool;

    private int mCurrentNavId;
    private Fragment mCurrentFragment;

    private SharedPreferences mPreferences;
    private boolean mIsForeground;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        setContentView(R.layout.activity_main);
        getWindow().setBackgroundDrawable(null);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

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
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
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
        if (currentFrgName.equals(TAG_FRG_POST)) {
            mCurrentNavId = R.id.nav_post;
            mCurrentFragment = mFrgPost;
        } else if (currentFrgName.equals(TAG_FRG_POOL)) {
            mCurrentNavId = R.id.nav_pool;
            mCurrentFragment = mFrgPool;
        }
    }

    private void initDrawerLayout() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
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
                    case R.id.nav_setting:
                        startActivity(new Intent(MainActivity.this, SettingActivity.class));
                        break;
                    case R.id.nav_sauce_nao:
                        startActivity(new Intent(MainActivity.this, SauceNaoActivity.class));
                        break;
                    case R.id.nav_game:
                        startActivity(new Intent(MainActivity.this, GameActivity.class));
                        break;
                }
                mCurrentNavId = 0;
            }
        });
    }

    private void initNavMenuLayout() {
        mCurrentNavId = mCurrentNavId == 0 ? R.id.nav_post : mCurrentNavId;
        mNavigation = (NavigationView) findViewById(R.id.nav_view);
        mNavigation.setItemTextColor(getResources().getColorStateList(R.color.nav_menu_text_color));
        mNavigation.setItemIconTintList(getResources().getColorStateList(R.color.nav_menu_text_color));
        mNavigation.setItemBackgroundResource(R.drawable.bg_nav_menu);
        UIUtils.setNavigationMenuLineStyle(mNavigation,
                getResources().getColor(R.color.color_text_unselected), UIUtils.dp2px(this, 0.5f));
        mNavigation.setCheckedItem(mCurrentNavId);
        mNavigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                mCurrentNavId = item.getItemId();
                toggleDrawerLayout();
                return mCurrentNavId == R.id.nav_post || mCurrentNavId == R.id.nav_pool;
            }
        });

        NavigationMenuView navMenu = (NavigationMenuView) mNavigation.getChildAt(0);
        if (navMenu != null) {
            navMenu.setVerticalScrollBarEnabled(false);
        }
    }

    private void initNavHeaderLayout() {
        View navHeader = mNavigation.getHeaderView(0);

        // 切换搜图网站
        final Button btnFunny = (Button) navHeader.findViewById(R.id.btn_funny);
        btnFunny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Sound.getInstance().playToggleR18ModeSound(MainActivity.this);
                CustomDialog.showChangeBaseUrlDialog(MainActivity.this, new CustomDialog.SimpleDialogActionListener() {
                    @Override
                    public void onPositive() {
                        super.onPositive();
                        toggleDrawerLayout();
                    }
                });
            }
        });

        // 图片对应一周7天
        final ImageView ivExtra = (ImageView) navHeader.findViewById(R.id.iv_extra);
        GlideApp.with(this).load(getExtraImageSrcId()).into(ivExtra);

        // 切换搜图网站
//        String baseUrl = OkHttp.getBaseUrl(this);
//        final List<String> baseList = Arrays.asList(Constants.BASE_URLS);
//        final SmoothRadioGroup rgChangeBaseUrl = (SmoothRadioGroup) navHeader.findViewById(R.id.rg_change_base_url);
//        rgChangeBaseUrl.check(rgChangeBaseUrl.getChildAt(baseList.indexOf(baseUrl)).getId());
//        rgChangeBaseUrl.setOnCheckedChangeListener(new SmoothRadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(SmoothRadioGroup smoothRadioGroup, int id) {
//                toggleDrawerLayout();
//                for (int i = 0; i < rgChangeBaseUrl.getChildCount(); i++) {
//                    View child = rgChangeBaseUrl.getChildAt(i);
//                    if (child.getId() == id) {
//                        mPreferences.edit().putString(Constants.BASE_URL, baseList.get(i)).apply();
//                        // 发送通知到PostFragment, PoolFragment
//                        EventBus.getDefault().post(new MsgBean(Constants.CHANGE_BASE_URL, null));
//                        break;
//                    }
//                }
//            }
//        });
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
        transaction.commit();
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
        Sound.getInstance().release();
        OkHttp.getInstance().cancelAll();
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
