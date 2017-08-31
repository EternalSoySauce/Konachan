package com.ess.konachan.ui.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.ess.konachan.R;
import com.ess.konachan.ui.fragment.SettingFragment;

public class SettingActivity extends AppCompatActivity {

    private FragmentManager mFragmentManager;
    private Fragment mSettingFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mFragmentManager = getFragmentManager();
        mSettingFragment = savedInstanceState == null ? SettingFragment.newInstance()
                : mFragmentManager.getFragment(savedInstanceState, SettingFragment.class.getName());

        if (!mSettingFragment.isAdded()) {
            mFragmentManager.beginTransaction()
                    .add(R.id.fl_content_setting, mSettingFragment, SettingFragment.class.getName())
                    .commit();
        }

        initToolBarLayout();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mFragmentManager.putFragment(outState, SettingFragment.class.getName(), mSettingFragment);
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
}
