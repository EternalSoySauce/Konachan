package com.ess.anime.wallpaper.ui.activity.web;

import com.ess.anime.wallpaper.R;

public class YandexImagesActivity extends BaseWebActivity {

    @Override
    CharSequence title() {
        return getString(R.string.nav_yandex);
    }

    @Override
    boolean showReceivedTitle() {
        return false;
    }

    @Override
    String webUrl() {
        return "https://yandex.com/images/";
    }

    @Override
    void showHelpDialog() {
    }

    @Override
    boolean hasHelpDialog() {
        return false;
    }

}
