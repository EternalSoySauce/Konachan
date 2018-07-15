package com.lss.anime.wallpaper.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.lss.anime.wallpaper.global.Constants;

import java.util.ArrayList;

public class ViewPagerImageDetailAdapter extends FragmentPagerAdapter {

    private ArrayList<Fragment> mFragmentList;

    public ViewPagerImageDetailAdapter(FragmentManager fm, ArrayList<Fragment> fragmentList) {
        super(fm);
        mFragmentList = fragmentList;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList == null ? 0 : mFragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return getItem(position).getArguments().getString(Constants.PAGE_TITLE);
    }
}
