package com.ess.anime.wallpaper.ui.activity.web;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.ess.anime.wallpaper.model.entity.ReverseSearchWebsiteItem;
import com.ess.anime.wallpaper.ui.view.CustomDialog;
import com.just.agentweb.MiddlewareWebClientBase;

import androidx.annotation.NonNull;

public class ReverseSearchWebsiteActivity extends BaseWebActivity {

    private final static String WEBSITE_ENTITY = "WEBSITE_ENTITY";

    public static void launch(Context context, ReverseSearchWebsiteItem websiteItem) {
        Intent intent = new Intent(context, ReverseSearchWebsiteActivity.class);
        intent.putExtra(WEBSITE_ENTITY, websiteItem);
        context.startActivity(intent);
    }

    private ReverseSearchWebsiteItem mWebsiteItem;

    @Override
    protected void init(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            mWebsiteItem = getIntent().getParcelableExtra(WEBSITE_ENTITY);
        } else {
            mWebsiteItem = savedInstanceState.getParcelable(WEBSITE_ENTITY);
        }
        super.init(savedInstanceState);
        if (mWebsiteItem == null) {
            finish();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(WEBSITE_ENTITY, mWebsiteItem);
    }

    @Override
    CharSequence title() {
        if (mWebsiteItem != null) {
            return getString(mWebsiteItem.websiteNameRes);
        } else {
            return "";
        }
    }

    @Override
    boolean showReceivedTitle() {
        return false;
    }

    @Override
    String webUrl() {
        if (mWebsiteItem != null) {
            return mWebsiteItem.websiteUrl;
        } else {
            return null;
        }
    }

    @Override
    MiddlewareWebClientBase customWebViewClient() {
        return null;
    }

    @Override
    void showHelpDialog() {
        if (mWebsiteItem != null && hasHelpDialog()) {
            CustomDialog.showWebsiteHelpDialog(this, title(), mWebsiteItem.websiteHelpRes);
        }
    }

    @Override
    boolean hasHelpDialog() {
        if (mWebsiteItem != null) {
            return mWebsiteItem.websiteHelpRes != 0;
        } else {
            return false;
        }
    }

}
