package com.ess.anime.wallpaper.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.adapter.RecyclerCompleteSearchAdapter;
import com.ess.anime.wallpaper.adapter.RecyclerSearchModePopupAdapter;
import com.ess.anime.wallpaper.database.GreenDaoUtils;
import com.ess.anime.wallpaper.database.SearchTagBean;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.ui.view.CustomDialog;
import com.ess.anime.wallpaper.ui.view.SearchHistoryLayout;
import com.ess.anime.wallpaper.ui.view.SearchModeDocLayout;
import com.ess.anime.wallpaper.utils.StringUtils;
import com.ess.anime.wallpaper.utils.UIUtils;
import com.ess.anime.wallpaper.website.search.SearchAutoCompleteManager;
import com.jiang.android.indicatordialog.IndicatorBuilder;
import com.jiang.android.indicatordialog.IndicatorDialog;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.OnClick;

import static com.jiang.android.indicatordialog.IndicatorBuilder.GRAVITY_LEFT;

public class SearchActivity extends BaseActivity {

    public final static String TAG = SearchActivity.class.getName();

    @BindView(R.id.et_search)
    EditText mEtSearch;
    @BindView(R.id.rv_auto_complete_search)
    RecyclerView mRvCompleteSearch;
    @BindView(R.id.tv_clear_all_search_history)
    TextView mTvClearAllSearchHistory;
    @BindView(R.id.smart_tab)
    SmartTabLayout mSmartTab;
    @BindView(R.id.vp_search)
    ViewPager mVpSearch;

    SearchModeDocLayout mLayoutSearchModeDoc;
    SearchHistoryLayout mLayoutSearchHistory;

    private RecyclerCompleteSearchAdapter mCompleteSearchAdapter;

    private SharedPreferences mPreferences;
    private int mCurrentSearchMode;
    private IndicatorDialog mPopup;
    private RecyclerSearchModePopupAdapter mSpinnerAdapter;
    private int mSelectedPos;

    // 判断EditText是用户输入还是setText的标志位，默认为true
    private boolean mUserInput = true;

    @Override
    int layoutRes() {
        return R.layout.activity_search;
    }

    @Override
    void init(Bundle savedInstanceState) {
        initData();
        initViewPager();
        initSlidingTabLayout();
        initEditSearch();
        initCompleteSearchRecyclerView();
        initListPopupWindow();
        changeEditAttrs();
        changeDocumentColor();
    }

    private void initData() {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mCurrentSearchMode = mPreferences.getInt(Constants.SEARCH_MODE, Constants.SEARCH_MODE_TAGS);
        mSelectedPos = mCurrentSearchMode - Constants.SEARCH_CODE - 1;
    }

    @OnClick(R.id.tv_clear_all_search_history)
    void clearAllSearchHistory() {
        CustomDialog.showClearAllSearchHistoryDialog(this, new CustomDialog.SimpleDialogActionListener() {
            @Override
            public void onPositive() {
                super.onPositive();
                GreenDaoUtils.deleteAllSearchTags();
                mLayoutSearchHistory.resetData();
            }
        });
    }

