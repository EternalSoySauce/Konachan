package com.ess.anime.wallpaper.ui.activity;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.bean.MsgBean;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.ui.fragment.LoadingFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.lang.reflect.Method;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import butterknife.ButterKnife;

public abstract class BaseActivity extends AppCompatActivity {

    public final static Integer[] SUPPORT_SCREEN_ORIENTATIONS = {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT,
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE,
            ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
    };

    private LoadingFragment mLoadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(layoutOrientation());
        super.onCreate(savedInstanceState);
        setContentView(layoutRes());
        getWindow().setBackgroundDrawableResource(R.color.colorPrimaryDark);
        ButterKnife.bind(this);

        Method[] methods = getClass().getMethods();
        for (Method method : methods) {
            if (method.getAnnotation(Subscribe.class) != null) {
                if (!EventBus.getDefault().isRegistered(this)) {
                    EventBus.getDefault().register(this);
                }
                break;
            }
        }

        init(savedInstanceState);
        updateUI();
    }

    protected int layoutOrientation() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences.getInt(Constants.SCREEN_ORIENTATION, SUPPORT_SCREEN_ORIENTATIONS[0]);
    }

    protected abstract int layoutRes();

    protected abstract void init(Bundle savedInstanceState);

    protected void updateUI() {
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateUI();
    }

    protected void showLoadingSafely() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            showLoading();
        } else {
            runOnUiThread(this::showLoading);
        }
    }

    private void showLoading() {
        FragmentManager manager = getSupportFragmentManager();
        if (!manager.isDestroyed() && !manager.isStateSaved()) {
            if (mLoadingDialog == null) {
                mLoadingDialog = new LoadingFragment();
            }
            if (!mLoadingDialog.isAdded() && !mLoadingDialog.isStateSaved()) {
                mLoadingDialog.show(manager, null);
            }
        }
    }

    protected void hideLoadingSafely() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            hideLoading();
        } else {
            runOnUiThread(this::hideLoading);
        }
    }

    private void hideLoading() {
        if (mLoadingDialog != null) {
            mLoadingDialog.dismissAllowingStateLoss();
            mLoadingDialog = null;
        }
    }

    @Override
    protected void onDestroy() {
        hideLoading();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onDestroy();
    }

    // 设置页切换强制横屏开关后收到的通知，obj 为 null
    @Subscribe
    public void toggleScreenOrientation(MsgBean msgBean) {
        if (msgBean.msg.equals(Constants.TOGGLE_SCREEN_ORIENTATION)) {
            setRequestedOrientation(layoutOrientation());
        }
    }

}
