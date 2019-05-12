package com.ess.anime.wallpaper.view;

import com.chad.library.adapter.base.loadmore.LoadMoreView;
import com.ess.anime.wallpaper.R;

public final class CustomLoadMoreView extends LoadMoreView {

    @Override
    public int getLayoutId() {
        return R.layout.layout_load_more;
    }

    @Override
    protected int getLoadingViewId() {
        return R.id.layout_loading;
    }

    @Override
    protected int getLoadFailViewId() {
        return R.id.layout_load_fail;
    }

    @Override
    protected int getLoadEndViewId() {
        return 0;
    }

}