    private void initViewPager() {
        mLayoutSearchModeDoc = (SearchModeDocLayout) View.inflate(this, R.layout.layout_search_mode_doc, null);
        mLayoutSearchHistory = (SearchHistoryLayout) View.inflate(this, R.layout.layout_search_history, null);
        PagerAdapter pagerAdapter = new PagerAdapter() {
            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                if (position == 0) {
                    container.addView(mLayoutSearchModeDoc);
                    return mLayoutSearchModeDoc;
                } else if (position == 1) {
                    container.addView(mLayoutSearchHistory);
                    return mLayoutSearchHistory;
                }
                return super.instantiateItem(container, position);
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                container.removeView((View) object);
            }

            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == object;
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                if (position == 0) {
                    return getString(R.string.search_instruction);
                } else if (position == 1) {
                    return getString(R.string.search_history);
                }
                return super.getPageTitle(position);
            }
        };
        ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                mTvClearAllSearchHistory.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
            }
        };
        mVpSearch.setAdapter(pagerAdapter);
        mVpSearch.setOffscreenPageLimit(pagerAdapter.getCount());
        mVpSearch.addOnPageChangeListener(onPageChangeListener);
        mVpSearch.setCurrentItem(1);
        onPageChangeListener.onPageSelected(mVpSearch.getCurrentItem());
    }

    private void initSlidingTabLayout() {
        mSmartTab.setViewPager(mVpSearch);
    }

    // 下拉栏图标
    @OnClick(R.id.iv_spinner)
    void changeSearchMode(View view) {
        mPopup.show(view);
        UIUtils.closeSoftInput(SearchActivity.this);
    }

    // 清空搜索内容
    @OnClick(R.id.iv_clear)
    void clearSearch() {
        mEtSearch.setText("");
    }

    private void initEditSearch() {
        mEtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String tags = mEtSearch.getText().toString().trim();
                if (!TextUtils.isEmpty(tags)) {
                    GreenDaoUtils.updateSearchTag(new SearchTagBean(tags, mCurrentSearchMode, System.currentTimeMillis()));
                    Intent intent = new Intent();
                    intent.putExtra(Constants.SEARCH_TAG, tags);
                    intent.putExtra(Constants.SEARCH_MODE, mCurrentSearchMode);
                    setResult(Constants.SEARCH_CODE, intent);
                    UIUtils.closeSoftInput(SearchActivity.this);
                    finish();
                }
            }
            return false;
        });

        mEtSearch.setFilters(new InputFilter[]{(source, start, end, dest, dstart, dend) -> {
            switch (mCurrentSearchMode) {
                case Constants.SEARCH_MODE_TAGS:
                    return source.toString().replace(" ", "_");
                case Constants.SEARCH_MODE_CHINESE:
                    Pattern pattern = Pattern.compile("[\u4e00-\u9fa5·]+");
                    return StringUtils.filter(source.toString(), pattern);
                default:
                    return source;
            }
        }});

        mEtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int visible = TextUtils.isEmpty(s) ? View.GONE : View.VISIBLE;
                findViewById(R.id.iv_clear).setVisibility(visible);

                SearchAutoCompleteManager.getInstance().stopTask();
                if (mCurrentSearchMode == Constants.SEARCH_MODE_TAGS) {
                    String tag = s.toString();
                    int splitIndex = Math.max(tag.lastIndexOf(","), tag.lastIndexOf("，"));
                    tag = tag.substring(splitIndex + 1);
                    if (!TextUtils.isEmpty(tag) && mUserInput) {
                        SearchAutoCompleteManager.getInstance().startTask(tag, promptList -> {
                            mCompleteSearchAdapter.setNewData(promptList);
                            mRvCompleteSearch.setVisibility(View.VISIBLE);
                        });
                    } else {
                        mCompleteSearchAdapter.setNewData(null);
                        mRvCompleteSearch.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                mUserInput = true;
            }
        });
    }

    private void changeEditAttrs() {
        mEtSearch.setText("");
        mEtSearch.setHint(mSpinnerAdapter.getItem(mSelectedPos));
        if (mCurrentSearchMode == Constants.SEARCH_MODE_ID) {
            mEtSearch.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        } else {
            mEtSearch.setInputType(EditorInfo.TYPE_CLASS_TEXT);
        }
    }

    private void changeDocumentColor() {
        if (mLayoutSearchModeDoc != null) {
            mLayoutSearchModeDoc.changeDocumentColor(mSelectedPos);
        }
    }

    private void initCompleteSearchRecyclerView() {
        mRvCompleteSearch.setLayoutManager(new LinearLayoutManager(this));
        mCompleteSearchAdapter = new RecyclerCompleteSearchAdapter();
        mCompleteSearchAdapter.setOnItemClickListener((adapter, view, position) -> {
            String text = mEtSearch.getText().toString();
            int splitIndex = Math.max(text.lastIndexOf(","), text.lastIndexOf("，"));
            String newText = text.substring(0, splitIndex + 1) + mCompleteSearchAdapter.getItem(position);
            mUserInput = false;
            mEtSearch.setText(newText);
            mEtSearch.setSelection(newText.length());
        });
        mRvCompleteSearch.setAdapter(mCompleteSearchAdapter);
    }

    private void initListPopupWindow() {
        // 选择搜索模式弹窗
        List<String> searchModeList = Arrays.asList(getResources().getStringArray(R.array.spinner_list_item_search_mode));
        mSpinnerAdapter = new RecyclerSearchModePopupAdapter(searchModeList);
        mSpinnerAdapter.setSelection(mSelectedPos);
        mSpinnerAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (position != mSelectedPos) {
                mSelectedPos = position;
                mSpinnerAdapter.setSelection(position);
                mCurrentSearchMode = position + Constants.SEARCH_CODE + 1;
                mPreferences.edit().putInt(Constants.SEARCH_MODE, mCurrentSearchMode).apply();
                mCompleteSearchAdapter.setNewData(null);
                mRvCompleteSearch.setVisibility(View.GONE);
                changeEditAttrs();
                changeDocumentColor();
            }
            mPopup.dismiss();
        });

        mPopup = new IndicatorBuilder(this)
                .width(computePopupItemMaxWidth())
                .height(-1)
                .bgColor(getResources().getColor(R.color.colorPrimary))
                .dimEnabled(false)
                .gravity(GRAVITY_LEFT)
                .ArrowDirection(IndicatorBuilder.TOP)
                .ArrowRectage(0.16f)
                .radius(8)
                .layoutManager(new LinearLayoutManager(this))
                .adapter(mSpinnerAdapter)
                .create();
        mPopup.setCanceledOnTouchOutside(true);
        mPopup.getDialog().setOnShowListener(dialog -> UIUtils.setBackgroundAlpha(this, 0.4f));
        mPopup.getDialog().setOnDismissListener(dialog -> UIUtils.setBackgroundAlpha(this, 1f));
    }

    // 使弹窗自适应文字宽度
    private int computePopupItemMaxWidth() {
        float maxWidth = 0;
        View layout = View.inflate(this, R.layout.recyclerview_item_popup_search_mode, null);
        TextView tv = layout.findViewById(R.id.tv_search_mode);
        TextPaint paint = tv.getPaint();
        for (int i = 0; i < mSpinnerAdapter.getItemCount(); i++) {
            String item = mSpinnerAdapter.getItem(i);
            maxWidth = Math.max(maxWidth, paint.measureText(item));
        }
        maxWidth += tv.getPaddingStart() + tv.getPaddingEnd();
        return (int) maxWidth;
    }

    @Override
    public void onBackPressed() {
        if (mVpSearch.getCurrentItem() == 1 && mLayoutSearchHistory.isEditing()) {
            mLayoutSearchHistory.cancelEdit();
        } else {
            super.onBackPressed();
        }
    }

    @OnClick(R.id.tv_cancel_search)
    @Override
    public void finish() {
        super.finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isFinishing()) {
            SearchAutoCompleteManager.getInstance().stopTask();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SearchAutoCompleteManager.getInstance().stopTask();
    }

}
