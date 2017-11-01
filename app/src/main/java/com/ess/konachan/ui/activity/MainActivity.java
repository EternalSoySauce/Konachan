package com.ess.konachan.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
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
import android.widget.Toast;
import android.widget.ToggleButton;

import com.ess.konachan.R;
import com.ess.konachan.bean.MsgBean;
import com.ess.konachan.global.Constants;
import com.ess.konachan.http.OkHttp;
import com.ess.konachan.other.Sound;
import com.ess.konachan.ui.fragment.PoolFragment;
import com.ess.konachan.ui.fragment.PostFragment;
import com.ess.konachan.utils.UIUtils;

import org.greenrobot.eventbus.EventBus;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        setContentView(R.layout.activity_main);

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
                        Intent collectionIntent = new Intent(MainActivity.this, CollectionActivity.class);
                        startActivity(collectionIntent);
                        break;
                    case R.id.nav_setting:
                        Intent settingIntent = new Intent(MainActivity.this, SettingActivity.class);
                        startActivity(settingIntent);
                        break;
                    case R.id.nav_game:
                        Intent gameIntent = new Intent(MainActivity.this, GameActivity.class);
                        startActivity(gameIntent);
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
        mNavigation.setItemBackgroundResource(R.drawable.nav_menu_background_color);
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
        boolean isR18Mode = mPreferences.getBoolean(Constants.IS_R18_MODE, false);
        View navHeader = mNavigation.getHeaderView(0);

        final ToggleButton btnFunny = (ToggleButton) navHeader.findViewById(R.id.btn_funny);
        btnFunny.setChecked(isR18Mode);
        btnFunny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = btnFunny.isChecked();
                if (isChecked) {
                    Sound.getInstance().playHentaiSound(MainActivity.this);
                }
                mPreferences.edit().putBoolean(Constants.IS_R18_MODE, isChecked).apply();
                // 发送通知到PostFragment, PoolFragment
                EventBus.getDefault().post(new MsgBean(Constants.TOGGLE_SCAN_MODE, null));
            }
        });
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
    protected void onDestroy() {
        super.onDestroy();
        Sound.getInstance().release();
        OkHttp.getInstance().cancelAll();
    }
}
