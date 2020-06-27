package com.ess.anime.wallpaper.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.ess.anime.wallpaper.R;

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
    void init(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            mHyperlink = getIntent().getStringExtra(HYPERLINK);
        } else {
            mHyperlink = savedInstanceState.getString(HYPERLINK);
        }
        findViewById(R.id.iv_help).setVisibility(View.GONE);
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
    void showHelpDialog() {
    }

}
