package com.ess.anime.wallpaper.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.widget.Toolbar;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.listener.OnTouchScaleListener;
import com.ess.anime.wallpaper.ui.view.GameSurfaceView;

import butterknife.BindView;
import butterknife.OnClick;

public class GameActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.tool_bar)
    Toolbar mToolbar;
    @BindView(R.id.surface_view_game)
    GameSurfaceView mGameView;
    @BindView(R.id.iv_game)
    ImageView mIvGame;

    @Override
    int layoutRes() {
        return R.layout.activity_game;
    }

    @Override
    void init(Bundle savedInstanceState) {
        initToolBarLayout();
        initGameSurfaceView();
        initViews();
    }

    private void initToolBarLayout() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initGameSurfaceView() {
        mGameView.setOnActionListener(bitmap -> mIvGame.setImageBitmap(bitmap));
    }

    private void initViews() {
        mIvGame.setImageBitmap(mGameView.getGameBitmap());

        OnTouchScaleListener touchListener = new OnTouchScaleListener(0.9f);
        findViewById(R.id.btn_column_3).setOnTouchListener(touchListener);
        findViewById(R.id.btn_column_4).setOnTouchListener(touchListener);
        findViewById(R.id.btn_column_5).setOnTouchListener(touchListener);
        findViewById(R.id.btn_restart).setOnTouchListener(touchListener);
    }

    @OnClick({R.id.btn_column_3, R.id.btn_column_4, R.id.btn_column_5, R.id.btn_restart})
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_column_3:
                mGameView.changeColumn(3);
                break;
            case R.id.btn_column_4:
                mGameView.changeColumn(4);
                break;
            case R.id.btn_column_5:
                mGameView.changeColumn(5);
                break;
            case R.id.btn_restart:
                mGameView.restartGame();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGameView.recycleBitmaps(true);
    }
}
