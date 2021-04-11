package com.ess.anime.wallpaper.ui.activity.web;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.ui.view.CustomDialog;

public class TraceMoeActivity extends BaseWebActivity {

    @Override
    CharSequence title() {
        return getString(R.string.nav_trace_moe);
    }

    @Override
    boolean showReceivedTitle() {
        return false;
    }

    @Override
    String webUrl() {
        return "https://trace.moe/";
    }

    @Override
    void showHelpDialog() {
        CustomDialog.showWebsiteHelpDialog(this, title(), R.string.dialog_tracemoe_help);
    }

    @Override
    boolean hasHelpDialog() {
        return true;
    }

}
