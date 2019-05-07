package com.ess.anime.wallpaper.ui.activity;

import com.ess.anime.wallpaper.R;

public class TraceMoeActivity extends BaseWebActivity {

    @Override
    int titleRes() {
        return R.string.nav_trace_moe;
    }

    @Override
    String webUrl() {
        return "https://trace.moe/";
    }

}
