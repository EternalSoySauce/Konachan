package com.ess.anime.wallpaper.ui.activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.DatePicker;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.listener.DoubleTapEffector;
import com.ess.anime.wallpaper.model.viewmodel.PopularWebsiteViewModel;
import com.ess.anime.wallpaper.ui.fragment.PopularBaseFragment;
import com.ess.anime.wallpaper.ui.fragment.PopularDailyFragment;
import com.ess.anime.wallpaper.ui.fragment.PopularMonthlyFragment;
import com.ess.anime.wallpaper.ui.fragment.PopularOverallFragment;
import com.ess.anime.wallpaper.ui.fragment.PopularWeeklyFragment;
import com.ess.anime.wallpaper.utils.UIUtils;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import java.util.Calendar;
import java.util.Locale;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.OnClick;

public class PopularActivity extends BaseActivity {

    @BindView(R.id.tool_bar)
    ViewGroup mToolbar;
    @BindView(R.id.smart_tab)
    SmartTabLayout mSmartTab;
    @BindView(R.id.vp_popular)
    ViewPager mVpPopular;

    private PopularWebsiteViewModel mViewModel;

    @Override
    protected int layoutRes() {
        return R.layout.activity_popular;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        initData();
        initToolBarLayout();
        initViewPager();
        initSlidingTabLayout();
    }

    private void initData() {
        mViewModel = new ViewModelProvider(this).get(PopularWebsiteViewModel.class);
        mViewModel.setCalenderData(Calendar.getInstance());
    }

    private void initToolBarLayout() {
        //双击返回顶部
        DoubleTapEffector.addDoubleTapEffect(mToolbar, () -> {
            PagerAdapter adapter = mVpPopular.getAdapter();
            if (adapter instanceof FragmentPagerItemAdapter) {
                Fragment fragment = ((FragmentPagerItemAdapter) adapter).getItem(mVpPopular.getCurrentItem());
                if (fragment instanceof PopularBaseFragment) {
                    ((PopularBaseFragment) fragment).scrollToTop();
                }
            }
        });
    }

    private void initViewPager() {
        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add(R.string.popular_title_daily, PopularDailyFragment.class)
                .add(R.string.popular_title_weekly, PopularWeeklyFragment.class)
                .add(R.string.popular_title_monthly, PopularMonthlyFragment.class)
                .add(R.string.popular_title_overall, PopularOverallFragment.class)
                .create());
        mVpPopular.setAdapter(adapter);
        mVpPopular.setOffscreenPageLimit(adapter.getCount());
        for (int i = 0; i < adapter.getCount(); i++) {
            PopularBaseFragment fragment = (PopularBaseFragment) adapter.getItem(i);
            if (fragment.supportWebsitePopular()) {
                mVpPopular.setCurrentItem(i);
                break;
            }
        }
    }

    private void initSlidingTabLayout() {
        ViewGroup.LayoutParams layoutParams = mSmartTab.getLayoutParams();
        if (getResources().getConfiguration().locale.getLanguage().equals(Locale.CHINESE.getLanguage())) {
            layoutParams.width = UIUtils.dp2px(this, 300);
        } else {
            layoutParams.width = UIUtils.dp2px(this, 335);
        }
        mSmartTab.setLayoutParams(layoutParams);
        mSmartTab.setViewPager(mVpPopular);
    }

    @OnClick(R.id.iv_calendar)
    void selectCalenderData() {
        int year = mViewModel.getCalenderYear();
        int month = mViewModel.getCalenderMonth();
        int day = mViewModel.getCalenderDay();

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        mViewModel.setCalenderData(year, month, dayOfMonth);
                    }
                }, year, month, day);

        datePickerDialog.show();
    }

    @OnClick(R.id.iv_back)
    @Override
    public void finish() {
        super.finish();
    }

}
