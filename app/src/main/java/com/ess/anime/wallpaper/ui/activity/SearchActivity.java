package com.ess.anime.wallpaper.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.adapter.RecyclerCompleteSearchAdapter;
import com.ess.anime.wallpaper.adapter.RecyclerSearchModePopupAdapter;
import com.ess.anime.wallpaper.bean.SearchBean;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.global.DocData;
import com.ess.anime.wallpaper.http.OkHttp;
import com.ess.anime.wallpaper.utils.FileUtils;
import com.ess.anime.wallpaper.utils.StringUtils;
import com.ess.anime.wallpaper.utils.UIUtils;
import com.ess.anime.wallpaper.view.CustomDialog;
import com.jiang.android.indicatordialog.IndicatorBuilder;
import com.jiang.android.indicatordialog.IndicatorDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

import static com.jiang.android.indicatordialog.IndicatorBuilder.GRAVITY_LEFT;

public class SearchActivity extends AppCompatActivity {

    private SharedPreferences mPreferences;
    private int mCurrentSearchMode;

    private EditText mEtSearch;
    private LinearLayout mLayoutDocSearchMode;
    private RecyclerView mRvCompleteSearch;
    private RecyclerCompleteSearchAdapter mCompleteSearchAdapter;
    private IndicatorDialog mPopup;
    private RecyclerSearchModePopupAdapter mSpinnerAdapter;
    private int mSelectedPos;

    // 判断EditText是用户输入还是setText的标志位，默认为true
    private boolean mUserInput = true;

    // 存储着K站所有的tag，用于搜索提示
    private ArrayList<SearchBean> mSearchList = new ArrayList<>();

    // 当前搜索内容的下拉提示内容
    private LinkedHashMap<String, Integer> mPromptMap = new LinkedHashMap<>();

    // 筛选下拉提示异步任务
    private AsyncTask mAutoCompleteTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        getWindow().setBackgroundDrawable(null);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mCurrentSearchMode = mPreferences.getInt(Constants.SEARCH_MODE, Constants.SEARCH_CODE_TAGS);
        mSelectedPos = mCurrentSearchMode - Constants.SEARCH_CODE - 1;

