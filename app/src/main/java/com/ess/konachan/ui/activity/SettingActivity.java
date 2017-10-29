package com.ess.konachan.ui.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.ess.konachan.R;
import com.ess.konachan.view.CustomDialog;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        initToolBarLayout();
    }

    private void initToolBarLayout() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.layout_tag_type_help:
                CustomDialog.showTagTypeHelpDialog(SettingActivity.this);
                break;
            case R.id.layout_advanced_search_help:
                CustomDialog.showAdvancedSearchHelpDialog(SettingActivity.this);
                break;
        }
    }
}
