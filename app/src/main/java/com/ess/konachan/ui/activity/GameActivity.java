package com.ess.konachan.ui.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.ess.konachan.R;
import com.ess.konachan.view.GameSurfaceView;

public class GameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        initToolBarLayout();
        initView();
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

    private void initView() {
        final GameSurfaceView surfaceView = (GameSurfaceView) findViewById(R.id.surface_view_game);

        Button btnColumn3 = (Button) findViewById(R.id.btn_column_3);
        btnColumn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                surfaceView.changeColumn(3);
            }
        });

        Button btnColumn4 = (Button) findViewById(R.id.btn_column_4);
        btnColumn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                surfaceView.changeColumn(4);
            }
        });

        Button btnColumn5 = (Button) findViewById(R.id.btn_column_5);
        btnColumn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                surfaceView.changeColumn(5);
            }
        });

        Button btnRestart = (Button) findViewById(R.id.btn_restart);
        btnRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                surfaceView.restartGame();
            }
        });

    }
}
