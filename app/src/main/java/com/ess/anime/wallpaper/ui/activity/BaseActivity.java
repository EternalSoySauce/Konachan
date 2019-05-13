package com.ess.anime.wallpaper.ui.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.ButterKnife;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layoutRes());
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        ButterKnife.bind(this);
        init(savedInstanceState);
    }

    abstract int layoutRes();

    abstract void init(Bundle savedInstanceState);

}