        initSearchViews();
        initSearchDocumentViews();
        initCompleteSearchRecyclerView();
        initListPopupWindow();
        changeEditAttrs();
        changeDocumentColor();
        initTagList();
    }

    private void initSearchViews() {
        // 下拉栏图标
        findViewById(R.id.iv_spinner).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopup.show(v);
                UIUtils.closeSoftInput(SearchActivity.this, mEtSearch);
            }
        });

        // 清空搜索内容
        findViewById(R.id.iv_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEtSearch.setText("");
            }
        });

        // 取消搜索
        findViewById(R.id.tv_cancel_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mEtSearch = (EditText) findViewById(R.id.et_search);
        mEtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String tags = mEtSearch.getText().toString().trim();
                    if (!TextUtils.isEmpty(tags)) {
                        Intent intent = new Intent();
                        intent.putExtra(Constants.SEARCH_TAG, tags);
                        setResult(mCurrentSearchMode, intent);
                        UIUtils.closeSoftInput(SearchActivity.this, mEtSearch);
                        finish();
                    }
                }
                return false;
            }
        });
        mEtSearch.setFilters(new InputFilter[]{new InputFilter() {

            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                switch (mCurrentSearchMode) {
                    case Constants.SEARCH_CODE_TAGS:
                        return source.toString().replace(" ", "_");
                    case Constants.SEARCH_CODE_CHINESE:
                        Pattern pattern = Pattern.compile("[\u4e00-\u9fa5·]+");
                        return StringUtils.filter(source.toString(), pattern);
                    default:
                        return source;
                }
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

                if (mCurrentSearchMode == Constants.SEARCH_CODE_TAGS) {
                    // TODO 完善搜索提示（现在与K站算法不完全一样）
                    String tag = s.toString();
                    int splitIndex = Math.max(tag.lastIndexOf(","), tag.lastIndexOf("，"));
                    tag = tag.substring(splitIndex + 1);
                    if (!TextUtils.isEmpty(tag) && mUserInput) {
                        if (mAutoCompleteTask != null) {
                            mAutoCompleteTask.cancel(true);
                        }
                        mPromptMap.clear();
                        mAutoCompleteTask = new AutoCompleteTagAsyncTask().execute(tag);
                    } else {
                        mCompleteSearchAdapter.clear();
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
        if (mCurrentSearchMode == Constants.SEARCH_CODE_ID) {
            mEtSearch.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        } else {
            mEtSearch.setInputType(EditorInfo.TYPE_CLASS_TEXT);
        }
    }

    private void initSearchDocumentViews() {
        ArrayList<String> docList = DocData.getSearchModeDocumentList(this);
        mLayoutDocSearchMode = (LinearLayout) findViewById(R.id.layout_doc_search_mode);

        TextView tvDocSearchTag = (TextView) findViewById(R.id.tv_doc_search_tag);
        tvDocSearchTag.setText(docList.get(0));

        TextView tvDocSearchId = (TextView) findViewById(R.id.tv_doc_search_id);
        tvDocSearchId.setText(docList.get(1));

        TextView tvDocSearchChinese = (TextView) findViewById(R.id.tv_doc_search_chinese);
        tvDocSearchChinese.setText(docList.get(2));


        TextView tvDocSearchAdvanced = (TextView) findViewById(R.id.tv_doc_search_advanced);
        tvDocSearchAdvanced.setText(setLinkToShowTagTypeDoc(docList.get(3)));
        tvDocSearchAdvanced.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private SpannableString setLinkToShowTagTypeDoc(String baseText) {
        SpannableString spanText = new SpannableString(getString(R.string.click_here, baseText));
        spanText.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
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
        mRvCompleteSearch = (RecyclerView) findViewById(R.id.rv_auto_complete_search);
        mRvCompleteSearch.setLayoutManager(new LinearLayoutManager(this));
        mCompleteSearchAdapter = new RecyclerCompleteSearchAdapter(new RecyclerCompleteSearchAdapter.onItemClickListener() {
            @Override
            public void onItemClick(String tag) {
                String text = mEtSearch.getText().toString();
                int splitIndex = Math.max(text.lastIndexOf(","), text.lastIndexOf("，"));
                String newText = text.substring(0, splitIndex + 1) + tag;
                mUserInput = false;
                mEtSearch.setText(newText);
                mEtSearch.setSelection(newText.length());
            }
        });
        mRvCompleteSearch.setAdapter(mCompleteSearchAdapter);
    }

    private void initListPopupWindow() {
        // 选择搜索模式弹窗
        String[] searchModeArray = getResources().getStringArray(R.array.spinner_list_item);
        mSpinnerAdapter = new RecyclerSearchModePopupAdapter(this, searchModeArray);
        mSpinnerAdapter.setSelection(mSelectedPos);
        mSpinnerAdapter.setOnItemClickListener(new RecyclerSearchModePopupAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (position != mSelectedPos) {
                    mSelectedPos = position;
                    mSpinnerAdapter.setSelection(position);
                    mCurrentSearchMode = position + Constants.SEARCH_CODE + 1;
                    mPreferences.edit().putInt(Constants.SEARCH_MODE, mCurrentSearchMode).apply();
                    mCompleteSearchAdapter.clear();
                    mRvCompleteSearch.setVisibility(View.GONE);
                    changeEditAttrs();
                    changeDocumentColor();
                }
                mPopup.dismiss();
            }
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
        mPopup.getDialog().setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                UIUtils.setBackgroundAlpha(SearchActivity.this, 0.4f);
            }
        });
        mPopup.getDialog().setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                UIUtils.setBackgroundAlpha(SearchActivity.this, 1f);
            }
        });
    }

    // 使弹窗自适应文字宽度
    private int computePopupItemMaxWidth() {
        float maxWidth = 0;
        View layout = View.inflate(this, R.layout.recyclerview_item_popup_search_mode, null);
        TextView tv = (TextView) layout.findViewById(R.id.tv_search_mode);
        TextPaint paint = tv.getPaint();
        for (int i = 0; i < mSpinnerAdapter.getItemCount(); i++) {
            String item = mSpinnerAdapter.getItem(i);
            maxWidth = Math.max(maxWidth, paint.measureText(item));
        }
        maxWidth += tv.getPaddingStart() + tv.getPaddingEnd();
        return (int) maxWidth;
    }

    // 获取json文件里所有的搜索标签
    private void initTagList() {
        String name = "";
        String path = getFilesDir().getPath();
        String baseUrl = OkHttp.getBaseUrl(this);
        switch (baseUrl) {
            case Constants.BASE_URL_KONACHAN_S:
                name = FileUtils.encodeMD5String(Constants.TAG_JSON_URL_KONACHAN_S);
                break;
            case Constants.BASE_URL_KONACHAN_E:
                name = FileUtils.encodeMD5String(Constants.TAG_JSON_URL_KONACHAN_E);
                break;
            case Constants.BASE_URL_YANDE:
                name = FileUtils.encodeMD5String(Constants.TAG_JSON_URL_YANDE);
                break;
            case Constants.BASE_URL_LOLIBOORU:
                name = FileUtils.encodeMD5String(Constants.TAG_JSON_URL_LOLIBOORU);
                break;
            case Constants.BASE_URL_DANBOORU:
                // Danbooru没有搜索提示
                break;
        }

        File file = new File(path, name);
        if (file.exists()) {
            String json = FileUtils.fileToString(file);
            json = json == null ? "" : json;
            try {
                String data = new JSONObject(json).getString("data");
                String[] tags = data.split(" ");
                for (String tag : tags) {
                    String[] details = tag.split("`");
                    if (details.length > 1) {
                        SearchBean searchBean = new SearchBean(details[0]);
                        searchBean.tagList.addAll(Arrays.asList(details).subList(1, details.length));
                        mSearchList.add(searchBean);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    // 筛选当前搜索内容的提示标签，最多10个
    // 去除"_" "-"等连字符
    private void getTagList(String search) {
        search = search.replaceAll("_|-", "");
        if (!TextUtils.isEmpty(search)) {
            int length = search.length();
            for (int i = 0; i <= length; i++) {
                String start = search.substring(0, length - i).toLowerCase();
                String contain = search.substring(length - i).toLowerCase();
                filter(start, contain);
                if (mPromptMap.size() >= 10) {
                    break;
                }
            }
        }
    }

    // 层级筛选
    // 例：搜索fla，筛选顺序为
    // startWith("fla")
    // -> startWidth("fl"), contains("a")
    // -> startWith("f"), contains("la")
    // -> contains("fla")
    private void filter(String start, String contain) {
        for (SearchBean searchBean : mSearchList) {
            for (String tag : searchBean.tagList) {
                boolean find = false;
                String[] parts = tag.split("_");
                for (String part : parts) {
                    if (part.startsWith(start) && part.contains(contain)) {
                        if (!mPromptMap.containsKey(tag)) {
                            mPromptMap.put(tag, searchBean.colorId);
                        }
                        find = true;
                        break;
                    }
                }
                if (find) {
                    break;
                }
            }
            if (mPromptMap.size() >= 15) {
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAutoCompleteTask != null) {
            mAutoCompleteTask.cancel(true);
        }
    }

    // 异步执行筛选下拉提示操作
    private class AutoCompleteTagAsyncTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            getTagList(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mCompleteSearchAdapter.clear();
            mCompleteSearchAdapter.addDatas(mPromptMap.keySet());
            mRvCompleteSearch.setVisibility(View.VISIBLE);
        }
    }
}
