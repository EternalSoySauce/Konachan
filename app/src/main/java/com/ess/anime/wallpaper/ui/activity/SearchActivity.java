package com.ess.anime.wallpaper.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.adapter.RecyclerCompleteSearchAdapter;
import com.ess.anime.wallpaper.adapter.RecyclerSearchModePopupAdapter;
import com.ess.anime.wallpaper.bean.SearchBean;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.http.OkHttp;
import com.ess.anime.wallpaper.model.helper.DocDataHelper;
import com.ess.anime.wallpaper.ui.view.CustomDialog;
import com.ess.anime.wallpaper.utils.FileUtils;
import com.ess.anime.wallpaper.utils.StringUtils;
import com.ess.anime.wallpaper.utils.UIUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.jiang.android.indicatordialog.IndicatorBuilder;
import com.jiang.android.indicatordialog.IndicatorDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.jiang.android.indicatordialog.IndicatorBuilder.GRAVITY_LEFT;

public class SearchActivity extends BaseActivity {

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

    // 存储着K站所有的tag，用于搜索提示
    private ArrayList<SearchBean> mSearchList = new ArrayList<>();

    // 当前搜索内容的下拉提示内容
    private LinkedHashMap<String, Integer> mPromptMap = new LinkedHashMap<>();

    // 筛选下拉提示异步任务
    private AsyncTask mAutoCompleteTask;

    // Sankaku, Gelbooru网络请求下拉提示
    private Call mTagCall;

    @Override
    int layoutRes() {
        return R.layout.activity_search;
    }

    @Override
    void init(Bundle savedInstanceState) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mCurrentSearchMode = mPreferences.getInt(Constants.SEARCH_MODE, Constants.SEARCH_CODE_TAGS);
        mSelectedPos = mCurrentSearchMode - Constants.SEARCH_CODE - 1;

        initEditSearch();
        initSearchDocumentViews();
        initCompleteSearchRecyclerView();
        initListPopupWindow();
        changeEditAttrs();
        changeDocumentColor();
        initTagList();
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
                    setResult(mCurrentSearchMode, intent);
                    UIUtils.closeSoftInput(SearchActivity.this);
                    finish();
                }
            }
            return false;
        });

        mEtSearch.setFilters(new InputFilter[]{(source, start, end, dest, dstart, dend) -> {
            switch (mCurrentSearchMode) {
                case Constants.SEARCH_CODE_TAGS:
                    return source.toString().replace(" ", "_");
                case Constants.SEARCH_CODE_CHINESE:
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

                cancelAutoCompleteTask();
                cancelTagCall();
                if (mCurrentSearchMode == Constants.SEARCH_CODE_TAGS) {
                    // TODO 完善搜索提示（现在与K站算法不完全一样）
                    String tag = s.toString();
                    int splitIndex = Math.max(tag.lastIndexOf(","), tag.lastIndexOf("，"));
                    tag = tag.substring(splitIndex + 1);
                    if (!TextUtils.isEmpty(tag) && mUserInput) {
                        mPromptMap.clear();
                        switch (OkHttp.getBaseUrl(SearchActivity.this)) {
                            case Constants.BASE_URL_SANKAKU:
                                // Sankaku搜索提示需动态请求网络
                                getTagListFromNetwork("https://chan.sankakucomplex.com/tag/autosuggest?tag=" + tag);
                                break;
                            case Constants.BASE_URL_GELBOORU:
                                // Gelbooru搜索提示需动态请求网络
                                getTagListFromNetwork("https://gelbooru.com/index.php?page=autocomplete&term=" + tag);
                                break;
                            default:
                                mAutoCompleteTask = new AutoCompleteTagAsyncTask().execute(tag);
                                break;
                        }
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
        List<String> searchModeList = Arrays.asList(getResources().getStringArray(R.array.spinner_list_item));
        mSpinnerAdapter = new RecyclerSearchModePopupAdapter(searchModeList);
        mSpinnerAdapter.setSelection(mSelectedPos);
        mSpinnerAdapter.setOnItemClickListener((adapter, view, position) -> {
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
                // Danbooru没有搜索提示，借用Konachan(r18)的
                name = FileUtils.encodeMD5String(Constants.TAG_JSON_URL_KONACHAN_E);
                break;
            case Constants.BASE_URL_SANKAKU:
                // Sankaku搜索提示为动态请求：https://chan.sankakucomplex.com/tag/autosuggest?tag=xxx
                break;
            case Constants.BASE_URL_GELBOORU:
                // Gelbooru搜索提示为动态请求：https://gelbooru.com/index.php?page=autocomplete&term=xxx
                break;
        }

        File file = new File(path, name);
        if (file.exists() && file.isFile()) {
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
        search = search.replaceAll("[_\\-]", "");
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

    // Sankaku, Gelbooru搜索提示为动态请求
    private void getTagListFromNetwork(final String url) {
        cancelTagCall();
        mTagCall = OkHttp.getInstance().connect(url, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (OkHttp.isNetworkProblem(e) && call == mTagCall) {
                    getTagListFromNetwork(url);
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (call == mTagCall) {
                    if (response.isSuccessful()) {
                        String json = response.body().string();
                        JsonArray tagArray = null;
                        switch (OkHttp.getBaseUrl(SearchActivity.this)) {
                            case Constants.BASE_URL_SANKAKU:
                                tagArray = new JsonParser().parse(json).getAsJsonArray().get(1).getAsJsonArray();
                                break;
                            case Constants.BASE_URL_GELBOORU:
                                tagArray = new JsonParser().parse(json).getAsJsonArray();
                                break;
                        }
                        if (tagArray != null) {
                            for (int i = 0; i < tagArray.size(); i++) {
                                mPromptMap.put(tagArray.get(i).getAsString(), 0);
                            }
                        }
                        setCompleteSearchTags();
                    } else {
                        getTagListFromNetwork(url);
                    }
                }
                response.close();
            }
        });
    }

    private void setCompleteSearchTags() {
        runOnUiThread(() -> {
            mCompleteSearchAdapter.clear();
            mCompleteSearchAdapter.addData(mPromptMap.keySet());
            mRvCompleteSearch.setVisibility(View.VISIBLE);
        });
    }

    private void cancelAutoCompleteTask() {
        if (mAutoCompleteTask != null) {
            mAutoCompleteTask.cancel(true);
        }
    }

    private void cancelTagCall() {
        if (mTagCall != null && !mTagCall.isCanceled()) {
            mTagCall.cancel();
            mTagCall = null;
        }
    }

    @OnClick(R.id.tv_cancel_search)
    @Override
    public void finish() {
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelAutoCompleteTask();
        cancelTagCall();
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
            setCompleteSearchTags();
        }
    }
}
