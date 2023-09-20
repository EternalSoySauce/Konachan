package com.ess.anime.wallpaper.ui.fragment;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.website.WebsiteManager;

public class PopularMonthlyFragment extends PopularBaseFragment {

    @Override
    String getWebsitePopularUrl(int year, int month, int day, int page) {
        return WebsiteManager.getInstance().getWebsiteConfig().getPopularMonthlyUrl(year, month, day, page);
    }

    @Override
    int getNotSupportTipsResId() {
        return R.string.popular_not_support_tips_monthly;
    }

}
