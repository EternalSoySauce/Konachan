package com.ess.anime.wallpaper.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.adapter.RecyclerCompleteSearchAdapter;
import com.ess.anime.wallpaper.adapter.RecyclerSearchModePopupAdapter;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.model.helper.DocDataHelper;
import com.ess.anime.wallpaper.website.search.SearchAutoCompleteManager;
import com.ess.anime.wallpaper.ui.view.CustomDialog;
import com.ess.anime.wallpaper.utils.StringUtils;
import com.ess.anime.wallpaper.utils.UIUtils;
import com.jiang.android.indicatordialog.IndicatorBuilder;
import com.jiang.android.indicatordialog.IndicatorDialog;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.OnClick;

import static com.jiang.android.indicatordialog.IndicatorBuilder.GRAVITY_LEFT;

public class SearchActivity extends BaseActivity {

    public final static String TAG = SearchActivity.class.getName();

    @BindView(R.id.et_search)
    EditText mEtSearch;
    @BindView(R.id.layout_doc_search_mode)
    LinearLayout mLayoutDocSearchMode;
    @BindView(R.id.rv_auto_complete_search)
    RecyclerView mRvCompleteSearch;

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
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mCurrentSearchMode = mPreferences.getInt(Constants.SEARCH_MODE, Constants.SEARCH_MODE_TAGS);
        mSelectedPos = mCurrentSearchMode - Constants.SEARCH_CODE - 1;

        initEditSearch();
        initSearchDocumentViews();
        initCompleteSearchRecyclerView();
        initListPopupWindow();
        changeEditAttrs();
        changeDocumentColor();
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

    private void initSearchDocumentViews() {
        List<String> docList = DocDataHelper.getSearchModeDocumentList(this);

        TextView tvDocSearchTag = findViewById(R.id.tv_doc_search_tag);
        tvDocSearchTag.setText(docList.get(0));

        TextView tvDocSearchId = findViewById(R.id.tv_doc_search_id);
        tvDocSearchId.setText(docList.get(1));

        TextView tvDocSearchChinese = findViewById(R.id.tv_doc_search_chinese);
        tvDocSearchChinese.setText(docList.get(2));

        TextView tvDocSearchAdvanced = findViewById(R.id.tv_doc_search_advanced);
        tvDocSearchAdvanced.setText(setLinkToShowTagTypeDoc(docList.get(3)));
        tvDocSearchAdvanced.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private SpannableString setLinkToShowTagTypeDoc(String baseText) {
        SpannableString spanText = new SpannableString(getString(R.string.click_here, baseText));
        spanText.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                CustomDialog.showAdvancedSearchHelpDialog(SearchActivity.this);
            }
        }, baseText.length(), spanText.length() - 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        spanText.setSpan(new UnderlineSpan(), baseText.length(),
                spanText.length() - 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        spanText.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.color_link)),
                baseText.length(), spanText.length() - 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return spanText;
    }

    private void changeDocumentColor() {
        for (int i = 0; i < mLayoutDocSearchMode.getChildCount(); i++) {
            TextView tv = (TextView) mLayoutDocSearchMode.getChildAt(i);
            int textColor = i == mSelectedPos ? R.color.color_text_selected : R.color.color_text_unselected;
            tv.setTextColor(getResources().getColor(textColor));
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
