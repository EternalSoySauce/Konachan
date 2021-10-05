package com.ess.anime.wallpaper.ui.activity;

import android.os.Bundle;
import android.os.Looper;

import com.ess.anime.wallpaper.ui.fragment.LoadingFragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import butterknife.ButterKnife;

public abstract class BaseActivity extends AppCompatActivity {

    private LoadingFragment mLoadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layoutRes());
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        ButterKnife.bind(this);
        init(savedInstanceState);
    }

    protected abstract int layoutRes();

    protected abstract void init(Bundle savedInstanceState);

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
        super.onDestroy();
    }
}
