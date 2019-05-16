package com.ess.anime.wallpaper.ui.activity;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.ui.view.CustomDialog;

public class TraceMoeActivity extends BaseWebActivity {

    @Override
    int titleRes() {
        return R.string.nav_trace_moe;
    }

    @Override
    String webUrl() {
        return "https://trace.moe/";
    }

    @Override
    void showHelpDialog() {
        CustomDialog.showWebsiteHelpDialog(this, titleRes(), R.string.dialog_tracemoe_help);
    }

}
