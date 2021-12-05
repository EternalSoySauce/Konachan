package com.ess.anime.wallpaper.ui.activity.web;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.just.agentweb.MiddlewareWebClientBase;

import androidx.annotation.NonNull;

public class HyperlinkActivity extends BaseWebActivity {

    private final static String HYPERLINK = "HYPERLINK";

    public static void launch(Context context, String hyperlink) {
        Intent intent = new Intent(context, HyperlinkActivity.class);
        intent.putExtra(HYPERLINK, hyperlink);
        context.startActivity(intent);
    }

    private String mHyperlink;

    @Override
    protected void init(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            mHyperlink = getIntent().getStringExtra(HYPERLINK);
        } else {
            mHyperlink = savedInstanceState.getString(HYPERLINK);
        }
        super.init(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(HYPERLINK, mHyperlink);
    }

    @Override
    CharSequence title() {
        return "";
    }

    @Override
    boolean showReceivedTitle() {
        return true;
    }

    @Override
    String webUrl() {
        return mHyperlink;
    }

    @Override
    MiddlewareWebClientBase customWebViewClient() {
        return null;
    }

    @Override
    void showHelpDialog() {
    }

    @Override
    boolean hasHelpDialog() {
        return false;
    }

}
