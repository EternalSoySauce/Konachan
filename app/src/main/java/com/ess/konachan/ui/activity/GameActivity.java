package com.ess.konachan.ui.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.ess.konachan.R;
import com.ess.konachan.listener.OnTouchScaleListener;
import com.ess.konachan.view.GameSurfaceView;

public class GameActivity extends AppCompatActivity implements View.OnClickListener {

    private GameSurfaceView mGameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

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
        mGameView = (GameSurfaceView) findViewById(R.id.surface_view_game);

        OnTouchScaleListener touchListener = new OnTouchScaleListener(0.9f);
        findViewById(R.id.btn_column_3).setOnTouchListener(touchListener);
        findViewById(R.id.btn_column_4).setOnTouchListener(touchListener);
        findViewById(R.id.btn_column_5).setOnTouchListener(touchListener);
        findViewById(R.id.btn_restart).setOnTouchListener(touchListener);
    }

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
