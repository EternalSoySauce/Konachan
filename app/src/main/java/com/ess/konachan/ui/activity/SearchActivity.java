package com.ess.konachan.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ListPopupWindow;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.ess.konachan.R;
import com.ess.konachan.adapter.ListSearchModePopupAdapter;
import com.ess.konachan.bean.SearchBean;
import com.ess.konachan.global.Constants;
import com.ess.konachan.http.OkHttp;
import com.ess.konachan.utils.FileUtils;
import com.ess.konachan.utils.StringUtils;
import com.ess.konachan.utils.UIUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

public class SearchActivity extends AppCompatActivity {

    private SharedPreferences mPreferences;
    private int mCurrentSearchMode;

    private EditText mEtSearch;
    private ListPopupWindow mPopup;
    private ListSearchModePopupAdapter mSpinnerAdapter;
    private int mSelectedPos;

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

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mCurrentSearchMode = mPreferences.getInt(Constants.SEARCH_MODE, Constants.SEARCH_CODE_TAGS);
        mSelectedPos = mCurrentSearchMode - Constants.SEARCH_CODE - 1;

        initViews();
        initListPopupWindow();
        changeEditAttrs();
        initTagList();
    }

    private void initViews() {
        // 下拉栏图标
        findViewById(R.id.iv_spinner).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopup.setAnchorView(v);
                mPopup.show();
                UIUtils.setBackgroundAlpha(SearchActivity.this, 0.4f);
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
                    if (!TextUtils.isEmpty(s)) {
                        if (mAutoCompleteTask != null) {
                            mAutoCompleteTask.cancel(true);
                        }
                        mPromptMap.clear();
                        mAutoCompleteTask = new AutoCompleteTagAsyncTask().execute(s.toString());
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
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

    private void initListPopupWindow() {
        // 选择搜索模式弹窗
        mPopup = new ListPopupWindow(this);
        String[] searchModeArray = getResources().getStringArray(R.array.spinner_list_item);
        mSpinnerAdapter = new ListSearchModePopupAdapter(this, searchModeArray);
        mSpinnerAdapter.setSelection(mSelectedPos);
        mPopup.setAdapter(mSpinnerAdapter);
        mPopup.setSelection(mSelectedPos);
        mPopup.setWidth(computePopupItemMaxWidth());
        mPopup.setHeight(ListPopupWindow.WRAP_CONTENT);
        mPopup.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
        mPopup.setModal(true);
        mPopup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position != mSelectedPos) {
                    mSelectedPos = position;
                    mSpinnerAdapter.setSelection(position);
                    mCurrentSearchMode = position + Constants.SEARCH_CODE + 1;
                    mPreferences.edit().putInt(Constants.SEARCH_MODE, mCurrentSearchMode).apply();
                    changeEditAttrs();
                }
                mPopup.dismiss();
            }
        });
        mPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                UIUtils.setBackgroundAlpha(SearchActivity.this, 1f);
            }
        });
    }

    // 使弹窗自适应文字宽度
    private int computePopupItemMaxWidth() {
        float maxWidth = 0;
        View layout = View.inflate(this, R.layout.list_item_popup_search_mode, null);
        TextView tv = (TextView) layout.findViewById(R.id.tv_search_mode);
        TextPaint paint = tv.getPaint();
        for (int i = 0; i < mSpinnerAdapter.getCount(); i++) {
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
        String searchMode = OkHttp.getSearchModeUrl(this);
        switch (searchMode) {
            case Constants.BASE_URL_SAFE_MODE:
                name = FileUtils.encodeMD5String(Constants.SAFE_MODE_TAG_JSON_URL);
                break;
            case Constants.BASE_URL_R18_MODE:
                name = FileUtils.encodeMD5String(Constants.R18_MODE_TAG_JSON_URL);
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
                    SearchBean searchBean = new SearchBean(details[0]);
                    searchBean.tagList.addAll(Arrays.asList(details).subList(1, details.length));
                    mSearchList.add(searchBean);
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
            if (mPromptMap.size() >= 10) {
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
            for (String prompt : mPromptMap.keySet()) {
                Log.i("rrr", prompt);
            }
        }
    }
}
