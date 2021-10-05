package com.ess.anime.wallpaper.ui.activity;

import android.os.Bundle;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.adapter.RecyclerReverseSearchWebsiteAdapter;
import com.ess.anime.wallpaper.model.entity.ReverseSearchWebsiteItem;

import java.util.ArrayList;
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
        List<ReverseSearchWebsiteItem> items = new ArrayList<>();
        items.add(new ReverseSearchWebsiteItem(
                R.drawable.ic_reverse_search_website_1,
                R.string.website_name_sauce_nao,
                R.string.website_desc_sauce_nao,
                R.string.dialog_saucenao_help,
                "https://saucenao.com/"
        ));
        items.add(new ReverseSearchWebsiteItem(
                R.drawable.ic_reverse_search_website_2,
                R.string.website_name_trace_moe,
                R.string.website_desc_trace_moe,
                R.string.dialog_tracemoe_help,
                "https://trace.moe/"
        ));
        items.add(new ReverseSearchWebsiteItem(
                R.drawable.ic_reverse_search_website_3,
                R.string.website_name_ascii2d,
                R.string.website_desc_ascii2d,
                R.string.dialog_ascii2d_help,
                "https://ascii2d.net/"
        ));
        items.add(new ReverseSearchWebsiteItem(
                R.drawable.ic_reverse_search_website_4,
                R.string.website_name_yandex,
                R.string.website_desc_yandex,
                R.string.dialog_yandex_help,
                "https://yandex.com/"
        ));
        return items;
    }

}
