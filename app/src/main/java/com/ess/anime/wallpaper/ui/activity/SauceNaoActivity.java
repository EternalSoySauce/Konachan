package com.ess.anime.wallpaper.ui.activity;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.ui.view.CustomDialog;

public class SauceNaoActivity extends BaseWebActivity {

    @Override
    int titleRes() {
        return R.string.nav_sauce_nao;
    }

    @Override
    String webUrl() {
        return "https://saucenao.com/";
    }

    @Override
    void showHelpDialog() {
        CustomDialog.showWebsiteHelpDialog(this, titleRes(), R.string.dialog_saucenao_help);
    }

}
