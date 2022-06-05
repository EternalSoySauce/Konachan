package com.ess.anime.wallpaper.ui.activity;

import android.os.Bundle;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.adapter.RecyclerReverseSearchWebsiteAdapter;
import com.ess.anime.wallpaper.model.entity.ReverseSearchWebsiteItem;
import com.ess.anime.wallpaper.model.helper.ReverseSearchWebsiteDataHelper;

import java.util.List;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;

public class ReverseSearchActivity extends BaseActivity {

    @BindView(R.id.tool_bar)
    Toolbar mToolbar;
    @BindView(R.id.rv_website)
    RecyclerView mRvWebsite;

    @Override
    protected int layoutRes() {
        return R.layout.activity_reverse_search;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        initToolBarLayout();
        initRecyclerWebsite();
    }

    private void initToolBarLayout() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initRecyclerWebsite() {
        mRvWebsite.setLayoutManager(new LinearLayoutManager(this));
        mRvWebsite.setAdapter(new RecyclerReverseSearchWebsiteAdapter(getWebsiteItemList()));
    }

    private List<ReverseSearchWebsiteItem> getWebsiteItemList() {
        return ReverseSearchWebsiteDataHelper.getWebsiteItemList(this);
    }

}
